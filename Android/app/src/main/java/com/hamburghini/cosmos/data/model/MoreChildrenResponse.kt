package com.hamburghini.cosmos.data.model

import com.google.gson.annotations.SerializedName

data class MoreChildrenResponse(
    @SerializedName("json") val json: MoreChildrenJson
)

data class MoreChildrenJson(
    @SerializedName("errors") val errors: List<List<String>>,
    @SerializedName("data") val data: MoreChildrenData?
)

data class MoreChildrenData(
    @SerializedName("things") val things: List<RedditObject<Comment>>
)