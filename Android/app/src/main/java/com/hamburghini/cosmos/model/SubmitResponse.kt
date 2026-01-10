package com.hamburghini.cosmos.model

import com.google.gson.annotations.SerializedName

data class SubmitResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("jquery") val jquery: List<List<Any>>?, // Reddit's legacy response format
    @SerializedName("data") val data: SubmitResponseData?
)

data class SubmitResponseData(
    @SerializedName("url") val url: String,
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String // fullname (e.g., "t3_xxxxx")
)
