package ml.docilealligator.infinityforreddit.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.Spannable;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.text.HtmlCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.UploadedImage;
import retrofit2.Retrofit;

public final class Utils {
    public static final int NETWORK_TYPE_OTHER = -1;
    public static final int NETWORK_TYPE_WIFI = 0;
    public static final int NETWORK_TYPE_CELLULAR = 1;
    private static final long SECOND_MILLIS = 1000;
    private static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final long MONTH_MILLIS = 30 * DAY_MILLIS;
    private static final long YEAR_MILLIS = 12 * MONTH_MILLIS;
    private static final Pattern[] REGEX_PATTERNS = {
            Pattern.compile("((?<=[\\s])|^)/[rRuU]/[\\w-]+/{0,1}"),
            Pattern.compile("((?<=[\\s])|^)[rRuU]/[\\w-]+/{0,1}"),
            Pattern.compile("\\^{2,}"),
            Pattern.compile("!\\[gif]\\(giphy\\|\\w+\\)"),
            Pattern.compile("!\\[gif]\\(giphy\\|\\w+\\|downsized\\)"),
            Pattern.compile("!\\[gif]\\(emote\\|\\w+\\|\\w+\\)"),
    };


    public static String modifyMarkdown(String markdown) {
        String regexed = REGEX_PATTERNS[0].matcher(markdown).replaceAll("[$0](https://www.reddit.com$0)");
        regexed = REGEX_PATTERNS[1].matcher(regexed).replaceAll("[$0](https://www.reddit.com/$0)");
        regexed = REGEX_PATTERNS[2].matcher(regexed).replaceAll("^");

        return regexed;
    }

    public static String inlineImages(String body, JSONObject mediaMetadata) {
        String value = body;
        if(mediaMetadata.length() == 0){
            return body;
        }
        JSONArray names = mediaMetadata.names();
        try{
            for(int i=0; i<names.length(); i++){
                String id = names.getString(i);
                JSONObject metadata = mediaMetadata.getJSONObject(id);
                String status = metadata.getString("status");
                if(!status.equals("valid")){
                    continue;
                }

                String mimeType = metadata.getString("m");
                if(!(mimeType.equals("image/jpeg") || mimeType.equals("image/png"))){
                    continue;
                }

                JSONArray previews = metadata.getJSONArray("p");
                int length = previews.length();
                int index = length/2;
                JSONObject largestPreview = previews.getJSONObject(index);
                String url = largestPreview.getString("u");
                value = value.replace(id, url);
            }
        }catch (JSONException e){

        }
        return value;
    }

    public static String parseInlineEmotesAndGifs(String markdown, JSONObject mediaMetadataObject, JSONObject expressionAssetData) throws JSONException {
        JSONArray mediaMetadataNames = mediaMetadataObject.names();
        if (mediaMetadataNames != null) {
            for (int i = 0; i < mediaMetadataNames.length(); i++) {
                if (!mediaMetadataNames.isNull(i)) {
                    String mediaMetadataKey = mediaMetadataNames.getString(i);
                    if (mediaMetadataObject.isNull(mediaMetadataKey)) {
                        continue;
                    }
                    JSONObject item = mediaMetadataObject.getJSONObject(mediaMetadataKey);
                    if (item.isNull(JSONUtils.STATUS_KEY)
                            || !item.getString(JSONUtils.STATUS_KEY).equals("valid")
                            || item.isNull(JSONUtils.ID_KEY)
                            || item.isNull(JSONUtils.T_KEY)
                            || item.isNull(JSONUtils.S_KEY)) {
                        continue;
                    }
                    String mime_type = item.getString("m");
                    String type = item.getString("t");
                    String emote_id = item.getString(JSONUtils.ID_KEY);
                    JSONObject s_key = item.getJSONObject(JSONUtils.S_KEY);
                    String emote_url = "";


                    if(mime_type.equals("image/gif")){
                        emote_url = s_key.getString("gif");
                    }else if(type.equals("emoji")){
                        emote_url = s_key.getString("u");
                    }else{
                        continue;
                    }
                    markdown = markdown.replace(emote_id, emote_url);
                }
            }
        }

        if(expressionAssetData == null){
            return markdown;
        }

        String front = "";
        String back = "";
        String container = "<div>%s%s%s</div>";
        int res = 0;
        String tmpl = "<div style=\"width: 150px; height: 150px\"><img style=\"position: absolute; top: 0\"src=\"%s\"/><div style=\"width: 100%%;height: 100%%;position: relative;display: grid;overflow: hidden;\"><div style=\"width: 150px;height: 150px;position: absolute;top: 25;display: flex;justify-content: center;\"><img src=\"%s\"/></div></div><img style=\"position: absolute; top: 0\"src=\"%s\"/></div>";

        JSONArray expressionAssetDataNames = expressionAssetData.names();
        if(expressionAssetDataNames != null){
            for (int i = 0; i < expressionAssetDataNames.length(); i++){
                if(!expressionAssetDataNames.isNull(i)){
                    String expressionAssetKey = expressionAssetDataNames.getString(i);
                    if (expressionAssetData.isNull(expressionAssetKey)) {
                        continue;
                    }
                    JSONObject item = expressionAssetData.getJSONObject(expressionAssetKey);
                    JSONArray expression = item.getJSONArray("expression");
                    res = expression.getJSONObject(0).getJSONObject(JSONUtils.S_KEY).getInt(JSONUtils.X_KEY);
                    for(int j = 0; j< expression.length(); j++){
                        if(expression.getJSONObject(j).getJSONObject(JSONUtils.S_KEY).getInt(JSONUtils.X_KEY) == res){
                            switch (expression.getJSONObject(j).getString("l")){
                                case "BACK":
                                    back = expression.getJSONObject(j).getJSONObject(JSONUtils.S_KEY).getString(JSONUtils.U_KEY);
                                    break;
                                case "FRONT":
                                    front = expression.getJSONObject(j).getJSONObject(JSONUtils.S_KEY).getString(JSONUtils.U_KEY);
                                    break;
                            }

                        }
                    }
                    String snoo = String.format("<img src=\"%s\"/>", item.getJSONObject("avatar").getJSONObject(JSONUtils.S_KEY).getString(JSONUtils.U_KEY));
                    container = String.format(tmpl, back, snoo, front);

                    markdown = markdown.replace(String.format("![img](%s)",expressionAssetKey), "*This comment contains a Collectible Expression which are not available on old Reddit.*");
                }
            }
        }
        return markdown;
    }

