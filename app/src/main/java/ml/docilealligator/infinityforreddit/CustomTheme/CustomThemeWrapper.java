package ml.docilealligator.infinityforreddit.CustomTheme;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils;

import static ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils.AMOLED;
import static ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils.DARK;

public class CustomThemeWrapper {
    private SharedPreferences lightThemeSharedPreferences;
    private SharedPreferences darkThemeSharedPreferences;
    private SharedPreferences amoledThemeSharedPreferences;
    private int themeType;

    public CustomThemeWrapper(SharedPreferences lightThemeSharedPreferences,
                              SharedPreferences darkThemeSharedPreferences,
                              SharedPreferences amoledThemeSharedPreferences) {
        this.lightThemeSharedPreferences = lightThemeSharedPreferences;
        this.darkThemeSharedPreferences = darkThemeSharedPreferences;
        this.amoledThemeSharedPreferences = amoledThemeSharedPreferences;
    }

    private SharedPreferences getThemeSharedPreferences() {
        switch (themeType) {
            case DARK:
                return darkThemeSharedPreferences;
            case AMOLED:
                return amoledThemeSharedPreferences;
            default:
                return lightThemeSharedPreferences;
        }
    }

    private int getDefaultColor(String normalHex, String darkHex, String amoledDarkHex) {
        switch (themeType) {
            case DARK:
                return Color.parseColor(darkHex);
            case AMOLED:
                return Color.parseColor(amoledDarkHex);
            default:
                return Color.parseColor(normalHex);
        }
    }

    public void setThemeType(int themeType) {
        this.themeType = themeType;
    }

