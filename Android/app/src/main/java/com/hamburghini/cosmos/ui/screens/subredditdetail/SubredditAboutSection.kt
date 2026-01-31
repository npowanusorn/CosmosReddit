package com.hamburghini.cosmos.ui.screens.subredditdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hamburghini.cosmos.data.model.SubredditAboutData

@Composable
fun SubredditAboutSection(
    modifier: Modifier = Modifier,
    subreddit: SubredditAboutData
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Description Section ---
        item {
            Section {
                SectionTitle("Description")
                Text(
                    text = subreddit.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // --- Community Stats ---
        item {
            Section {
                SectionTitle("Community Stats")

                Card(
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(
                            value = "${subreddit.accountsActive}",
                            label = "Members",
                            valueColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        HorizontalDivider(
                            modifier = Modifier
                                .height(48.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )

                        StatItem(
                            value = "${subreddit.activeUserCount}",
                            label = "Online",
                            valueColor = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- Community Info ---
        item {
            Section {
                SectionTitle("Community Info")

                InfoRow("Created", "${subreddit.createdUtc}")
                InfoRow("Type", "${subreddit.subredditType}")
                InfoRow("Submissions", "${subreddit.submissionType}")
            }
        }

        // --- Content Restrictions ---
        item {
            Section {
                SectionTitle("Content Restrictions")

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (subreddit.over18) {
                        RestrictionChip(
                            text = "NSFW (18+)",
                            background = Color(0xFFFF4444)
                        )
                    }

                    if (subreddit.quarantine == true) {
                        RestrictionChip(
                            text = "Quarantined",
                            background = Color(0xFFFFA500)
                        )
                    }

                    if (subreddit.restrictPosting == true) {
                        RestrictionChip(text = "Restricted Posting")
                    }

                    if (subreddit.restrictCommenting == true) {
                        RestrictionChip(text = "Restricted Commenting")
                    }
                }
            }
        }
    }
}

// Content version for use within LazyColumn items
@Composable
fun SubredditAboutSectionContent(
    subreddit: SubredditAboutData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Description Section
        Section {
            SectionTitle("Description")
            Text(
                text = subreddit.description ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Community Stats
        Section {
            SectionTitle("Community Stats")

            Card(
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.surfaceVariant
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(
                        value = "${subreddit.accountsActive}",
                        label = "Members",
                        valueColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    HorizontalDivider(
                        modifier = Modifier
                            .height(48.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )

                    StatItem(
                        value = "${subreddit.activeUserCount}",
                        label = "Online",
                        valueColor = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Community Info
        Section {
            SectionTitle("Community Info")

            InfoRow("Created", "${subreddit.createdUtc}")
            InfoRow("Type", "${subreddit.subredditType}")
            InfoRow("Submissions", "${subreddit.submissionType}")
        }

        // Content Restrictions
        Section {
            SectionTitle("Content Restrictions")

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (subreddit.over18) {
                    RestrictionChip(
                        text = "NSFW (18+)",
                        background = Color(0xFFFF4444)
                    )
                }

                if (subreddit.quarantine == true) {
                    RestrictionChip(
                        text = "Quarantined",
                        background = Color(0xFFFFA500)
                    )
                }

                if (subreddit.restrictPosting == true) {
                    RestrictionChip(text = "Restricted Posting")
                }

                if (subreddit.restrictCommenting == true) {
                    RestrictionChip(text = "Restricted Commenting")
                }
            }
        }
    }
}

@Composable
private fun Section(content: @Composable ColumnScope.() -> Unit) {
    Column(content = content)
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun RestrictionChip(
    text: String,
    background: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = background
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (background.luminance() < 0.5f) Color.White
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSubredditAboutSection() {
    SubredditAboutSection(
        subreddit = SubredditAboutData.mock
    )
}