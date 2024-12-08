package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.paging.ListenableFuturePagingSource;
import androidx.paging.PagingState;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.apis.GqlAPI;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import okhttp3.RequestBody;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PostPagingSource extends ListenableFuturePagingSource<String, Post> {
    public static final int TYPE_FRONT_PAGE = 0;
    public static final int TYPE_SUBREDDIT = 1;
    public static final int TYPE_USER = 2;
    public static final int TYPE_SEARCH = 3;
    public static final int TYPE_MULTI_REDDIT = 4;
    public static final int TYPE_ANONYMOUS_FRONT_PAGE = 5;
    public static final int TYPE_ANONYMOUS_MULTIREDDIT = 6;

    public static final String USER_WHERE_SUBMITTED = "submitted";
    public static final String USER_WHERE_UPVOTED = "upvoted";
    public static final String USER_WHERE_DOWNVOTED = "downvoted";
    public static final String USER_WHERE_HIDDEN = "hidden";
    public static final String USER_WHERE_SAVED = "saved";
    public static final String USER_WHERE_GILDED = "gilded";

    private Executor executor;
    private Retrofit retrofit;
    private Retrofit gqlRetrofit;
    private String accessToken;
    private String accountName;
    private SharedPreferences sharedPreferences;
    private SharedPreferences postFeedScrolledPositionSharedPreferences;
    private String subredditOrUserName;
    private String query;
    private String trendingSource;
    private int postType;
    private SortType sortType;
    private PostFilter postFilter;
    private List<String> readPostList;
    private String userWhere;
    private String multiRedditPath;
    private LinkedHashSet<Post> postLinkedHashSet;
    private String previousLastItem;

    PostPagingSource(Executor executor, Retrofit retrofit, Retrofit gqlRetrofit, String accessToken, String accountName,
                     SharedPreferences sharedPreferences,
                     SharedPreferences postFeedScrolledPositionSharedPreferences, int postType,
                     SortType sortType, PostFilter postFilter, List<String> readPostList) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.gqlRetrofit = gqlRetrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.postType = postType;
        this.sortType = sortType == null ? new SortType(SortType.Type.BEST) : sortType;
        this.postFilter = postFilter;
        this.readPostList = readPostList;
        postLinkedHashSet = new LinkedHashSet<>();
    }

    PostPagingSource(Executor executor, Retrofit retrofit, Retrofit gqlRetrofit, String accessToken, String accountName,
                     SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                     String path, int postType, SortType sortType, PostFilter postFilter,
                     List<String> readPostList) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.gqlRetrofit = gqlRetrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        if (postType == TYPE_SUBREDDIT || postType == TYPE_ANONYMOUS_FRONT_PAGE) {
            this.subredditOrUserName = path;
        } else {
            if (sortType != null) {
                if (path.endsWith("/")) {
                    multiRedditPath = path + sortType.getType().value;
                } else {
                    multiRedditPath = path + "/" + sortType.getType().value;
                }
            } else {
                multiRedditPath = path;
            }
        }
        this.postType = postType;
        if (sortType == null) {
            if (path.equals("popular") || path.equals("all")) {
                this.sortType = new SortType(SortType.Type.HOT);
            } else {
                this.sortType = new SortType(SortType.Type.BEST);
            }
        } else {
            this.sortType = sortType;
        }
        this.postFilter = postFilter;
        this.readPostList = readPostList;
        postLinkedHashSet = new LinkedHashSet<>();
    }

    PostPagingSource(Executor executor, Retrofit retrofit, Retrofit gqlRetrofit, String accessToken, String accountName,
                     SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                     String subredditOrUserName, int postType, SortType sortType, PostFilter postFilter,
                     String where, List<String> readPostList) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.gqlRetrofit = gqlRetrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.subredditOrUserName = subredditOrUserName;
        this.postType = postType;
        this.sortType = sortType == null ? new SortType(SortType.Type.NEW) : sortType;
        this.postFilter = postFilter;
        userWhere = where;
        this.readPostList = readPostList;
        postLinkedHashSet = new LinkedHashSet<>();
    }

    PostPagingSource(Executor executor, Retrofit retrofit, Retrofit gqlRetrofit, String accessToken, String accountName,
                     SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                     String subredditOrUserName, String query, String trendingSource, int postType,
                     SortType sortType, PostFilter postFilter, List<String> readPostList) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.gqlRetrofit = gqlRetrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.subredditOrUserName = subredditOrUserName;
        this.query = query;
        this.trendingSource = trendingSource;
        this.postType = postType;
        this.sortType = sortType == null ? new SortType(SortType.Type.RELEVANCE) : sortType;
        this.postFilter = postFilter;
        postLinkedHashSet = new LinkedHashSet<>();
        this.readPostList = readPostList;
    }

    @Nullable
    @Override
    public String getRefreshKey(@NonNull PagingState<String, Post> pagingState) {
        return null;
    }

    @NonNull
    @Override
    public ListenableFuture<LoadResult<String, Post>> loadFuture(@NonNull LoadParams<String> loadParams) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        switch (postType) {
            case TYPE_FRONT_PAGE:
                if (gqlRetrofit != null){
                    GqlAPI gqlAPI = gqlRetrofit.create(GqlAPI.class);
                    return loadHomePosts(loadParams, api, gqlAPI);
                }else {
                    return loadHomePosts(loadParams, api, null);
                }
            case TYPE_SUBREDDIT:
                if (gqlRetrofit != null){
                    GqlAPI gqlAPI = gqlRetrofit.create(GqlAPI.class);
                    return loadSubredditPosts(loadParams, api, gqlAPI);
                }else {
                    return loadSubredditPosts(loadParams, api, null);
                }
            case TYPE_USER:
                if (gqlRetrofit != null){
                    GqlAPI gqlAPI = gqlRetrofit.create(GqlAPI.class);
                    return loadUserPosts(loadParams, api, gqlAPI);
                } else {
                    return loadUserPosts(loadParams, api, null);
                }
            case TYPE_SEARCH:
                if(gqlRetrofit != null){
                    GqlAPI gqlAPI = gqlRetrofit.create(GqlAPI.class);
                    return loadSearchPosts(loadParams, api, gqlAPI);
                }
                return loadSearchPosts(loadParams, api, null);
            case TYPE_MULTI_REDDIT:
                return loadMultiRedditPosts(loadParams, api);
            default:
                return loadAnonymousHomePosts(loadParams, api);
        }
    }

    public LoadResult<String, Post> transformData(Response<String> response) {
        if (response.isSuccessful()) {
            String responseString = response.body();
            LinkedHashSet<Post> newPosts = ParsePost.parsePostsSync(responseString, -1, postFilter, readPostList);
            String lastItem = ParsePost.getLastItem(responseString);
            if (newPosts == null) {
                return new LoadResult.Error<>(new Exception("Error parsing posts"));
            } else {
                int currentPostsSize = postLinkedHashSet.size();
                if (lastItem != null && lastItem.equals(previousLastItem)) {
                    lastItem = null;
                }
                previousLastItem = lastItem;

                postLinkedHashSet.addAll(newPosts);
                if (currentPostsSize == postLinkedHashSet.size()) {
                    return new LoadResult.Page<>(new ArrayList<>(), null, lastItem);
                } else {
                    return new LoadResult.Page<>(new ArrayList<>(postLinkedHashSet).subList(currentPostsSize, postLinkedHashSet.size()), null, lastItem);
                }
            }
        } else {
            return new LoadResult.Error<>(new Exception("Response failed"));
        }
    }

    public LoadResult<String, Post> transformDataGQL(Response<String> response) {
        if (response.isSuccessful()) {
            String responseString = response.body();
            LinkedHashSet<Post> newPosts = ParsePost.parsePostsSyncGQL(responseString, -1, postFilter, readPostList);
            String lastItem = ParsePost.getLastItemGQL(responseString);
            if (newPosts == null) {
                return new LoadResult.Error<>(new Exception("Error parsing posts"));
            } else {
                int currentPostsSize = postLinkedHashSet.size();
                if (lastItem != null && lastItem.equals(previousLastItem)) {
                    lastItem = null;
                }
                previousLastItem = lastItem;

                postLinkedHashSet.addAll(newPosts);
                if (currentPostsSize == postLinkedHashSet.size()) {
                    return new LoadResult.Page<>(new ArrayList<>(), null, lastItem);
                } else {
                    return new LoadResult.Page<>(new ArrayList<>(postLinkedHashSet).subList(currentPostsSize, postLinkedHashSet.size()), null, lastItem);
                }
            }
        } else {
            return new LoadResult.Error<>(new Exception("Response failed"));
        }
    }

    private ListenableFuture<LoadResult<String, Post>> loadHomePosts(@NonNull LoadParams<String> loadParams, RedditAPI redditAPI, GqlAPI gqlAPI) {
        ListenableFuture<Response<String>> bestPost;
        String afterKey;
        ListenableFuture<LoadResult<String, Post>> pageFuture;

        if (loadParams.getKey() == null) {
            boolean savePostFeedScrolledPosition = sortType != null && sortType.getType() == SortType.Type.BEST && sharedPreferences.getBoolean(SharedPreferencesUtils.SAVE_FRONT_PAGE_SCROLLED_POSITION, false);
            if (savePostFeedScrolledPosition) {
                String accountNameForCache = accountName == null ? SharedPreferencesUtils.FRONT_PAGE_SCROLLED_POSITION_ANONYMOUS : accountName;
                afterKey = postFeedScrolledPositionSharedPreferences.getString(accountNameForCache + SharedPreferencesUtils.FRONT_PAGE_SCROLLED_POSITION_FRONT_PAGE_BASE, null);
            } else {
                afterKey = null;
            }
        } else {
            afterKey = loadParams.getKey();
        }
        if(gqlAPI != null){
            RequestBody body = createHomePostsVars(sortType.getType(), sortType.getTime(), afterKey);
            bestPost = gqlAPI.getBestPostsListenableFuture(APIUtils.getOAuthHeader(accessToken), body);
            pageFuture = Futures.transform(bestPost, this::transformDataGQL, executor);
        }else{
            bestPost = redditAPI.getBestPostsListenableFuture(sortType.getType(), sortType.getTime(), afterKey,
                    APIUtils.getOAuthHeader(accessToken));
            pageFuture = Futures.transform(bestPost, this::transformData, executor);
        }

        ListenableFuture<LoadResult<String, Post>> partialLoadResultFuture =
                Futures.catching(pageFuture, HttpException.class,
                        LoadResult.Error::new, executor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, LoadResult.Error::new, executor);
    }

    private RequestBody createSearchPostsVars(String query, SortType.Type sortType, SortType.Time sortTime, String originPageType, String subredditOrUserName, String lastItem){
        /*
        {
            "id": "78271215900a",
                "variables": {
                    "query": "widowmaker",
                    "productSurface": "android",
                    "pageSize": null,
                    "afterCursor": "MjQ=",
                    "sort": "RELEVANCE",
                    "filters": [
                        {
                            "key": "nsfw",
                            "value": "1"
                        },
                        {
			                "key": "time_range",
			                "value": "month"
		                }
                    ],
                    "searchInput": {
                        "queryId": "3ad85c27-8e02-42d7-8ad8-7a7d1ad1ef90",
                        "correlationId": "cf55deea-2891-432d-8166-4eab9d16184d",
                        "originPageType": "home",
                        "structureType": "search"
                    },
                    "includeAwards": true
        }

 */
        JSONObject data = new JSONObject();
        try{
            data.put("operationName", "SearchPosts");
            JSONObject variables = new JSONObject();
            variables.put("query", query);
            variables.put("productSurface", "android");
            variables.put("pageSize", null);
            variables.put("afterCursor", lastItem);
            variables.put("sort", sortType.value.toUpperCase(Locale.ROOT));

            JSONArray filters = new JSONArray();

            JSONObject nsfwFilter = new JSONObject();
            nsfwFilter.put("key", "nsfw");
            nsfwFilter.put("value", "1");
            filters.put(nsfwFilter);

            if(sortTime != null){
                if(!sortTime.value.equals("all")){
                    JSONObject timeFilter = new JSONObject();
                    timeFilter.put("key", "time_range");
                    timeFilter.put("value", sortTime.value);
                    filters.put(timeFilter);
                }
            }

            if(subredditOrUserName != null){
                JSONObject subredditFilter = new JSONObject();
                subredditFilter.put("key", "subreddit_names");
                subredditFilter.put("value", subredditOrUserName);
                filters.put(subredditFilter);
            }

            variables.put("filters", filters);

            JSONObject searchInput = new JSONObject();
            searchInput.put("originPageType", originPageType);
            searchInput.put("structureType", "search");
            variables.put("searchInput", searchInput);

            variables.put("includeAwards", true);

            data.put("variables", variables);
            data.put("extensions", createExtensionsObject("0d65275f79845617802538c6c85fcacec73743590743158f69083e4a69ab68ea"));
        }catch (JSONException e){

        }

        return RequestBody.create(data.toString(), okhttp3.MediaType.parse("application/json; charset=utf-8"));
    }

    private JSONObject createExtensionsObject(String sha256Hash){
        JSONObject data = new JSONObject();
        JSONObject persistedQuery = new JSONObject();
        try{
            persistedQuery.put("version", 1);
            persistedQuery.put("sha256Hash", sha256Hash);
            data.put("persistedQuery", persistedQuery);
        }catch (JSONException e){

        }
        return data;
    }

    private RequestBody createHomePostsVars(SortType.Type sortType, SortType.Time sortTime, String lastItem){
        JSONObject data = new JSONObject();
        try{
            data.put("operationName", "HomeElements");

            JSONObject variables = new JSONObject();

            JSONObject advancedConfiguration = new JSONObject().put("eligibleExperienceOverrides", new JSONArray());
            JSONArray experienceInputs = new JSONArray().put("REONBOARDING_IN_FEED");

            variables.put("advancedConfiguration", advancedConfiguration);
            variables.put("experienceInputs", experienceInputs);
            variables.put("feedContext", new JSONObject().put("experimentOverrides", new JSONArray()));

            if(lastItem != null){
                if(lastItem.startsWith("t3_")){
                    String base64LastItem = android.util.Base64.encodeToString(lastItem.getBytes(StandardCharsets.UTF_8),android.util.Base64.DEFAULT);
                    variables.put("after", base64LastItem.trim());
                }else{
                    variables.put("after", lastItem);
                }
            }

            variables.put("forceAds", new JSONObject());
            variables.put("includeAnnouncements", false);
            variables.put("includeAwards", true);
            variables.put("includeCommentPostUnits", false);
            variables.put("includeExposureEvents", false);
            variables.put("includePostStats", true);
            variables.put("includeTopicRecommendations", false);
            variables.put("interestTopicIds", new JSONArray());
            variables.put("pageSize", 15);
            variables.put("sort", sortType.value.toUpperCase(Locale.ROOT));

            if(sortTime != null){
                variables.put("range", sortTime.value.toUpperCase(Locale.ROOT));
            }
            data.put("variables", variables);
            data.put("extensions", createExtensionsObject("fc940c3f6cabd1700b9f38fabbd2eb341a568061c3c106b9185046f162883472"));
        }catch (JSONException e){

        }
        return RequestBody.create(data.toString(), okhttp3.MediaType.parse("application/json; charset=utf-8"));
    }

    private RequestBody createSubredditPostsVars(String subredditName, SortType.Type sortType, SortType.Time sortTime, String lastItem){
        JSONObject data = new JSONObject();
        try{
            data.put("operationName", "SubredditFeedElements");

            JSONObject variables = new JSONObject();
            variables.put("subredditName", subredditName);
            variables.put("sort", sortType.value.toUpperCase(Locale.ROOT));

            if(sortTime != null){
                variables.put("range", sortTime.value.toUpperCase(Locale.ROOT));
            }
            if(lastItem != null){
                variables.put("after", lastItem);
            }

            variables.put("forceAds", new JSONObject());
            variables.put("feedFilters", new JSONObject());
            variables.put("optedIn", true);
            variables.put("includeSubredditInPosts", false);
            variables.put("includeAwards", true);
            variables.put("feedContext", new JSONObject().put("experimentOverrides", new JSONArray()));
            variables.put("includePostStats", true);

            data.put("variables", variables);
            data.put("extensions", createExtensionsObject("dc87cffd470f0aacdd3863592f29c8e38e6492ca7f7d8388c6e54fc0e6802e10"));
        }catch (JSONException e){

        }
        return RequestBody.create(data.toString(), okhttp3.MediaType.parse("application/json; charset=utf-8"));
    }

    private RequestBody createUserPostsVariables(String username, SortType.Type sortType, String lastItem){
        /*
        {
	"id": "908cb14d33d1",
	"variables": {
		"username": "milkandginger",
		"sort": "HOT",
		"filter": "POSTS_SETS",
		"includeAwards": false,
		"includePostStats": true
	}
}
         */

        JSONObject data = new JSONObject();

        try {
            data.put("operationName", "UserSubmittedPostSets");

            JSONObject variables = new JSONObject();
            variables.put("username", username);
            variables.put("sort", sortType.value.toUpperCase(Locale.ROOT));

            if(lastItem != null){
                variables.put("after", lastItem);
            }

            variables.put("filter", "POSTS_SETS");
            variables.put("includeAwards", true);
            variables.put("includePostStats", true);


            data.put("variables", variables);
            data.put("extensions", createExtensionsObject("6b5c7d946482883e03bc0c433d94408d24a06faf3c8ba27ea7e07fa1fe22fca7"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return RequestBody.create(data.toString(), okhttp3.MediaType.parse("application/json; charset=utf-8"));
    }

    private ListenableFuture<LoadResult<String, Post>> loadSubredditPosts(@NonNull LoadParams<String> loadParams, RedditAPI redditAPI, GqlAPI gqlAPI ) {
        ListenableFuture<Response<String>> subredditPost;
        boolean fallback = subredditOrUserName.equals("popular") || subredditOrUserName.equals("all");
        if (accessToken == null) {
            subredditPost = redditAPI.getSubredditBestPostsListenableFuture(subredditOrUserName, sortType.getType(), sortType.getTime(), loadParams.getKey());
        } else {
            if( fallback ){
                subredditPost = redditAPI.getSubredditBestPostsOauthListenableFuture(subredditOrUserName, sortType.getType(), sortType.getTime(), loadParams.getKey(), APIUtils.getOAuthHeader(accessToken));
            } else{
                RequestBody body = createSubredditPostsVars(subredditOrUserName, sortType.getType(), sortType.getTime(), loadParams.getKey());
                subredditPost = gqlAPI.getSubredditBestPostsOauthListenableFuture(APIUtils.getOAuthHeader(accessToken), body);
            }
        }
        ListenableFuture<LoadResult<String, Post>> pageFuture;
        if(fallback || accessToken == null){
            pageFuture = Futures.transform(subredditPost, this::transformData, executor);
        }else{
            pageFuture = Futures.transform(subredditPost, this::transformDataGQL, executor);
        }


        ListenableFuture<LoadResult<String, Post>> partialLoadResultFuture =
                Futures.catching(pageFuture, HttpException.class,
                        LoadResult.Error::new, executor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, LoadResult.Error::new, executor);
    }

    private ListenableFuture<LoadResult<String, Post>> loadUserPosts(@NonNull LoadParams<String> loadParams, RedditAPI api, GqlAPI gql) {
        ListenableFuture<Response<String>> userPosts;
        ListenableFuture<LoadResult<String, Post>> pageFuture;

        if (accessToken == null) {
            userPosts = api.getUserPostsListenableFuture(subredditOrUserName, loadParams.getKey(), sortType.getType(),
                    sortType.getTime());
            pageFuture = Futures.transform(userPosts, this::transformData, executor);
        } else {
            if(gql != null && userWhere.equals(USER_WHERE_SUBMITTED)){
                RequestBody body = createUserPostsVariables(subredditOrUserName, sortType.getType(), loadParams.getKey());
                userPosts = gql.getUserPostsOauthListenableFuture(APIUtils.getOAuthHeader(accessToken), body);
                pageFuture = Futures.transform(userPosts, this::transformDataGQL, executor);
            }else{
                userPosts = api.getUserPostsOauthListenableFuture(subredditOrUserName, userWhere, loadParams.getKey(), sortType.getType(),
                        sortType.getTime(), APIUtils.getOAuthHeader(accessToken));
                pageFuture = Futures.transform(userPosts, this::transformData, executor);
            }
        }


        ListenableFuture<LoadResult<String, Post>> partialLoadResultFuture =
                Futures.catching(pageFuture, HttpException.class,
                        LoadResult.Error::new, executor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, LoadResult.Error::new, executor);
    }

    private ListenableFuture<LoadResult<String, Post>> loadSearchPosts(@NonNull LoadParams<String> loadParams, RedditAPI api, GqlAPI gql) {
        ListenableFuture<Response<String>> searchPosts;

        boolean gqlEnabled = false;

        if (subredditOrUserName == null) {
            if (accessToken == null) {
                searchPosts = api.searchPostsListenableFuture(query, loadParams.getKey(), sortType.getType(), sortType.getTime(),
                        trendingSource);
            } else {
                //searchPosts = api.searchPostsOauthListenableFuture(query, loadParams.getKey(), sortType.getType(),sortType.getTime(), trendingSource, APIUtils.getOAuthHeader(accessToken));
                RequestBody body = createSearchPostsVars(query, sortType.getType(), sortType.getTime(), "home", null, loadParams.getKey());
                searchPosts = gql.searchPostsOauthListenableFuture(APIUtils.getOAuthHeader(accessToken), body);
                gqlEnabled = true;
            }
        } else {
            if (accessToken == null) {
                searchPosts = api.searchPostsInSpecificSubredditListenableFuture(subredditOrUserName, query,
                        sortType.getType(), sortType.getTime(), loadParams.getKey());
            } else {
                if(subredditOrUserName.startsWith("u_")){
                    searchPosts = api.searchPostsInSpecificSubredditOauthListenableFuture(subredditOrUserName, query, sortType.getType(), sortType.getTime(), loadParams.getKey(), APIUtils.getOAuthHeader(accessToken));
                }else{
                    RequestBody body = createSearchPostsVars(query, sortType.getType(), sortType.getTime(), "community", subredditOrUserName, loadParams.getKey());
                    searchPosts = gql.searchPostsOauthListenableFuture(APIUtils.getOAuthHeader(accessToken), body);
                    gqlEnabled = true;
                }
            }
        }

        ListenableFuture<LoadResult<String, Post>> pageFuture;
        if(gqlEnabled){
            pageFuture = Futures.transform(searchPosts, this::transformDataGQL, executor);
        }else{
            pageFuture = Futures.transform(searchPosts, this::transformData, executor);
        }

        ListenableFuture<LoadResult<String, Post>> partialLoadResultFuture =
                Futures.catching(pageFuture, HttpException.class,
                        LoadResult.Error::new, executor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, LoadResult.Error::new, executor);
    }

    private ListenableFuture<LoadResult<String, Post>> loadMultiRedditPosts(@NonNull LoadParams<String> loadParams, RedditAPI api) {
        ListenableFuture<Response<String>> multiRedditPosts;
        if (accessToken == null) {
            multiRedditPosts = api.getMultiRedditPostsListenableFuture(multiRedditPath, loadParams.getKey(), sortType.getTime());
        } else {
            multiRedditPosts = api.getMultiRedditPostsOauthListenableFuture(multiRedditPath, loadParams.getKey(),
                    sortType.getTime(), APIUtils.getOAuthHeader(accessToken));
        }

        ListenableFuture<LoadResult<String, Post>> pageFuture = Futures.transform(multiRedditPosts, this::transformData, executor);

        ListenableFuture<LoadResult<String, Post>> partialLoadResultFuture =
                Futures.catching(pageFuture, HttpException.class,
                        LoadResult.Error::new, executor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, LoadResult.Error::new, executor);
    }

    private ListenableFuture<LoadResult<String, Post>> loadAnonymousHomePosts(@NonNull LoadParams<String> loadParams, RedditAPI api) {
        ListenableFuture<Response<String>> anonymousHomePosts;
        anonymousHomePosts = api.getSubredditBestPostsListenableFuture(subredditOrUserName, sortType.getType(), sortType.getTime(), loadParams.getKey());

        ListenableFuture<LoadResult<String, Post>> pageFuture = Futures.transform(anonymousHomePosts, this::transformData, executor);

        ListenableFuture<LoadResult<String, Post>> partialLoadResultFuture =
                Futures.catching(pageFuture, HttpException.class,
                        LoadResult.Error::new, executor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, LoadResult.Error::new, executor);
    }
}