    public int getColorPrimary() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.COLOR_PRIMARY,
                getDefaultColor("#1565C0", "#242424", "#000000"));
    }

    public int getColorPrimaryDark() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.COLOR_PRIMARY_DARK,
                getDefaultColor("#0D47A1", "#121212", "#000000"));
    }

    public int getColorAccent() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.COLOR_ACCENT,
                getDefaultColor("#FF4081", "#FF4081", "#FF4081"));
    }

    public int getColorPrimaryLightTheme() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.COLOR_PRIMARY_LIGHT_THEME,
                getDefaultColor("#1565C0", "#1565C0", "#1565C0"));
    }

    public int getPrimaryTextColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.PRIMARY_TEXT_COLOR,
                getDefaultColor("#000000", "#FFFFFF", "#FFFFFF"));
    }

    public int getSecondaryTextColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.SECONDARY_TEXT_COLOR,
                getDefaultColor("#8A000000", "#B3FFFFFF", "#B3FFFFFF"));
    }

    public int getPostTitleColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.POST_TITLE_COLOR,
                getDefaultColor("#000000", "#FFFFFF", "#FFFFFF"));
    }

    public int getPostContentColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.POST_CONTENT_COLOR,
                getDefaultColor("#8A000000", "#B3FFFFFF", "#B3FFFFFF"));
    }

    public int getCommentColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.COMMENT_COLOR,
                getDefaultColor("#000000", "#FFFFFF", "#FFFFFF"));
    }

    public int getButtonTextColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.BUTTON_TEXT_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getBackgroundColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.BACKGROUND_COLOR,
                getDefaultColor("#FFFFFF", "#121212", "#000000"));
    }

    public int getCardViewBackgroundColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.CARD_VIEW_BACKGROUND_COLOR,
                getDefaultColor("#FFFFFF", "#242424", "#000000"));
    }

    public int getCommentBackgroundColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.COMMENT_BACKGROUND_COLOR,
                getDefaultColor("#FFFFFF", "#242424", "#000000"));
    }

    public int getBottomAppBarBackgroundColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.BOTTOM_APP_BAR_BACKGROUND_COLOR,
                getDefaultColor("#FFFFFF", "#121212", "#000000"));
    }

    public int getPrimaryIconColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.PRIMARY_ICON_COLOR,
                getDefaultColor("#000000", "#FFFFFF", "#FFFFFF"));
    }

    public int getPostIconAndInfoColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.POST_ICON_AND_INFO_COLOR,
                getDefaultColor("#8A000000", "#B3FFFFFF", "#B3FFFFFF"));
    }

    public int getCommentIconAndInfoColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.COMMENT_ICON_AND_INFO_COLOR,
                getDefaultColor("#8A000000", "#B3FFFFFF", "#B3FFFFFF"));
    }

    public int getToolbarPrimaryTextAndIconColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.TOOLBAR_PRIMARY_TEXT_AND_ICON_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getToolbarSecondaryTextColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.TOOLBAR_SECONDARY_TEXT_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getCircularProgressBarBackground() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.CIRCULAR_PROGRESS_BAR_BACKGROUND,
                getDefaultColor("#FFFFFF", "#242424", "#000000"));
    }

    public int getTabLayoutWithExpandedCollapsingToolbarTabBackground() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_EXPANDED_COLLAPSING_TOOLBAR_TAB_BACKGROUND,
                getDefaultColor("#FFFFFF", "#242424", "#000000"));
    }

    public int getTabLayoutWithExpandedCollapsingToolbarTextColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_EXPANDED_COLLAPSING_TOOLBAR_TEXT_COLOR,
                getDefaultColor("#1565C0", "#FFFFFF", "#FFFFFF"));
    }

    public int getTabLayoutWithExpandedCollapsingToolbarTabIndicator() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_EXPANDED_COLLAPSING_TOOLBAR_TAB_INDICATOR,
                getDefaultColor("#1565C0", "#FFFFFF", "#FFFFFF"));
    }

    public int getTabLayoutWithCollapsedCollapsingToolbarTabBackground() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_COLLAPSED_COLLAPSING_TOOLBAR_TAB_BACKGROUND,
                getDefaultColor("#1565C0", "#242424", "#000000"));
    }

    public int getTabLayoutWithCollapsedCollapsingToolbarTextColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_COLLAPSED_COLLAPSING_TOOLBAR_TEXT_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getTabLayoutWithCollapsedCollapsingToolbarTabIndicator() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_COLLAPSED_COLLAPSING_TOOLBAR_TAB_INDICATOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getUpvoted() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.UPVOTED,
                getDefaultColor("#E91E63", "#E91E63", "#E91E63"));
    }

    public int getDownvoted() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.DOWNVOTED,
                getDefaultColor("#007DDE", "#007DDE", "#007DDE"));
    }

    public int getPostTypeBackgroundColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.POST_TYPE_BACKGROUND_COLOR,
                getDefaultColor("#0D47A1", "#1565C0", "#1565C0"));
    }

    public int getPostTypeTextColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.POST_TYPE_TEXT_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getSpoilerBackgroundColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.SPOILER_BACKGROUND_COLOR,
                getDefaultColor("#EE02EB", "#EE02EB", "#EE02EB"));
    }

    public int getSpoilerTextColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.SPOILER_TEXT_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getNsfwBackgroundColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.NSFW_BACKGROUND_COLOR,
                getDefaultColor("#FF4081", "#FF4081", "#FF4081"));
    }

    public int getNsfwTextColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.NSFW_TEXT_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getFlairBackgroundColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.FLAIR_BACKGROUND_COLOR,
                getDefaultColor("#00AA8C", "#00AA8C", "#00AA8C"));
    }

    public int getFlairTextColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.FLAIR_TEXT_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getArchivedIconTint() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.ARCHIVED_ICON_TINT,
                getDefaultColor("#B4009F", "#B4009F", "#B4009F"));
    }

    public int getLockedIconTint() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.LOCKED_ICON_TINT,
                getDefaultColor("#EE7302", "#EE7302", "#EE7302"));
    }

    public int getCrosspostIconTint() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.CROSSPOST_ICON_TINT,
                getDefaultColor("#FF4081", "#FF4081", "#FF4081"));
    }

    public int getStickiedPostIconTint() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.STICKIED_POST_ICON_TINT,
                getDefaultColor("#0D47A1", "#1565C0", "#1565C0"));
    }

    public int getSubscribed() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.SUBSCRIBED,
                getDefaultColor("#FF4081", "#FF4081", "#FF4081"));
    }

    public int getUnsubscribed() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.UNSUBSCRIBED,
                getDefaultColor("#0D47A1", "#1565C0", "#1565C0"));
    }

    public int getUsername() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.USERNAME,
                getDefaultColor("#0D47A1", "#1E88E5", "#1E88E5"));
    }

    public int getSubreddit() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.SUBREDDIT,
                getDefaultColor("#E91E63", "#E91E63", "#E91E63"));
    }

    public int getAuthorFlairTextColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.AUTHOR_FLAIR_TEXT_COLOR,
                getDefaultColor("#EE02C4", "#EE02C4", "#EE02C4"));
    }

    public int getSubmitter() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.SUBMITTER,
                getDefaultColor("#EE8A02", "#EE8A02", "#EE8A02"));
    }

    public int getModerator() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.MODERATOR,
                getDefaultColor("#00BA81", "#00BA81", "#00BA81"));
    }

    public int getSingleCommentThreadBackgroundColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.SINGLE_COMMENT_THREAD_BACKGROUND_COLOR,
                getDefaultColor("#B3E5F9", "#123E77", "#123E77"));
    }

    public int getUnreadMessageBackgroundColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.UNREAD_MESSAGE_BACKGROUND_COLOR,
                getDefaultColor("#B3E5F9", "#123E77", "#123E77"));
    }

    public int getDividerColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.DIVIDER_COLOR,
                getDefaultColor("#E0E0E0", "#69666C", "#69666C"));
    }

    public int getNoPreviewLinkBackgroundColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.NO_PREVIEW_LINK_BACKGROUND_COLOR,
                getDefaultColor("#E0E0E0", "#424242", "#424242"));
    }

    public int getVoteAndReplyUnavailableButtonColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.VOTE_AND_REPLY_UNAVAILABLE_BUTTON_COLOR,
                getDefaultColor("#F0F0F0", "#3C3C3C", "#3C3C3C"));
    }

    public int getCommentVerticalBarColor1() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_1,
                getDefaultColor("#1565C0", "#1565C0", "#1565C0"));
    }

    public int getCommentVerticalBarColor2() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_2,
                getDefaultColor("#EE02BE", "#C300B3", "#C300B3"));
    }

    public int getCommentVerticalBarColor3() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_3,
                getDefaultColor("#02DFEE", "#00B8DA", "#00B8DA"));
    }

    public int getCommentVerticalBarColor4() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_4,
                getDefaultColor("#EED502", "#EDCA00", "#EDCA00"));
    }

    public int getCommentVerticalBarColor5() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_5,
                getDefaultColor("#EE0220", "#EE0219", "#EE0219"));
    }

    public int getCommentVerticalBarColor6() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_6,
                getDefaultColor("#02EE6E", "#00B925", "#00B925"));
    }

    public int getCommentVerticalBarColor7() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.COMMENT_VERTICAL_BAR_COLOR_7,
                getDefaultColor("#EE4602", "#EE4602", "#EE4602"));
    }

    public int getFABIconColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.FAB_ICON_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getChipTextColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.CHIP_TEXT_COLOR,
                getDefaultColor("#FFFFFF", "#FFFFFF", "#FFFFFF"));
    }

    public int getNavBarColor() {
        return getThemeSharedPreferences().getInt(CustomThemeSharedPreferencesUtils.NAV_BAR_COLOR,
                getDefaultColor("#FFFFFF", "#121212", "#000000"));
    }

    public boolean isLightStatusBar() {
        return getThemeSharedPreferences().getBoolean(CustomThemeSharedPreferencesUtils.LIGHT_STATUS_BAR, false);
    }

    public boolean isLightNavBar() {
        return getThemeSharedPreferences().getBoolean(CustomThemeSharedPreferencesUtils.LIGHT_NAV_BAR,
                themeType == CustomThemeSharedPreferencesUtils.LIGHT);
    }

    public boolean isChangeStatusBarIconColorAfterToolbarCollapsedInImmersiveInterface() {
        return getThemeSharedPreferences().getBoolean(
                CustomThemeSharedPreferencesUtils.CHANGE_STATUS_BAR_ICON_COLOR_AFTER_TOOLBAR_COLLAPSED_IN_IMMERSIVE_INTERFACE,
                themeType == CustomThemeSharedPreferencesUtils.LIGHT);
    }

    public static ArrayList<CustomTheme> getPredifinedThemes(Context context) {
        ArrayList<CustomTheme> customThemes = new ArrayList<>();
        customThemes.add(getIndigo(context));
        customThemes.add(getIndigoDark(context));
        customThemes.add(getIndigoAmoled(context));
        return customThemes;
    }

    public static CustomTheme getIndigo(Context context) {
        CustomTheme customTheme = new CustomTheme(context.getString(R.string.theme_name_indigo));
        customTheme.isLightTheme = true;
        customTheme.isDarkTheme = false;
        customTheme.isAmoledTheme = false;
        customTheme.colorPrimary = Color.parseColor("#1565C0");
        customTheme.colorPrimaryDark = Color.parseColor("#0D47A1");
        customTheme.colorAccent = Color.parseColor("#FF4081");
        customTheme.colorPrimaryLightTheme = Color.parseColor("#1565C0");
        customTheme.primaryTextColor = Color.parseColor("#000000");
        customTheme.secondaryTextColor = Color.parseColor("#8A000000");
        customTheme.postTitleColor = Color.parseColor("#000000");
        customTheme.postContentColor = Color.parseColor("#8A000000");
        customTheme.commentColor = Color.parseColor("#000000");
        customTheme.buttonTextColor = Color.parseColor("#FFFFFF");
        customTheme.backgroundColor = Color.parseColor("#FFFFFF");
        customTheme.cardViewBackgroundColor = Color.parseColor("#FFFFFF");
        customTheme.commentBackgroundColor = Color.parseColor("#FFFFFF");
        customTheme.bottomAppBarBackgroundColor = Color.parseColor("#FFFFFF");
        customTheme.primaryIconColor = Color.parseColor("#000000");
        customTheme.postIconAndInfoColor = Color.parseColor("#8A000000");
        customTheme.commentIconAndInfoColor = Color.parseColor("#8A000000");
        customTheme.toolbarPrimaryTextAndIconColor = Color.parseColor("#FFFFFF");
        customTheme.toolbarSecondaryTextColor = Color.parseColor("#FFFFFF");
        customTheme.circularProgressBarBackground = Color.parseColor("#FFFFFF");
        customTheme.tabLayoutWithExpandedCollapsingToolbarTabBackground = Color.parseColor("#FFFFFF");
        customTheme.tabLayoutWithExpandedCollapsingToolbarTextColor = Color.parseColor("#1565C0");
        customTheme.tabLayoutWithExpandedCollapsingToolbarTabIndicator = Color.parseColor("#1565C0");
        customTheme.tabLayoutWithCollapsedCollapsingToolbarTabBackground = Color.parseColor("#1565C0");
        customTheme.tabLayoutWithCollapsedCollapsingToolbarTextColor = Color.parseColor("#FFFFFF");
        customTheme.tabLayoutWithCollapsedCollapsingToolbarTabIndicator = Color.parseColor("#FFFFFF");
        customTheme.upvoted = Color.parseColor("#E91E63");
        customTheme.downvoted = Color.parseColor("#007DDE");
        customTheme.postTypeBackgroundColor = Color.parseColor("#0D47A1");
        customTheme.postTypeTextColor = Color.parseColor("#FFFFFF");
        customTheme.spoilerBackgroundColor = Color.parseColor("#EE02EB");
        customTheme.spoilerTextColor = Color.parseColor("#FFFFFF");
        customTheme.nsfwBackgroundColor = Color.parseColor("#FF4081");
        customTheme.nsfwTextColor = Color.parseColor("#FFFFFF");
        customTheme.flairBackgroundColor = Color.parseColor("#00AA8C");
        customTheme.flairTextColor = Color.parseColor("#FFFFFF");
        customTheme.archivedTint = Color.parseColor("#B4009F");
        customTheme.lockedIconTint = Color.parseColor("#EE7302");
        customTheme.crosspostIconTint = Color.parseColor("#FF4081");
        customTheme.stickiedPostIconTint = Color.parseColor("#0D47A1");
        customTheme.subscribed = Color.parseColor("#FF4081");
        customTheme.unsubscribed = Color.parseColor("#0D47A1");
        customTheme.username = Color.parseColor("#0D47A1");
        customTheme.subreddit = Color.parseColor("#E91E63");
        customTheme.authorFlairTextColor = Color.parseColor("#EE02C4");
        customTheme.submitter = Color.parseColor("#EE8A02");
        customTheme.moderator = Color.parseColor("#00BA81");
        customTheme.singleCommentThreadBackgroundColor = Color.parseColor("#B3E5F9");
        customTheme.unreadMessageBackgroundColor = Color.parseColor("#B3E5F9");
        customTheme.dividerColor = Color.parseColor("#E0E0E0");
        customTheme.noPreviewLinkBackgroundColor = Color.parseColor("#E0E0E0");
        customTheme.voteAndReplyUnavailableButtonColor = Color.parseColor("#F0F0F0");
        customTheme.commentVerticalBarColor1 = Color.parseColor("#1565C0");
        customTheme.commentVerticalBarColor2 = Color.parseColor("#EE02BE");
        customTheme.commentVerticalBarColor3 = Color.parseColor("#02DFEE");
        customTheme.commentVerticalBarColor4 = Color.parseColor("#EED502");
        customTheme.commentVerticalBarColor5 = Color.parseColor("#EE0220");
        customTheme.commentVerticalBarColor6 = Color.parseColor("#02EE6E");
        customTheme.commentVerticalBarColor7 = Color.parseColor("#EE4602");
        customTheme.fabIconColor = Color.parseColor("#FFFFFF");
        customTheme.chipTextColor = Color.parseColor("#FFFFFF");
        customTheme.navBarColor = Color.parseColor("#FFFFFF");
        customTheme.isLightStatusBar = false;
        customTheme.isLightNavBar = true;
        customTheme.isChangeStatusBarIconColorAfterToolbarCollapsedInImmersiveInterface = true;

        return customTheme;
    }

    public static CustomTheme getIndigoDark(Context context) {
        CustomTheme customTheme = new CustomTheme(context.getString(R.string.theme_name_indigo_dark));
        customTheme.isLightTheme = false;
        customTheme.isDarkTheme = true;
        customTheme.isAmoledTheme = false;
        customTheme.colorPrimary = Color.parseColor("#242424");
        customTheme.colorPrimaryDark = Color.parseColor("#121212");
        customTheme.colorAccent = Color.parseColor("#FF4081");
        customTheme.colorPrimaryLightTheme = Color.parseColor("#1565C0");
        customTheme.primaryTextColor = Color.parseColor("#FFFFFF");
        customTheme.secondaryTextColor = Color.parseColor("#B3FFFFFF");
        customTheme.postTitleColor = Color.parseColor("#FFFFFF");
        customTheme.postContentColor = Color.parseColor("#B3FFFFFF");
        customTheme.commentColor = Color.parseColor("#FFFFFF");
        customTheme.buttonTextColor = Color.parseColor("#FFFFFF");
        customTheme.backgroundColor = Color.parseColor("#121212");
        customTheme.cardViewBackgroundColor = Color.parseColor("#242424");
        customTheme.commentBackgroundColor = Color.parseColor("#242424");
        customTheme.bottomAppBarBackgroundColor = Color.parseColor("#121212");
        customTheme.primaryIconColor = Color.parseColor("#FFFFFF");
        customTheme.postIconAndInfoColor = Color.parseColor("#B3FFFFFF");
        customTheme.commentIconAndInfoColor = Color.parseColor("#B3FFFFFF");
        customTheme.toolbarPrimaryTextAndIconColor = Color.parseColor("#FFFFFF");
        customTheme.toolbarSecondaryTextColor = Color.parseColor("#FFFFFF");
        customTheme.circularProgressBarBackground = Color.parseColor("#242424");
        customTheme.tabLayoutWithExpandedCollapsingToolbarTabBackground = Color.parseColor("#242424");
        customTheme.tabLayoutWithExpandedCollapsingToolbarTextColor = Color.parseColor("#FFFFFF");
        customTheme.tabLayoutWithExpandedCollapsingToolbarTabIndicator = Color.parseColor("#FFFFFF");
        customTheme.tabLayoutWithCollapsedCollapsingToolbarTabBackground = Color.parseColor("#242424");
        customTheme.tabLayoutWithCollapsedCollapsingToolbarTextColor = Color.parseColor("#FFFFFF");
        customTheme.tabLayoutWithCollapsedCollapsingToolbarTabIndicator = Color.parseColor("#FFFFFF");
        customTheme.upvoted = Color.parseColor("#E91E63");
        customTheme.downvoted = Color.parseColor("#007DDE");
        customTheme.postTypeBackgroundColor = Color.parseColor("#1565C0");
        customTheme.postTypeTextColor = Color.parseColor("#FFFFFF");
        customTheme.spoilerBackgroundColor = Color.parseColor("#EE02EB");
        customTheme.spoilerTextColor = Color.parseColor("#FFFFFF");
        customTheme.nsfwBackgroundColor = Color.parseColor("#FF4081");
        customTheme.nsfwTextColor = Color.parseColor("#FFFFFF");
        customTheme.flairBackgroundColor = Color.parseColor("#00AA8C");
        customTheme.flairTextColor = Color.parseColor("#FFFFFF");
        customTheme.archivedTint = Color.parseColor("#B4009F");
        customTheme.lockedIconTint = Color.parseColor("#EE7302");
        customTheme.crosspostIconTint = Color.parseColor("#FF4081");
        customTheme.stickiedPostIconTint = Color.parseColor("#1565C0");
        customTheme.subscribed = Color.parseColor("#FF4081");
        customTheme.unsubscribed = Color.parseColor("#1565C0");
        customTheme.username = Color.parseColor("#1E88E5");
        customTheme.subreddit = Color.parseColor("#E91E63");
        customTheme.authorFlairTextColor = Color.parseColor("#EE02C4");
        customTheme.submitter = Color.parseColor("#EE8A02");
        customTheme.moderator = Color.parseColor("#00BA81");
        customTheme.singleCommentThreadBackgroundColor = Color.parseColor("#123E77");
        customTheme.unreadMessageBackgroundColor = Color.parseColor("#123E77");
        customTheme.dividerColor = Color.parseColor("#69666C");
        customTheme.noPreviewLinkBackgroundColor = Color.parseColor("#424242");
        customTheme.voteAndReplyUnavailableButtonColor = Color.parseColor("#3C3C3C");
        customTheme.commentVerticalBarColor1 = Color.parseColor("#1565C0");
        customTheme.commentVerticalBarColor2 = Color.parseColor("#C300B3");
        customTheme.commentVerticalBarColor3 = Color.parseColor("#00B8DA");
        customTheme.commentVerticalBarColor4 = Color.parseColor("#EDCA00");
        customTheme.commentVerticalBarColor5 = Color.parseColor("#EE0219");
        customTheme.commentVerticalBarColor6 = Color.parseColor("#00B925");
        customTheme.commentVerticalBarColor7 = Color.parseColor("#EE4602");
        customTheme.fabIconColor = Color.parseColor("#FFFFFF");
        customTheme.chipTextColor = Color.parseColor("#FFFFFF");
        customTheme.navBarColor = Color.parseColor("#121212");
        customTheme.isLightStatusBar = false;
        customTheme.isLightNavBar = false;
        customTheme.isChangeStatusBarIconColorAfterToolbarCollapsedInImmersiveInterface = false;

        return customTheme;
    }

    public static CustomTheme getIndigoAmoled(Context context) {
        CustomTheme customTheme = new CustomTheme(context.getString(R.string.theme_name_indigo_dark));
        customTheme.isLightTheme = false;
        customTheme.isDarkTheme = true;
        customTheme.isAmoledTheme = false;
        customTheme.colorPrimary = Color.parseColor("#000000");
        customTheme.colorPrimaryDark = Color.parseColor("#000000");
        customTheme.colorAccent = Color.parseColor("#FF4081");
        customTheme.colorPrimaryLightTheme = Color.parseColor("#1565C0");
        customTheme.primaryTextColor = Color.parseColor("#FFFFFF");
        customTheme.secondaryTextColor = Color.parseColor("#B3FFFFFF");
        customTheme.postTitleColor = Color.parseColor("#FFFFFF");
        customTheme.postContentColor = Color.parseColor("#B3FFFFFF");
        customTheme.commentColor = Color.parseColor("#FFFFFF");
        customTheme.buttonTextColor = Color.parseColor("#FFFFFF");
        customTheme.backgroundColor = Color.parseColor("#000000");
        customTheme.cardViewBackgroundColor = Color.parseColor("#000000");
        customTheme.commentBackgroundColor = Color.parseColor("#000000");
        customTheme.bottomAppBarBackgroundColor = Color.parseColor("#000000");
        customTheme.primaryIconColor = Color.parseColor("#FFFFFF");
        customTheme.postIconAndInfoColor = Color.parseColor("#B3FFFFFF");
        customTheme.commentIconAndInfoColor = Color.parseColor("#B3FFFFFF");
        customTheme.toolbarPrimaryTextAndIconColor = Color.parseColor("#FFFFFF");
        customTheme.toolbarSecondaryTextColor = Color.parseColor("#FFFFFF");
        customTheme.circularProgressBarBackground = Color.parseColor("#000000");
        customTheme.tabLayoutWithExpandedCollapsingToolbarTabBackground = Color.parseColor("#000000");
        customTheme.tabLayoutWithExpandedCollapsingToolbarTextColor = Color.parseColor("#FFFFFF");
        customTheme.tabLayoutWithExpandedCollapsingToolbarTabIndicator = Color.parseColor("#FFFFFF");
        customTheme.tabLayoutWithCollapsedCollapsingToolbarTabBackground = Color.parseColor("#000000");
        customTheme.tabLayoutWithCollapsedCollapsingToolbarTextColor = Color.parseColor("#FFFFFF");
        customTheme.tabLayoutWithCollapsedCollapsingToolbarTabIndicator = Color.parseColor("#FFFFFF");
        customTheme.upvoted = Color.parseColor("#E91E63");
        customTheme.downvoted = Color.parseColor("#007DDE");
        customTheme.postTypeBackgroundColor = Color.parseColor("#1565C0");
        customTheme.postTypeTextColor = Color.parseColor("#FFFFFF");
        customTheme.spoilerBackgroundColor = Color.parseColor("#EE02EB");
        customTheme.spoilerTextColor = Color.parseColor("#FFFFFF");
        customTheme.nsfwBackgroundColor = Color.parseColor("#FF4081");
        customTheme.nsfwTextColor = Color.parseColor("#FFFFFF");
        customTheme.flairBackgroundColor = Color.parseColor("#00AA8C");
        customTheme.flairTextColor = Color.parseColor("#FFFFFF");
        customTheme.archivedTint = Color.parseColor("#B4009F");
        customTheme.lockedIconTint = Color.parseColor("#EE7302");
        customTheme.crosspostIconTint = Color.parseColor("#FF4081");
        customTheme.stickiedPostIconTint = Color.parseColor("#1565C0");
        customTheme.subscribed = Color.parseColor("#FF4081");
        customTheme.unsubscribed = Color.parseColor("#1565C0");
        customTheme.username = Color.parseColor("#1E88E5");
        customTheme.subreddit = Color.parseColor("#E91E63");
        customTheme.authorFlairTextColor = Color.parseColor("#EE02C4");
        customTheme.submitter = Color.parseColor("#EE8A02");
        customTheme.moderator = Color.parseColor("#00BA81");
        customTheme.singleCommentThreadBackgroundColor = Color.parseColor("#123E77");
        customTheme.unreadMessageBackgroundColor = Color.parseColor("#123E77");
        customTheme.dividerColor = Color.parseColor("#69666C");
        customTheme.noPreviewLinkBackgroundColor = Color.parseColor("#424242");
        customTheme.voteAndReplyUnavailableButtonColor = Color.parseColor("#3C3C3C");
        customTheme.commentVerticalBarColor1 = Color.parseColor("#1565C0");
        customTheme.commentVerticalBarColor2 = Color.parseColor("#C300B3");
        customTheme.commentVerticalBarColor3 = Color.parseColor("#00B8DA");
        customTheme.commentVerticalBarColor4 = Color.parseColor("#EDCA00");
        customTheme.commentVerticalBarColor5 = Color.parseColor("#EE0219");
        customTheme.commentVerticalBarColor6 = Color.parseColor("#00B925");
        customTheme.commentVerticalBarColor7 = Color.parseColor("#EE4602");
        customTheme.fabIconColor = Color.parseColor("#FFFFFF");
        customTheme.chipTextColor = Color.parseColor("#FFFFFF");
        customTheme.navBarColor = Color.parseColor("#000000");
        customTheme.isLightStatusBar = false;
        customTheme.isLightNavBar = false;
        customTheme.isChangeStatusBarIconColorAfterToolbarCollapsedInImmersiveInterface = false;

        return customTheme;
    }
}