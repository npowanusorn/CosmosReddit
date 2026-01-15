package com.hamburghini.cosmos.core.network

import com.hamburghini.cosmos.model.AccessTokenResponse
import com.hamburghini.cosmos.model.Comment
import com.hamburghini.cosmos.model.Post
import com.hamburghini.cosmos.model.RedditListingResponse
import com.hamburghini.cosmos.model.SubredditDetail
import com.hamburghini.cosmos.model.UserInfo
import com.hamburghini.cosmos.model.Message
import com.hamburghini.cosmos.model.SubredditRule
import com.hamburghini.cosmos.model.Award
import com.hamburghini.cosmos.model.Trophy
import com.hamburghini.cosmos.model.SubmitResponse
import com.hamburghini.cosmos.model.CommentResponse
import com.hamburghini.cosmos.model.MoreChildrenResponse
import com.hamburghini.cosmos.model.SubredditAbout
import com.hamburghini.cosmos.model.FlairResponse
import com.hamburghini.cosmos.model.SubredditAboutData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RedditApiService {

    // --- Public API Endpoints (www.reddit.com) ---

    /**
     * Fetches hot posts from a specified public subreddit.
     *
     * @param subreddit The name of the subreddit (e.g., "all", "popular", "AndroidDev").
     * @param after (Optional) The 'fullname' of the last post received for pagination.
     * @param limit (Optional) The number of posts to return (default is typically 25, max 100).
     */
    @GET("r/{subreddit}/hot.json")
    suspend fun getHotPosts(
        @Path("subreddit") subreddit: String,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Post>>

    /**
     * Fetches new posts from a specified public subreddit.
     *
     * @param subreddit The name of the subreddit.
     * @param after (Optional) The 'fullname' of the last post received for pagination.
     * @param limit (Optional) The number of posts to return.
     */
    @GET("r/{subreddit}/new.json")
    suspend fun getNewPosts(
        @Path("subreddit") subreddit: String,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Post>>

    /**
     * Fetches top posts from a specified public subreddit.
     *
     * @param subreddit The name of the subreddit.
     * @param time The time period for top posts ("hour", "day", "week", "month", "year", "all").
     * @param after (Optional) The 'fullname' of the last post received for pagination.
     * @param limit (Optional) The number of posts to return.
     */
    @GET("r/{subreddit}/top.json")
    suspend fun getTopPosts(
        @Path("subreddit") subreddit: String,
        @Query("t") time: String = "day",
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Post>>

    /**
     * Fetches rising posts from a specified public subreddit.
     *
     * @param subreddit The name of the subreddit.
     * @param after (Optional) The 'fullname' of the last post received for pagination.
     * @param limit (Optional) The number of posts to return.
     */
    @GET("r/{subreddit}/rising.json")
    suspend fun getRisingPosts(
        @Path("subreddit") subreddit: String,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Post>>

    /**
     * Fetches controversial posts from a specified public subreddit.
     *
     * @param subreddit The name of the subreddit.
     * @param time The time period ("hour", "day", "week", "month", "year", "all").
     * @param after (Optional) The 'fullname' of the last post received for pagination.
     * @param limit (Optional) The number of posts to return.
     */
    @GET("r/{subreddit}/controversial.json")
    suspend fun getControversialPosts(
        @Path("subreddit") subreddit: String,
        @Query("t") time: String = "day",
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Post>>

    /**
     * Searches for posts across Reddit or within a specific subreddit.
     *
     * @param query The search query.
     * @param subreddit (Optional) Limit search to a specific subreddit.
     * @param sort Sort order ("relevance", "hot", "top", "new", "comments").
     * @param time Time filter for sorted results ("hour", "day", "week", "month", "year", "all").
     * @param after (Optional) For pagination.
     * @param limit Number of results to return.
     */
    @GET("search.json")
    suspend fun searchPosts(
        @Query("q") query: String,
        @Query("restrict_sr") restrictToSubreddit: Boolean = false,
        @Query("subreddit") subreddit: String? = null,
        @Query("sort") sort: String = "relevance",
        @Query("t") time: String? = null,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Post>>

    /**
     * Searches for subreddits.
     *
     * @param query The search query.
     * @param sort Sort order ("relevance", "activity").
     * @param after (Optional) For pagination.
     * @param limit Number of results to return.
     */
    @GET("subreddits/search.json")
    suspend fun searchSubreddits(
        @Query("q") query: String,
        @Query("sort") sort: String = "relevance",
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<SubredditAbout>>

    /**
     * Fetches details for a specific post by its 'fullname' (e.g., "t3_xyz").
     *
     * @param postNames The 'fullname' of the post (e.g., "t3_abc").
     */
    @GET("by_id/{names}.json")
    suspend fun getPostDetails(
        @Path("names") postNames: String
    ): Response<RedditListingResponse<Post>>

    /**
     * Fetches comments for a specific post.
     * This endpoint returns a list of two listings: post data and comments data.
     *
     * @param subreddit The subreddit name of the post.
     * @param postId The ID of the post (without t3_ prefix).
     * @param sort The sorting method for comments ("confidence", "top", "new", "controversial", "old", "qa").
     * @param limit Number of comments to load initially.
     * @param depth Maximum depth of comment tree to return.
     */
    @GET("r/{subreddit}/comments/{postId}.json")
    suspend fun getPostComments(
        @Path("subreddit") subreddit: String,
        @Path("postId") postId: String,
        @Query("sort") sort: String = "confidence",
        @Query("limit") limit: Int = 100,
        @Query("depth") depth: Int? = null
    ): Response<List<RedditListingResponse<Comment>>>

    /**
     * Fetches subreddit detail.
     *
     * @param id The id of the subreddit (e.g., "t5_2qh0u").
     */
    @GET("api/info.json")
    fun getSubredditDetail(
        @Query("id") id: String,
    ): Call<RedditListingResponse<SubredditDetail>>

    /**
     * Fetches about information for a specific subreddit.
     *
     * @param subreddit The name of the subreddit.
     */
    @GET("r/{subreddit}/about.json")
    suspend fun getSubredditAbout(
        @Path("subreddit") subreddit: String
    ): Response<SubredditAbout>

    /**
     * Fetches rules for a specific subreddit.
     *
     * @param subreddit The name of the subreddit.
     */
    @GET("r/{subreddit}/about/rules.json")
    suspend fun getSubredditRules(
        @Path("subreddit") subreddit: String
    ): Response<SubredditRule>

    /**
     * Gets a random post from Reddit or a specific subreddit.
     *
     * @param subreddit (Optional) Subreddit to get random post from.
     */
    @GET("r/{subreddit}/random.json")
    suspend fun getRandomPost(
        @Path("subreddit") subreddit: String = "all"
    ): Response<List<RedditListingResponse<Post>>>

    /**
     * Gets posts from a specific user.
     *
     * @param username The username (without u/ prefix).
     * @param sort Sort order ("hot", "new", "top", "controversial").
     * @param time Time filter for sorted results.
     * @param after For pagination.
     * @param limit Number of posts to return.
     */
    @GET("user/{username}/submitted.json")
    suspend fun getUserPosts(
        @Path("username") username: String,
        @Query("sort") sort: String = "new",
        @Query("t") time: String? = null,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Post>>

    /**
     * Gets comments from a specific user.
     *
     * @param username The username (without u/ prefix).
     * @param sort Sort order ("hot", "new", "top", "controversial").
     * @param time Time filter for sorted results.
     * @param after For pagination.
     * @param limit Number of comments to return.
     */
    @GET("user/{username}/comments.json")
    suspend fun getUserComments(
        @Path("username") username: String,
        @Query("sort") sort: String = "new",
        @Query("t") time: String? = null,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Comment>>

    // --- OAuth/Authenticated API Endpoints ---

    /**
     * Exchanges an authorization code or refresh token for an access token.
     * This endpoint is on the "www.reddit.com" base URL.
     *
     * @param grantType "authorization_code" for initial token, "refresh_token" for refreshing.
     * @param code The authorization code (if grantType is "authorization_code").
     * @param redirectUri The redirect URI used in the OAuth flow.
     * @param basicAuth The "Basic" authorization header (Base64 encoded client_id:).
     * @param refreshToken The refresh token (if grantType is "refresh_token").
     * @param deviceId (Optional, if using client-side flow for unauthenticated apps).
     */
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("api/v1/access_token")
    suspend fun getAccessToken(
        @Field("grant_type") grantType: String,
        @Field("code") code: String? = null,
        @Field("redirect_uri") redirectUri: String,
        @Header("Authorization") basicAuth: String,
        @Field("refresh_token") refreshToken: String? = null,
        @Field("device_id") deviceId: String? = null
    ): Response<AccessTokenResponse>

    /**
     * Fetches hot posts from the authenticated user's front page.
     * Requires authentication.
     *
     * @param after For pagination.
     * @param limit Number of posts to return.
     */
    @GET("hot.json")
    suspend fun getMyHotPosts(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Post>>

    /**
     * Fetches best posts from the authenticated user's front page.
     * Requires authentication.
     */
    @GET("best.json")
    suspend fun getMyBestPosts(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Post>>

    /**
     * Fetches new posts from the authenticated user's front page.
     * Requires authentication.
     */
    @GET("new.json")
    suspend fun getMyNewPosts(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Post>>

    /**
     * Fetches top posts from the authenticated user's front page.
     * Requires authentication.
     */
    @GET("top.json")
    suspend fun getMyTopPosts(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25,
        @Query("t") timeframe: String = "day"
    ): Response<RedditListingResponse<Post>>

    /**
     * Votes on a post or comment.
     * Requires authentication.
     *
     * @param id The fullname of the item to vote on (e.g., "t3_postid", "t1_commentid").
     * @param direction -1 for downvote, 0 for remove vote, 1 for upvote.
     */
    @FormUrlEncoded
    @POST("api/vote")
    suspend fun vote(
        @Field("id") id: String,
        @Field("dir") direction: Int
    ): Response<ResponseBody>

    /**
     * Saves a post or comment.
     * Requires authentication.
     *
     * @param id The fullname of the item to save.
     */
    @FormUrlEncoded
    @POST("api/save")
    suspend fun save(
        @Field("id") id: String
    ): Response<ResponseBody>

    /**
     * Unsaves a post or comment.
     * Requires authentication.
     *
     * @param id The fullname of the item to unsave.
     */
    @FormUrlEncoded
    @POST("api/unsave")
    suspend fun unsave(
        @Field("id") id: String
    ): Response<ResponseBody>

    /**
     * Hides a post from the user's listings.
     * Requires authentication.
     *
     * @param id The fullname of the post to hide.
     */
    @FormUrlEncoded
    @POST("api/hide")
    suspend fun hide(
        @Field("id") id: String
    ): Response<ResponseBody>

    /**
     * Unhides a post.
     * Requires authentication.
     *
     * @param id The fullname of the post to unhide.
     */
    @FormUrlEncoded
    @POST("api/unhide")
    suspend fun unhide(
        @Field("id") id: String
    ): Response<ResponseBody>

    /**
     * Subscribes to a subreddit.
     * Requires authentication.
     *
     * @param subredditName The fullname of the subreddit (e.g., "t5_2qh0u").
     * @param action "sub" to subscribe, "unsub" to unsubscribe.
     * @param skipInitialDefaults Whether to skip adding default subreddits (for new accounts).
     */
    @FormUrlEncoded
    @POST("api/subscribe")
    suspend fun subscribe(
        @Field("sr") subredditName: String,
        @Field("action") action: String,
        @Field("skip_initial_defaults") skipInitialDefaults: Boolean = true
    ): Response<ResponseBody>

    /**
     * Submits a new text post to a subreddit.
     * Requires authentication.
     *
     * @param subreddit The subreddit to post to.
     * @param title The title of the post.
     * @param text The body text of the post.
     * @param nsfw Whether the post is NSFW.
     * @param spoiler Whether the post contains spoilers.
     * @param sendReplies Whether to send replies to inbox.
     * @param flairId (Optional) Flair template ID.
     * @param flairText (Optional) Flair text.
     */
    @FormUrlEncoded
    @POST("api/submit")
    suspend fun submitTextPost(
        @Field("sr") subreddit: String,
        @Field("kind") kind: String = "self",
        @Field("title") title: String,
        @Field("text") text: String,
        @Field("nsfw") nsfw: Boolean = false,
        @Field("spoiler") spoiler: Boolean = false,
        @Field("sendreplies") sendReplies: Boolean = true,
        @Field("flair_id") flairId: String? = null,
        @Field("flair_text") flairText: String? = null
    ): Response<SubmitResponse>

    /**
     * Submits a new link post to a subreddit.
     * Requires authentication.
     *
     * @param subreddit The subreddit to post to.
     * @param title The title of the post.
     * @param url The URL to submit.
     * @param nsfw Whether the post is NSFW.
     * @param spoiler Whether the post contains spoilers.
     * @param sendReplies Whether to send replies to inbox.
     * @param flairId (Optional) Flair template ID.
     * @param flairText (Optional) Flair text.
     */
    @FormUrlEncoded
    @POST("api/submit")
    suspend fun submitLinkPost(
        @Field("sr") subreddit: String,
        @Field("kind") kind: String = "link",
        @Field("title") title: String,
        @Field("url") url: String,
        @Field("nsfw") nsfw: Boolean = false,
        @Field("spoiler") spoiler: Boolean = false,
        @Field("sendreplies") sendReplies: Boolean = true,
        @Field("flair_id") flairId: String? = null,
        @Field("flair_text") flairText: String? = null
    ): Response<SubmitResponse>

    /**
     * Submits a new comment on a post or reply to a comment.
     * Requires authentication.
     *
     * @param parentId The fullname of the parent (post or comment).
     * @param text The comment text in markdown.
     */
    @FormUrlEncoded
    @POST("api/comment")
    suspend fun submitComment(
        @Field("parent") parentId: String,
        @Field("text") text: String
    ): Response<CommentResponse>

    /**
     * Edits a post or comment.
     * Requires authentication.
     *
     * @param thingId The fullname of the post or comment to edit.
     * @param text The new text content in markdown.
     */
    @FormUrlEncoded
    @POST("api/editusertext")
    suspend fun editText(
        @Field("thing_id") thingId: String,
        @Field("text") text: String
    ): Response<ResponseBody>

    /**
     * Deletes a post or comment.
     * Requires authentication.
     *
     * @param id The fullname of the item to delete.
     */
    @FormUrlEncoded
    @POST("api/del")
    suspend fun delete(
        @Field("id") id: String
    ): Response<ResponseBody>

    /**
     * Marks a post as NSFW or removes NSFW marking.
     * Requires authentication.
     *
     * @param id The fullname of the post.
     */
    @FormUrlEncoded
    @POST("api/marknsfw")
    suspend fun markNsfw(
        @Field("id") id: String
    ): Response<ResponseBody>

    /**
     * Removes NSFW marking from a post.
     * Requires authentication.
     *
     * @param id The fullname of the post.
     */
    @FormUrlEncoded
    @POST("api/unmarknsfw")
    suspend fun unmarkNsfw(
        @Field("id") id: String
    ): Response<ResponseBody>

    /**
     * Marks a post as containing spoilers.
     * Requires authentication.
     *
     * @param id The fullname of the post.
     */
    @FormUrlEncoded
    @POST("api/spoiler")
    suspend fun markSpoiler(
        @Field("id") id: String
    ): Response<ResponseBody>

    /**
     * Removes spoiler marking from a post.
     * Requires authentication.
     *
     * @param id The fullname of the post.
     */
    @FormUrlEncoded
    @POST("api/unspoiler")
    suspend fun unmarkSpoiler(
        @Field("id") id: String
    ): Response<ResponseBody>

    /**
     * Reports a post or comment.
     * Requires authentication.
     *
     * @param thingId The fullname of the item to report.
     * @param reason The reason for reporting.
     * @param siteReason (Optional) Specific site-wide rule violation.
     * @param otherReason (Optional) Custom reason text.
     */
    @FormUrlEncoded
    @POST("api/report")
    suspend fun report(
        @Field("thing_id") thingId: String,
        @Field("reason") reason: String,
        @Field("site_reason") siteReason: String? = null,
        @Field("other_reason") otherReason: String? = null
    ): Response<ResponseBody>

    /**
     * Blocks a user.
     * Requires authentication.
     *
     * @param accountId The account ID of the user to block (e.g., "t2_example").
     */
    @FormUrlEncoded
    @POST("api/block_user")
    suspend fun blockUser(
        @Field("account_id") accountId: String
    ): Response<ResponseBody>

    /**
     * Fetches the current authenticated user's information.
     * Requires authentication.
     */
    @GET("api/v1/me.json")
    suspend fun getMe(): Response<UserInfo>

    /**
     * Fetches information about a specific user.
     *
     * @param username The username (without u/ prefix).
     */
    @GET("user/{username}/about.json")
    suspend fun getUserAbout(
        @Path("username") username: String
    ): Response<RedditListingResponse<UserInfo>>

    /**
     * Gets the authenticated user's saved posts and comments.
     * Requires authentication.
     *
     * @param after For pagination.
     * @param limit Number of items to return.
     */
    @GET("user/{username}/saved.json")
    suspend fun getMySaved(
        @Path("username") username: String,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Any>> // Can be Post or Comment

    /**
     * Gets the authenticated user's upvoted posts and comments.
     * Requires authentication.
     *
     * @param username The authenticated user's username.
     * @param after For pagination.
     * @param limit Number of items to return.
     */
    @GET("user/{username}/upvoted.json")
    suspend fun getMyUpvoted(
        @Path("username") username: String,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Any>> // Can be Post or Comment

    /**
     * Gets the authenticated user's downvoted posts and comments.
     * Requires authentication.
     *
     * @param username The authenticated user's username.
     * @param after For pagination.
     * @param limit Number of items to return.
     */
    @GET("user/{username}/downvoted.json")
    suspend fun getMyDownvoted(
        @Path("username") username: String,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Any>> // Can be Post or Comment

    /**
     * Gets the authenticated user's hidden posts.
     * Requires authentication.
     *
     * @param username The authenticated user's username.
     * @param after For pagination.
     * @param limit Number of items to return.
     */
    @GET("user/{username}/hidden.json")
    suspend fun getMyHidden(
        @Path("username") username: String,
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Post>>

    /**
     * Gets the authenticated user's inbox messages (all types).
     * Requires authentication.
     *
     * @param after For pagination.
     * @param limit Number of messages to return.
     */
    @GET("message/inbox.json")
    suspend fun getInbox(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Message>>

    /**
     * Gets only unread messages from inbox.
     * Requires authentication.
     *
     * @param after For pagination.
     * @param limit Number of messages to return.
     */
    @GET("message/unread.json")
    suspend fun getUnreadMessages(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Message>>

    /**
     * Gets sent messages.
     * Requires authentication.
     *
     * @param after For pagination.
     * @param limit Number of messages to return.
     */
    @GET("message/sent.json")
    suspend fun getSentMessages(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<Message>>

    /**
     * Sends a private message.
     * Requires authentication.
     *
     * @param to Username to send message to.
     * @param subject Message subject.
     * @param text Message body.
     */
    @FormUrlEncoded
    @POST("api/compose")
    suspend fun sendMessage(
        @Field("to") to: String,
        @Field("subject") subject: String,
        @Field("text") text: String
    ): Response<ResponseBody>

    /**
     * Marks messages as read.
     * Requires authentication.
     *
     * @param id Comma-separated fullnames of messages to mark as read.
     */
    @FormUrlEncoded
    @POST("api/read_message")
    suspend fun markMessagesRead(
        @Field("id") id: String
    ): Response<ResponseBody>

    /**
     * Marks all messages as read.
     * Requires authentication.
     */
    @POST("api/read_all_messages")
    suspend fun markAllMessagesRead(): Response<ResponseBody>

    /**
     * Gets more comments when a comment tree is truncated.
     *
     * @param linkId The fullname of the post (e.g., "t3_abc").
     * @param children Comma-separated list of comment IDs to fetch.
     * @param sort Sort order for the comments.
     */
    @GET("api/morechildren.json")
    suspend fun getMoreComments(
        @Query("link_id") linkId: String,
        @Query("children") children: String,
        @Query("sort") sort: String = "confidence"
    ): Response<MoreChildrenResponse>

    /**
     * Gets available flairs for a subreddit.
     * Requires authentication for user flairs.
     *
     * @param subreddit The subreddit name.
     */
    @GET("r/{subreddit}/api/user_flair_v2.json")
    suspend fun getUserFlairs(
        @Path("subreddit") subreddit: String
    ): Response<List<FlairResponse>>

    /**
     * Gets available link flairs for a subreddit.
     *
     * @param subreddit The subreddit name.
     */
    @GET("r/{subreddit}/api/link_flair_v2.json")
    suspend fun getLinkFlairs(
        @Path("subreddit") subreddit: String
    ): Response<List<FlairResponse>>

    /**
     * Gets the authenticated user's trophies.
     * Requires authentication.
     */
    @GET("api/v1/me/trophies.json")
    suspend fun getMyTrophies(): Response<Trophy>

    /**
     * Gets trophies for a specific user.
     *
     * @param username Username without u/ prefix.
     */
    @GET("api/v1/user/{username}/trophies.json")
    suspend fun getUserTrophies(
        @Path("username") username: String
    ): Response<Trophy>

    /**
     * Gets a list of trending subreddits.
     */
    @GET("api/trending_subreddits.json")
    suspend fun getTrendingSubreddits(): Response<List<String>>

    /**
     * Gets subreddits the authenticated user is subscribed to.
     * Requires authentication.
     *
     * @param after For pagination.
     * @param limit Number of subreddits to return.
     */
    @GET("subreddits/mine/subscriber.json")
    suspend fun getMySubscribedSubreddits(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 100
    ): Response<RedditListingResponse<SubredditAboutData>>

    /**
     * Gets popular subreddits.
     *
     * @param after For pagination.
     * @param limit Number of subreddits to return.
     */
    @GET("subreddits/popular.json")
    suspend fun getPopularSubreddits(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<SubredditAboutData>>

    /**
     * Gets new subreddits.
     *
     * @param after For pagination.
     * @param limit Number of subreddits to return.
     */
    @GET("subreddits/new.json")
    suspend fun getNewSubreddits(
        @Query("after") after: String? = null,
        @Query("limit") limit: Int = 25
    ): Response<RedditListingResponse<SubredditAbout>>

    /**
     * Gets available awards for giving.
     * Requires authentication.
     */
    @GET("api/v1/gold/gild/{fullname}.json")
    suspend fun getAvailableAwards(
        @Path("fullname") fullname: String
    ): Response<List<Award>>
}