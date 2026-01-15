package com.hamburghini.cosmos.data.model

import com.google.gson.annotations.SerializedName

data class SubredditRule(
    @SerializedName("rules") val rules: List<Rule>,
    @SerializedName("site_rules") val siteRules: List<String>,
    @SerializedName("site_rules_flow") val siteRulesFlow: List<SiteRuleFlow>?
)

data class Rule(
    @SerializedName("kind") val kind: String, // "link", "comment", "all"
    @SerializedName("short_name") val shortName: String,
    @SerializedName("description") val description: String,
    @SerializedName("description_html") val descriptionHtml: String?,
    @SerializedName("violation_reason") val violationReason: String,
    @SerializedName("created_utc") val createdUtc: Long,
    @SerializedName("priority") val priority: Int
)

data class SiteRuleFlow(
    @SerializedName("reasonTextToShow") val reasonTextToShow: String,
    @SerializedName("reasonText") val reasonText: String
)