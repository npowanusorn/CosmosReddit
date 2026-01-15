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
)