package com.hamburghini.cosmos.data.model

import com.google.gson.annotations.SerializedName

data class FlairResponse(
    @SerializedName("id") val id: String,
    @SerializedName("text") val text: String,
    @SerializedName("text_editable") val textEditable: Boolean,
    @SerializedName("mod_only") val modOnly: Boolean,
    @SerializedName("text_color") val textColor: String, // "light" or "dark"
    @SerializedName("background_color") val backgroundColor: String,
    @SerializedName("css_class") val cssClass: String?,
    @SerializedName("richtext") val richtext: List<FlairRichtext>?,
    @SerializedName("type") val type: String // "text" or "richtext"
)

data class FlairRichtext(
    @SerializedName("e") val e: String, // "text" or "emoji"
    @SerializedName("t") val t: String?, // text content
    @SerializedName("a") val a: String?, // emoji id
    @SerializedName("u") val u: String? // emoji URL
)