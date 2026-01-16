package com.hamburghini.cosmos.data.model

import com.google.gson.annotations.SerializedName

data class Comment(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String, // fullname of comment, e.g., "t1_xxxxxx"
    @SerializedName("author") val author: String,
    @SerializedName("body") val body: String,
    @SerializedName("body_html") val body_html: String?,
    @SerializedName("score") val score: Int,
    @SerializedName("created_utc") val created_utc: Long,
    @SerializedName("replies") val replies: Any?
) {
    companion object {
        val mockComment = Comment(
            id = "k1a2b3c",
            name = "t1_k1a2b3c",
            author = "AndroidDev_99",
            body = "This is a really helpful explanation of the LazyColumn error! I was struggling with infinite constraints for hours until I found this. Thanks for the help!",
            body_html = "&lt;div class=\"md\"&gt;&lt;p&gt;This is a really helpful explanation...&lt;/p&gt;&lt;/div&gt;",
            score = 142,
            created_utc = 1705404667L,
            replies = null
        )
    }
}