    public static String trimTrailingWhitespace(String source) {

        if (source == null) {
            return "";
        }

        int i = source.length();

        // loop back to the first non-whitespace character
        do {
            i--;
        } while (i >= 0 && Character.isWhitespace(source.charAt(i)));

        return source.substring(0, i + 1);
    }

    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if (source == null) {
            return "";
        }

        int i = source.length();

        // loop back to the first non-whitespace character
        do {
            i--;
        } while (i >= 0 && Character.isWhitespace(source.charAt(i)));

        return source.subSequence(0, i + 1);
    }

    public static String getFormattedTime(Locale locale, long time, String pattern) {
        Calendar postTimeCalendar = Calendar.getInstance();
        postTimeCalendar.setTimeInMillis(time);
        return new SimpleDateFormat(pattern, locale).format(postTimeCalendar.getTime());
    }

    public static String getElapsedTime(Context context, long time) {
        long now = System.currentTimeMillis();
        long diff = now - time;

        if (diff < MINUTE_MILLIS) {
            return context.getString(R.string.elapsed_time_just_now);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return context.getString(R.string.elapsed_time_a_minute_ago);
        } else if (diff < 50 * MINUTE_MILLIS) {
            return context.getString(R.string.elapsed_time_minutes_ago, diff / MINUTE_MILLIS);
        } else if (diff < 120 * MINUTE_MILLIS) {
            return context.getString(R.string.elapsed_time_an_hour_ago);
        } else if (diff < 24 * HOUR_MILLIS) {
            return context.getString(R.string.elapsed_time_hours_ago, diff / HOUR_MILLIS);
        } else if (diff < 48 * HOUR_MILLIS) {
            return context.getString(R.string.elapsed_time_yesterday);
        } else if (diff < MONTH_MILLIS) {
            return context.getString(R.string.elapsed_time_days_ago, diff / DAY_MILLIS);
        } else if (diff < 2 * MONTH_MILLIS) {
            return context.getString(R.string.elapsed_time_a_month_ago);
        } else if (diff < YEAR_MILLIS) {
            return context.getString(R.string.elapsed_time_months_ago, diff / MONTH_MILLIS);
        } else if (diff < 2 * YEAR_MILLIS) {
            return context.getString(R.string.elapsed_time_a_year_ago);
        } else {
            return context.getString(R.string.elapsed_time_years_ago, diff / YEAR_MILLIS);
        }
    }

    public static String getNVotes(boolean showAbsoluteNumberOfVotes, int votes) {
        if (showAbsoluteNumberOfVotes) {
            return Integer.toString(votes);
        } else {
            if (Math.abs(votes) < 1000) {
                return Integer.toString(votes);
            }
            return String.format(Locale.US, "%.1f", (float) votes / 1000) + "K";
        }
    }

    public static void setHTMLWithImageToTextView(TextView textView, String content, boolean enlargeImage) {
        GlideImageGetter glideImageGetter = new GlideImageGetter(textView, enlargeImage);
        Spannable html = (Spannable) HtmlCompat.fromHtml(
                content, HtmlCompat.FROM_HTML_MODE_LEGACY, glideImageGetter, null);

        textView.setText(html);
    }

    public static int getConnectedNetwork(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            Network nw = connMgr.getActiveNetwork();
            if (nw == null) return NETWORK_TYPE_OTHER;
            try {
                NetworkCapabilities actNw = connMgr.getNetworkCapabilities(nw);
                if (actNw != null) {
                    if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return NETWORK_TYPE_WIFI;
                    }
                    if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return NETWORK_TYPE_CELLULAR;
                    }
                }
            } catch (SecurityException ignore) {
            }
            return NETWORK_TYPE_OTHER;
        }

        return NETWORK_TYPE_OTHER;
    }

    public static boolean isConnectedToWifi(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            Network nw = connMgr.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities actNw = connMgr.getNetworkCapabilities(nw);
            return actNw != null && actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        }

        return false;
    }

    public static boolean isConnectedToCellularData(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            Network nw = connMgr.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities actNw = connMgr.getNetworkCapabilities(nw);
            return actNw != null && actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
        }

        return false;
    }

    public static void displaySortTypeInToolbar(SortType sortType, Toolbar toolbar) {
        if (sortType != null) {
            if (sortType.getTime() != null) {
                toolbar.setSubtitle(sortType.getType().fullName + ": " + sortType.getTime().fullName);
            } else {
                toolbar.setSubtitle(sortType.getType().fullName);
            }
        }
    }

    public static void showKeyboard(Context context, Handler handler, View view) {
        handler.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 300);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @Nullable
    public static Drawable getTintedDrawable(Context context, int drawableId, int color) {
        final Drawable drawable = AppCompatResources.getDrawable(context, drawableId);
        if (drawable != null) {
            drawable.setTint(color);
        }
        return drawable;
    }

    public static void uploadImageToReddit(Context context, Executor executor, Retrofit oauthRetrofit,
                                           Retrofit uploadMediaRetrofit, String accessToken, EditText editText,
                                           CoordinatorLayout coordinatorLayout, Uri imageUri,
                                           ArrayList<UploadedImage> uploadedImages) {
        Toast.makeText(context, R.string.uploading_image, Toast.LENGTH_SHORT).show();
        Handler handler = new Handler();
        executor.execute(() -> {
            try {
                Bitmap bitmap = Glide.with(context).asBitmap().load(imageUri).submit().get();
                String imageUrlOrError = UploadImageUtils.uploadImage(oauthRetrofit, uploadMediaRetrofit, accessToken, bitmap);
                handler.post(() -> {
                    if (imageUrlOrError != null && !imageUrlOrError.startsWith("Error: ")) {
                        String fileName = Utils.getFileName(context, imageUri);
                        if (fileName == null) {
                            fileName = imageUrlOrError;
                        }
                        uploadedImages.add(new UploadedImage(fileName, imageUrlOrError));

                        int start = Math.max(editText.getSelectionStart(), 0);
                        int end = Math.max(editText.getSelectionEnd(), 0);
                        editText.getText().replace(Math.min(start, end), Math.max(start, end),
                                "[" + fileName + "](" + imageUrlOrError + ")",
                                0, "[]()".length() + fileName.length() + imageUrlOrError.length());
                        Snackbar.make(coordinatorLayout, R.string.upload_image_success, Snackbar.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, R.string.upload_image_failed, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(context, R.string.get_image_bitmap_failed, Toast.LENGTH_LONG).show());
            } catch (XmlPullParserException | JSONException | IOException e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(context, R.string.error_processing_image, Toast.LENGTH_LONG).show());
            }
        });
    }

    @Nullable
    public static String getFileName(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver != null) {
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                cursor.moveToFirst();
                String fileName = cursor.getString(nameIndex);
                if (fileName != null && fileName.contains(".")) {
                    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                }
                return fileName;
            }
        }

        return null;
    }

    public static void setTitleToMenuItem(MenuItem item, String desiredTitle) {
        if (desiredTitle != null) {
            item.setTitle(desiredTitle);
        }
    }

    public static void setTitleToTab(TabLayout.Tab tab, String title) {
        tab.setText(title);
    }
}
