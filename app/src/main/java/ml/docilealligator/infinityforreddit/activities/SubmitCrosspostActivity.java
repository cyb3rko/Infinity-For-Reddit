package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.Flair;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.asynctasks.LoadSubredditIcon;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.AccountChooserBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FlairBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivitySubmitCrosspostBinding;
import ml.docilealligator.infinityforreddit.events.SubmitCrosspostEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.services.SubmitPostService;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class SubmitCrosspostActivity extends BaseActivity implements FlairBottomSheetFragment.FlairSelectionCallback,
        AccountChooserBottomSheetFragment.AccountChooserListener {

    public static final String EXTRA_POST = "EP";

    private static final String SELECTED_ACCOUNT_STATE = "SAS";
    private static final String SUBREDDIT_NAME_STATE = "SNS";
    private static final String SUBREDDIT_ICON_STATE = "SIS";
    private static final String SUBREDDIT_SELECTED_STATE = "SSS";
    private static final String SUBREDDIT_IS_USER_STATE = "SIUS";
    private static final String LOAD_SUBREDDIT_ICON_STATE = "LSIS";
    private static final String IS_POSTING_STATE = "IPS";
    private static final String FLAIR_STATE = "FS";
    private static final String IS_SPOILER_STATE = "ISS";
    private static final String IS_NSFW_STATE = "INS";

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;
    
    private ActivitySubmitCrosspostBinding binding;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("gql")
    Retrofit mGqlRetrofit;
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
    private Account selectedAccount;
    private String mAccessToken;
    private Post post;
    private String iconUrl;
    private String subredditName;
    private boolean subredditSelected = false;
    private boolean subredditIsUser;
    private boolean loadSubredditIconSuccessful = true;
    private boolean isPosting;
    private int primaryTextColor;
    private int flairBackgroundColor;
    private int flairTextColor;
    private int spoilerBackgroundColor;
    private int spoilerTextColor;
    private int nsfwBackgroundColor;
    private int nsfwTextColor;
    private Flair flair;
    private boolean isSpoiler = false;
    private boolean isNSFW = false;
    private Resources resources;
    private Menu mMenu;
    private RequestManager mGlide;
    private FlairBottomSheetFragment flairSelectionBottomSheetFragment;
    private Snackbar mPostingSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivitySubmitCrosspostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appbarLayout);
        }

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGlide = Glide.with(this);

        mPostingSnackbar = Snackbar.make(binding.coordinatorLayout, R.string.posting, Snackbar.LENGTH_INDEFINITE);

        resources = getResources();

        post = getIntent().getParcelableExtra(EXTRA_POST);

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);

        if (savedInstanceState != null) {
            selectedAccount = savedInstanceState.getParcelable(SELECTED_ACCOUNT_STATE);
            subredditName = savedInstanceState.getString(SUBREDDIT_NAME_STATE);
            iconUrl = savedInstanceState.getString(SUBREDDIT_ICON_STATE);
            subredditSelected = savedInstanceState.getBoolean(SUBREDDIT_SELECTED_STATE);
            subredditIsUser = savedInstanceState.getBoolean(SUBREDDIT_IS_USER_STATE);
            loadSubredditIconSuccessful = savedInstanceState.getBoolean(LOAD_SUBREDDIT_ICON_STATE);
            isPosting = savedInstanceState.getBoolean(IS_POSTING_STATE);
            flair = savedInstanceState.getParcelable(FLAIR_STATE);
            isSpoiler = savedInstanceState.getBoolean(IS_SPOILER_STATE);
            isNSFW = savedInstanceState.getBoolean(IS_NSFW_STATE);

            if (selectedAccount != null) {
                mGlide.load(selectedAccount.getProfileImageUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(binding.accountIconGifImageView);

                binding.accountNameTextView.setText(selectedAccount.getAccountName());
            } else {
                loadCurrentAccount();
            }

            if (subredditName != null) {
                binding.subredditNameTextView.setTextColor(primaryTextColor);
                binding.subredditNameTextView.setText(subredditName);
                binding.flairCustomTextView.setVisibility(View.VISIBLE);
                if (!loadSubredditIconSuccessful) {
                    loadSubredditIcon();
                }
            }
            displaySubredditIcon();

            if (isPosting) {
                mPostingSnackbar.show();
            }

            if (flair != null) {
                binding.flairCustomTextView.setText(flair.getText());
                binding.flairCustomTextView.setBackgroundColor(flairBackgroundColor);
                binding.flairCustomTextView.setBorderColor(flairBackgroundColor);
                binding.flairCustomTextView.setTextColor(flairTextColor);
            }
            if (isSpoiler) {
                binding.spoilerCustomTextView.setBackgroundColor(spoilerBackgroundColor);
                binding.spoilerCustomTextView.setBorderColor(spoilerBackgroundColor);
                binding.spoilerCustomTextView.setTextColor(spoilerTextColor);
            }
            if (isNSFW) {
                binding.nsfwCustomTextView.setBackgroundColor(nsfwBackgroundColor);
                binding.nsfwCustomTextView.setBorderColor(nsfwBackgroundColor);
                binding.nsfwCustomTextView.setTextColor(nsfwTextColor);
            }
        } else {
            isPosting = false;

            loadCurrentAccount();

            mGlide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .into(binding.subredditIconGifImageView);

            if (post.isSpoiler()) {
                binding.spoilerCustomTextView.setBackgroundColor(spoilerBackgroundColor);
                binding.spoilerCustomTextView.setBorderColor(spoilerBackgroundColor);
                binding.spoilerCustomTextView.setTextColor(spoilerTextColor);
            }
            if (post.isNSFW()) {
                binding.nsfwCustomTextView.setBackgroundColor(nsfwBackgroundColor);
                binding.nsfwCustomTextView.setBorderColor(nsfwBackgroundColor);
                binding.nsfwCustomTextView.setTextColor(nsfwTextColor);
            }

            binding.postTitleEditText.setText(post.getTitle());
        }

        if (post.getPostType() == Post.TEXT_TYPE) {
            binding.postContentTextView.setVisibility(View.VISIBLE);
            binding.postContentTextView.setText(post.getSelfTextPlain());
        } else if (post.getPostType() == Post.LINK_TYPE || post.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
            binding.postContentTextView.setVisibility(View.VISIBLE);
            binding.postContentTextView.setText(post.getUrl());
        } else {
            Post.Preview preview = getPreview(post);
            if (preview != null) {
                binding.frameLayout.setVisibility(View.VISIBLE);
                mGlide.asBitmap().load(preview.getPreviewUrl()).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        binding.imageView.setImage(ImageSource.bitmap(resource));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

                if (post.getPostType() == Post.VIDEO_TYPE || post.getPostType() == Post.GIF_TYPE) {
                    binding.playButtonImageView.setVisibility(View.VISIBLE);
                    binding.playButtonImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_circle_36dp));
                } else if (post.getPostType() == Post.GALLERY_TYPE) {
                    binding.playButtonImageView.setVisibility(View.VISIBLE);
                    binding.playButtonImageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_gallery_24dp));
                }
            }
        }

        binding.accountLinearLayout.setOnClickListener(view -> {
            AccountChooserBottomSheetFragment fragment = new AccountChooserBottomSheetFragment();
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });

        binding.subredditIconGifImageView.setOnClickListener(view -> {
            binding.subredditNameTextView.performClick();
        });

        binding.subredditNameTextView.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubredditSelectionActivity.class);
            intent.putExtra(SubredditSelectionActivity.EXTRA_SPECIFIED_ACCOUNT, selectedAccount);
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });

        binding.rulesButton.setOnClickListener(view -> {
            if (subredditName == null) {
                Snackbar.make(binding.coordinatorLayout, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, RulesActivity.class);
                if (subredditIsUser) {
                    intent.putExtra(RulesActivity.EXTRA_SUBREDDIT_NAME, "u_" + subredditName);
                } else {
                    intent.putExtra(RulesActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                }
                startActivity(intent);
            }
        });

        binding.flairCustomTextView.setOnClickListener(view -> {
            if (flair == null) {
                flairSelectionBottomSheetFragment = new FlairBottomSheetFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FlairBottomSheetFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                if (subredditIsUser) {
                    bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, "u_" + subredditName);
                } else {
                    bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, subredditName);
                }
                flairSelectionBottomSheetFragment.setArguments(bundle);
                flairSelectionBottomSheetFragment.show(getSupportFragmentManager(), flairSelectionBottomSheetFragment.getTag());
            } else {
                binding.flairCustomTextView.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.flairCustomTextView.setTextColor(primaryTextColor);
                binding.flairCustomTextView.setText(getString(R.string.flair));
                flair = null;
            }
        });

        binding.spoilerCustomTextView.setOnClickListener(view -> {
            if (!isSpoiler) {
                binding.spoilerCustomTextView.setBackgroundColor(spoilerBackgroundColor);
                binding.spoilerCustomTextView.setBorderColor(spoilerBackgroundColor);
                binding.spoilerCustomTextView.setTextColor(spoilerTextColor);
                isSpoiler = true;
            } else {
                binding.spoilerCustomTextView.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.spoilerCustomTextView.setTextColor(primaryTextColor);
                isSpoiler = false;
            }
        });

        binding.nsfwCustomTextView.setOnClickListener(view -> {
            if (!isNSFW) {
                binding.nsfwCustomTextView.setBackgroundColor(nsfwBackgroundColor);
                binding.nsfwCustomTextView.setBorderColor(nsfwBackgroundColor);
                binding.nsfwCustomTextView.setTextColor(nsfwTextColor);
                isNSFW = true;
            } else {
                binding.nsfwCustomTextView.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.nsfwCustomTextView.setTextColor(primaryTextColor);
                isNSFW = false;
            }
        });

        binding.receivePostReplyNotificationsLinearLayout.setOnClickListener(view -> {
            binding.receivePostReplyNotificationsSwitch.performClick();
        });
    }

    private void loadCurrentAccount() {
        Handler handler = new Handler();
        mExecutor.execute(() -> {
            Account account = mRedditDataRoomDatabase.accountDao().getCurrentAccount();
            selectedAccount = account;
            handler.post(() -> {
                if (!isFinishing() && !isDestroyed() && account != null) {
                    mGlide.load(account.getProfileImageUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(binding.accountIconGifImageView);

                    binding.accountNameTextView.setText(account.getAccountName());
                }
            });
        });
    }

    @Nullable
    private Post.Preview getPreview(Post post) {
        ArrayList<Post.Preview> previews = post.getPreviews();
        if (previews != null && !previews.isEmpty()) {
            return previews.get(0);
        }

        return null;
    }

    @Override
    protected SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        binding.coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(
                binding.appbarLayout, null, binding.toolbar);
        primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        binding.accountNameTextView.setTextColor(primaryTextColor);
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        binding.subredditNameTextView.setTextColor(secondaryTextColor);
        binding.rulesButton.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        binding.rulesButton.setBackgroundColor(mCustomThemeWrapper.getColorPrimaryLightTheme());
        binding.receivePostReplyNotificationsTextView.setTextColor(primaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        binding.divider1.setBackgroundColor(dividerColor);
        binding.divider2.setBackgroundColor(dividerColor);
        binding.divider3.setBackgroundColor(dividerColor);
        binding.divider4.setBackgroundColor(dividerColor);
        flairBackgroundColor = mCustomThemeWrapper.getFlairBackgroundColor();
        flairTextColor = mCustomThemeWrapper.getFlairTextColor();
        spoilerBackgroundColor = mCustomThemeWrapper.getSpoilerBackgroundColor();
        spoilerTextColor = mCustomThemeWrapper.getSpoilerTextColor();
        nsfwBackgroundColor = mCustomThemeWrapper.getNsfwBackgroundColor();
        nsfwTextColor = mCustomThemeWrapper.getNsfwTextColor();
        binding.flairCustomTextView.setTextColor(primaryTextColor);
        binding.spoilerCustomTextView.setTextColor(primaryTextColor);
        binding.nsfwCustomTextView.setTextColor(primaryTextColor);
        binding.postTitleEditText.setTextColor(primaryTextColor);
        binding.postTitleEditText.setHintTextColor(secondaryTextColor);
        binding.postContentTextView.setTextColor(primaryTextColor);
        binding.postContentTextView.setHintTextColor(secondaryTextColor);
        binding.playButtonImageView.setColorFilter(mCustomThemeWrapper.getMediaIndicatorIconColor(), PorterDuff.Mode.SRC_IN);
        binding.playButtonImageView.setBackgroundTintList(ColorStateList.valueOf(mCustomThemeWrapper.getMediaIndicatorBackgroundColor()));
    }

    private void displaySubredditIcon() {
        if (iconUrl != null && !iconUrl.equals("")) {
            mGlide.load(iconUrl)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(binding.subredditIconGifImageView);
        } else {
            mGlide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .into(binding.subredditIconGifImageView);
        }
    }

    private void loadSubredditIcon() {
        LoadSubredditIcon.loadSubredditIcon(mExecutor, new Handler(), mRedditDataRoomDatabase, subredditName,
                mAccessToken, mOauthRetrofit, mRetrofit, mGqlRetrofit, iconImageUrl -> {
            iconUrl = iconImageUrl;
            displaySubredditIcon();
            loadSubredditIconSuccessful = true;
        });
    }

    private void promptAlertDialog(int titleResId, int messageResId) {
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(titleResId)
                .setMessage(messageResId)
                .setPositiveButton(R.string.yes, (dialogInterface, i)
                        -> finish())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.submit_crosspost_activity, menu);
        applyMenuItemTheme(menu);
        mMenu = menu;
        if (isPosting) {
            mMenu.findItem(R.id.action_send_submit_crosspost_activity).setEnabled(false);
            mMenu.findItem(R.id.action_send_submit_crosspost_activity).getIcon().setAlpha(130);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (isPosting) {
                promptAlertDialog(R.string.exit_when_submit, R.string.exit_when_submit_post_detail);
                return true;
            } else {
                if (!binding.postTitleEditText.getText().toString().equals("")) {
                    promptAlertDialog(R.string.discard, R.string.discard_detail);
                    return true;
                }
            }
            finish();
            return true;
        } else if (itemId == R.id.action_send_submit_crosspost_activity) {
            if (!subredditSelected) {
                Snackbar.make(binding.coordinatorLayout, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (binding.postTitleEditText.getText() == null || binding.postTitleEditText.getText().toString().equals("")) {
                Snackbar.make(binding.coordinatorLayout, R.string.title_required, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            isPosting = true;

            item.setEnabled(false);
            item.getIcon().setAlpha(130);

            mPostingSnackbar.show();

            String subredditName;
            if (subredditIsUser) {
                subredditName = "u_" + binding.subredditNameTextView.getText().toString();
            } else {
                subredditName = binding.subredditNameTextView.getText().toString();
            }

            Intent intent = new Intent(this, SubmitPostService.class);
            intent.putExtra(SubmitPostService.EXTRA_ACCOUNT, selectedAccount);
            intent.putExtra(SubmitPostService.EXTRA_SUBREDDIT_NAME, subredditName);
            intent.putExtra(SubmitPostService.EXTRA_TITLE, binding.postTitleEditText.getText().toString());
            if (post.isCrosspost()) {
                intent.putExtra(SubmitPostService.EXTRA_CONTENT, "t3_" + post.getCrosspostParentId());
            } else {
                intent.putExtra(SubmitPostService.EXTRA_CONTENT, post.getFullName());
            }
            intent.putExtra(SubmitPostService.EXTRA_KIND, APIUtils.KIND_CROSSPOST);
            intent.putExtra(SubmitPostService.EXTRA_FLAIR, flair);
            intent.putExtra(SubmitPostService.EXTRA_IS_SPOILER, isSpoiler);
            intent.putExtra(SubmitPostService.EXTRA_IS_NSFW, isNSFW);
            intent.putExtra(SubmitPostService.EXTRA_RECEIVE_POST_REPLY_NOTIFICATIONS, binding.receivePostReplyNotificationsSwitch.isChecked());
            intent.putExtra(SubmitPostService.EXTRA_POST_TYPE, SubmitPostService.EXTRA_POST_TYPE_CROSSPOST);
            ContextCompat.startForegroundService(this, intent);

            return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if (isPosting) {
            promptAlertDialog(R.string.exit_when_submit, R.string.exit_when_submit_post_detail);
        } else {
            if (!binding.postTitleEditText.getText().toString().equals("")) {
                promptAlertDialog(R.string.discard, R.string.discard_detail);
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SELECTED_ACCOUNT_STATE, selectedAccount);
        outState.putString(SUBREDDIT_NAME_STATE, subredditName);
        outState.putString(SUBREDDIT_ICON_STATE, iconUrl);
        outState.putBoolean(SUBREDDIT_SELECTED_STATE, subredditSelected);
        outState.putBoolean(SUBREDDIT_IS_USER_STATE, subredditIsUser);
        outState.putBoolean(LOAD_SUBREDDIT_ICON_STATE, loadSubredditIconSuccessful);
        outState.putBoolean(IS_POSTING_STATE, isPosting);
        outState.putParcelable(FLAIR_STATE, flair);
        outState.putBoolean(IS_SPOILER_STATE, isSpoiler);
        outState.putBoolean(IS_NSFW_STATE, isNSFW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                subredditName = data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                iconUrl = data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_ICON_URL);
                subredditSelected = true;
                subredditIsUser = data.getExtras().getBoolean(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_IS_USER);

                binding.subredditNameTextView.setTextColor(primaryTextColor);
                binding.subredditNameTextView.setText(subredditName);
                displaySubredditIcon();

                binding.flairCustomTextView.setVisibility(View.VISIBLE);
                binding.flairCustomTextView.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.flairCustomTextView.setTextColor(primaryTextColor);
                binding.flairCustomTextView.setText(getString(R.string.flair));
                flair = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void flairSelected(Flair flair) {
        this.flair = flair;
        binding.flairCustomTextView.setText(flair.getText());
        binding.flairCustomTextView.setBackgroundColor(flairBackgroundColor);
        binding.flairCustomTextView.setBorderColor(flairBackgroundColor);
        binding.flairCustomTextView.setTextColor(flairTextColor);
    }

    @Override
    public void onAccountSelected(Account account) {
        if (account != null) {
            selectedAccount = account;

            mGlide.load(selectedAccount.getProfileImageUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(binding.accountIconGifImageView);

            binding.accountNameTextView.setText(selectedAccount.getAccountName());
        }
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }

    @Subscribe
    public void onSubmitCrosspostEvent(SubmitCrosspostEvent submitCrosspostEvent) {
        isPosting = false;
        mPostingSnackbar.dismiss();
        if (submitCrosspostEvent.postSuccess) {
            Intent intent = new Intent(this, ViewPostDetailActivity.class);
            intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, submitCrosspostEvent.post);
            startActivity(intent);
            finish();
        } else {
            mMenu.findItem(R.id.action_send_submit_crosspost_activity).setEnabled(true);
            mMenu.findItem(R.id.action_send_submit_crosspost_activity).getIcon().setAlpha(255);
            if (submitCrosspostEvent.errorMessage == null || submitCrosspostEvent.errorMessage.equals("")) {
                Snackbar.make(binding.coordinatorLayout, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(binding.coordinatorLayout, submitCrosspostEvent.errorMessage.substring(0, 1).toUpperCase()
                        + submitCrosspostEvent.errorMessage.substring(1), Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}