package com.hamburghini.cosmos.data.model

import com.google.gson.annotations.SerializedName

// Top-level response for a Reddit listing, now generic over T
data class RedditListingResponse<T>(
    @SerializedName("data") val data: RedditListingData<T>
)

// Data wrapper for the listing content, now generic over T
data class RedditListingData<T>(
    @SerializedName("children") val children: List<RedditObject<T>>,
    @SerializedName("after") val after: String?,
    @SerializedName("before") val before: String?
)

// Individual Reddit object (Post, Comment, etc.), now generic over T
data class RedditObject<T>(
    @SerializedName("kind") val kind: String, // e.g., "t3" for post, "t1" for comment
    @SerializedName("data") val data: T // This will be deserialized into the specific type T by Gson
)