package ml.docilealligator.infinityforreddit.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.FetchMyInfo;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SessionHolder;
import ml.docilealligator.infinityforreddit.apis.RedditAccountsAPI;
import ml.docilealligator.infinityforreddit.asynctasks.ParseAndInsertNewAccount;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityLoginBinding;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import ml.docilealligator.infinityforreddit.utils.XHmac;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends BaseActivity {
    private static final String ENABLE_DOM_STATE = "EDS";
    private static final String IS_AGREE_TO_USER_AGGREMENT_STATE = "IATUAS";

    private ActivityLoginBinding binding;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("login")
    Retrofit mLoginRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private String authCode;
    private boolean enableDom = false;

    public static final String formatting(String str, long seconds) {
        String format = String.format(Locale.US, "%d:%s:%d:%d:%s",
                Arrays.copyOf(new Object[] { 1, "android", 2, Long.valueOf(seconds), str }, 5));
        return format;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);

        try {
            binding = ActivityLoginBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
        } catch (InflateException ie) {
            Log.e("LoginActivity", "Failed to inflate LoginActivity: " + ie.getMessage());
            Toast.makeText(LoginActivity.this, R.string.no_system_webview_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            enableDom = savedInstanceState.getBoolean(ENABLE_DOM_STATE);
        }

        CircularProgressIndicatorSpec spec = new CircularProgressIndicatorSpec(LoginActivity.this, null, 0, com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall);
        IndeterminateDrawable<CircularProgressIndicatorSpec> progressIndicatorDrawable =
                IndeterminateDrawable.createCircularDrawable(LoginActivity.this, spec);

        MaterialButton loginButton = binding.loginButton;
        loginButton.setOnClickListener(view -> {
            loginButton.setClickable(false);
            loginButton.setIcon(progressIndicatorDrawable);
            RedditAccountsAPI api = mLoginRetrofit.create(RedditAccountsAPI.class);
            String username = binding.textUsername.getText().toString();
            String password = binding.textPassword.getText().toString();

            if(username.isBlank() || password.isBlank()){
                Toast.makeText(LoginActivity.this, "Username or password is blank", Toast.LENGTH_LONG).show();
                loginButton.setIcon(null);
                loginButton.setClickable(true);
                return;
            }

            String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

            Map<String, String> loginHeaders = APIUtils.getHttpBasicAuthHeader();

            Locale locale = Locale.US;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            String msg = String.format(locale, "Epoch:%d|Body:%s",
                    Arrays.copyOf(new Object[] { Long.valueOf(seconds), body }, 2));

            String hmacBody = XHmac.getSignedHexString(msg);
            loginHeaders.put("x-hmac-signed-body",formatting(hmacBody, seconds));

            String dummyDeviceID = UUID.randomUUID().toString();
            loginHeaders.put("client-vendor-id", dummyDeviceID);

            String result = String.format(locale, "Epoch:%d|User-Agent:%s|Client-Vendor-ID:%s",
                    Arrays.copyOf(new Object[] { Long.valueOf(seconds), APIUtils.USER_AGENT, dummyDeviceID}, 3));
            String hmacResult = XHmac.getSignedHexString(result);
            loginHeaders.put("x-hmac-signed-result",formatting(hmacResult, seconds));


            Call<String> loginCall = api.login(loginHeaders, body);

            loginCall.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJSON = new JSONObject(response.body());
                            if (!responseJSON.getBoolean("success")) {
                                String explanation = responseJSON.getJSONObject("error").getString("explanation");
                                Toast.makeText(LoginActivity.this, explanation, Toast.LENGTH_LONG).show();
                                loginButton.setIcon(null);
                                loginButton.setClickable(true);
                                return;
                            }
                            String sessionCookie = response.headers().get("set-cookie");
                            String redditSession = sessionCookie.split("; ")[0].trim();
                            String sessionExpiryDate = sessionCookie.split(";")[4].split(",")[1].trim();
                            String sessionPattern = "dd-MMM-yyyy HH:mm:ss z";
                            SimpleDateFormat formatter = new SimpleDateFormat(sessionPattern, new Locale("en", "US"));
                            String sessionExpiryTimestamp = String.valueOf(formatter.parse(sessionExpiryDate).getTime());

                            mCurrentAccountSharedPreferences.edit()
                                    .putString(SharedPreferencesUtils.SESSION_COOKIE, redditSession)
                                    .putString(SharedPreferencesUtils.SESSION_EXPIRY, sessionExpiryTimestamp)
                                    .apply();

                            SessionHolder.INSTANCE.setCurrentSession(null);

                            Map<String, String> accessTokenHeaders = APIUtils.getHttpBasicAuthHeader();
                            accessTokenHeaders.put("cookie", redditSession);
                            Call<String> accessTokenCall = api.getAccessToken(accessTokenHeaders, APIUtils.SCOPE);
                            accessTokenCall.enqueue(new Callback<>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    if (response.isSuccessful()) {
                                        String accountResponse = response.body();
                                        if (accountResponse == null) {
                                            //Handle error
                                            loginButton.setIcon(null);
                                            loginButton.setClickable(true);
                                            return;
                                        }

                                        JSONObject responseJSON;
                                        try {
                                            responseJSON = new JSONObject(accountResponse);
                                            String accessToken = responseJSON.getString(APIUtils.ACCESS_TOKEN_KEY);
                                            int expiry = responseJSON.getInt(APIUtils.EXPIRY_TS_KEY);

                                            FetchMyInfo.fetchAccountInfo(mOauthRetrofit, mRedditDataRoomDatabase,
                                                    accessToken, new FetchMyInfo.FetchMyInfoListener() {
                                                        @Override
                                                        public void onFetchMyInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma) {
                                                            mCurrentAccountSharedPreferences.edit().putString(SharedPreferencesUtils.ACCESS_TOKEN, accessToken)
                                                                    .putString(SharedPreferencesUtils.ACCOUNT_NAME, name)
                                                                    .putInt(APIUtils.EXPIRY_TS_KEY, expiry)
                                                                    .putString(SharedPreferencesUtils.ACCOUNT_IMAGE_URL, profileImageUrl).apply();
                                                            ParseAndInsertNewAccount.parseAndInsertNewAccount(mExecutor, new Handler(), name, accessToken, "", profileImageUrl, bannerImageUrl,
                                                                    karma, authCode, redditSession, sessionExpiryTimestamp, mRedditDataRoomDatabase.accountDao(),
                                                                    () -> {
                                                                        Intent resultIntent = new Intent();
                                                                        setResult(Activity.RESULT_OK, resultIntent);
                                                                        finish();
                                                                    });
                                                        }

                                                        @Override
                                                        public void onFetchMyInfoFailed(boolean parseFailed) {
                                                            if (parseFailed) {
                                                                Toast.makeText(LoginActivity.this, R.string.parse_user_info_error, Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(LoginActivity.this, R.string.cannot_fetch_user_info, Toast.LENGTH_SHORT).show();
                                                            }

                                                            finish();
                                                        }
                                                    });
                                        } catch (JSONException e) {
                                            loginButton.setIcon(null);
                                            loginButton.setClickable(true);
                                            e.printStackTrace();
                                        }

                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(LoginActivity.this, R.string.retrieve_token_error, Toast.LENGTH_SHORT).show();
                                    t.printStackTrace();
                                    finish();
                                }
                            });
                        } catch (JSONException e) {
                            loginButton.setIcon(null);
                            loginButton.setClickable(true);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }


                    } else {
                        Toast.makeText(LoginActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

        });

        binding.fab.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.have_trouble_login_title)
                    .setMessage(R.string.have_trouble_login_message)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        enableDom = !enableDom;
                        ActivityCompat.recreate(this);
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });

        binding.textPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginButton.performClick();
                    return true;
                }
                return false;
            }
        });

        if (enableDom) {
            binding.twoFaInfoTextView.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ENABLE_DOM_STATE, enableDom);
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        TextView twoFaInfoTextView = binding.twoFaInfoTextView;
        binding.coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayout, null, binding.toolbar);
        twoFaInfoTextView.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        Drawable infoDrawable = Utils.getTintedDrawable(this, R.drawable.ic_info_preference_24dp, mCustomThemeWrapper.getPrimaryIconColor());
        twoFaInfoTextView.setCompoundDrawablesWithIntrinsicBounds(infoDrawable, null, null, null);
        applyFABTheme(binding.fab);
        if (typeface != null) {
            twoFaInfoTextView.setTypeface(typeface);
        }
        binding.textUsername.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        binding.textPassword.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        binding.loginButton.setBackgroundColor(customThemeWrapper.getColorAccent());
        binding.loginButton.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }
}
