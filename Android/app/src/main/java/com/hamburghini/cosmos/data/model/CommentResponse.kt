package com.hamburghini.cosmos.data.model

import com.google.gson.annotations.SerializedName

data class CommentResponse(
    @SerializedName("json") val json: CommentResponseJson
)

data class CommentResponseJson(
    @SerializedName("errors") val errors: List<List<String>>,
    @SerializedName("data") val data: CommentResponseData?
)

data class CommentResponseData(
    @SerializedName("things") val things: List<RedditObject<Comment>>
)
