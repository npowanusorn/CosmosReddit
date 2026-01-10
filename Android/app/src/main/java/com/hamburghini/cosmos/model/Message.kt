package com.hamburghini.cosmos.model

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String, // fullname (e.g., "t4_xxxxx")
    @SerializedName("subject") val subject: String,
    @SerializedName("body") val body: String,
    @SerializedName("body_html") val bodyHtml: String?,
    @SerializedName("author") val author: String,
    @SerializedName("dest") val dest: String?, // recipient username
    @SerializedName("created_utc") val createdUtc: Long,
    @SerializedName("context") val context: String?, // permalink to comment/post context
    @SerializedName("subreddit") val subreddit: String?, // subreddit name if applicable
    @SerializedName("subreddit_name_prefixed") val subredditNamePrefixed: String?,
    @SerializedName("new") val isNew: Boolean,
    @SerializedName("was_comment") val wasComment: Boolean, // true if message is a comment reply
    @SerializedName("type") val type: String?, // message type
    @SerializedName("parent_id") val parentId: String?, // parent comment/post if reply
    @SerializedName("first_message_name") val firstMessageName: String? // first message in thread
)