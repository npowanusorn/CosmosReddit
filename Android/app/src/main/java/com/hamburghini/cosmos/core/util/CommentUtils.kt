package com.hamburghini.cosmos.core.util

import com.google.gson.Gson
import com.hamburghini.cosmos.data.model.Comment

/**
 * Utilities for parsing and managing Reddit comment trees
 */
object CommentUtils {

    private val gson = Gson()

    /**
     * Parse comment replies from the Reddit API response
     * The replies field can be:
     * - Empty string ""
     * - null
     * - A RedditListingResponse object containing more comments
     */
    fun parseCommentReplies(replies: Any?): List<Comment> {
        return try {
            when (replies) {
                is String -> {
                    // Empty replies
                    emptyList()
                }
                null -> {
                    // No replies
                    emptyList()
                }
                else -> {
                    // Try to parse as RedditListingResponse
                    val jsonElement = gson.toJsonTree(replies)

                    if (jsonElement.isJsonObject) {
                        val jsonObject = jsonElement.asJsonObject

                        // Check if it has the structure of a listing
                        if (jsonObject.has("data")) {
                            val dataObject = jsonObject.getAsJsonObject("data")

                            if (dataObject.has("children")) {
                                val childrenArray = dataObject.getAsJsonArray("children")
                                val comments = mutableListOf<Comment>()

                                childrenArray.forEach { child ->
                                    val childObject = child.asJsonObject
                                    val kind = childObject.get("kind")?.asString

                                    // Only process actual comments (kind = "t1")
                                    // Skip "more" objects (kind = "more")
                                    if (kind == "t1") {
                                        val commentData = childObject.getAsJsonObject("data")
                                        val comment = gson.fromJson(commentData, Comment::class.java)
                                        comments.add(comment)
                                    }
                                }

                                comments
                            } else {
                                emptyList()
                            }
                        } else {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                }
            }
        } catch (e: Exception) {
            // If parsing fails, return empty list
            emptyList()
        }
    }

    /**
     * Flatten a comment tree into a list with depth information
     * This is useful for rendering in a LazyColumn
     */
    fun flattenCommentTree(comments: List<Comment>): List<CommentWithDepth> {
        val flatList = mutableListOf<CommentWithDepth>()

        fun traverse(comment: Comment, depth: Int) {
            flatList.add(CommentWithDepth(comment, depth))

            val replies = parseCommentReplies(comment.replies)
            replies.forEach { reply ->
                traverse(reply, depth + 1)
            }
        }

        comments.forEach { comment ->
            traverse(comment, 0)
        }

        return flatList
    }

    /**
     * Count total number of comments in a tree (including nested replies)
     */
    fun countTotalComments(comments: List<Comment>): Int {
        var count = 0

        fun traverse(comment: Comment) {
            count++
            val replies = parseCommentReplies(comment.replies)
            replies.forEach { reply ->
                traverse(reply)
            }
        }

        comments.forEach { comment ->
            traverse(comment)
        }

        return count
    }

    /**
     * Find a specific comment by ID in the tree
     */
    fun findCommentById(comments: List<Comment>, commentId: String): Comment? {
        fun search(comment: Comment): Comment? {
            if (comment.id == commentId) {
                return comment
            }

            val replies = parseCommentReplies(comment.replies)
            replies.forEach { reply ->
                search(reply)?.let { return it }
            }

            return null
        }

        comments.forEach { comment ->
            search(comment)?.let { return it }
        }

        return null
    }

    /**
     * Get maximum depth of comment tree
     */
    fun getMaxDepth(comments: List<Comment>): Int {
        var maxDepth = 0

        fun traverse(comment: Comment, depth: Int) {
            if (depth > maxDepth) {
                maxDepth = depth
            }

            val replies = parseCommentReplies(comment.replies)
            replies.forEach { reply ->
                traverse(reply, depth + 1)
            }
        }

        comments.forEach { comment ->
            traverse(comment, 0)
        }

        return maxDepth
    }

    /**
     * Extract "more comments" IDs from a comment listing
     * These are placeholder objects that need to be loaded separately
     */
    fun extractMoreCommentsIds(replies: Any?): List<String> {
        return try {
            when (replies) {
                is String, null -> emptyList()
                else -> {
                    val jsonElement = gson.toJsonTree(replies)

                    if (jsonElement.isJsonObject) {
                        val jsonObject = jsonElement.asJsonObject

                        if (jsonObject.has("data")) {
                            val dataObject = jsonObject.getAsJsonObject("data")

                            if (dataObject.has("children")) {
                                val childrenArray = dataObject.getAsJsonArray("children")
                                val moreIds = mutableListOf<String>()

                                childrenArray.forEach { child ->
                                    val childObject = child.asJsonObject
                                    val kind = childObject.get("kind")?.asString

                                    // Look for "more" objects
                                    if (kind == "more") {
                                        val moreData = childObject.getAsJsonObject("data")
                                        val children = moreData.getAsJsonArray("children")

                                        children?.forEach { id ->
                                            moreIds.add(id.asString)
                                        }
                                    }
                                }

                                moreIds
                            } else {
                                emptyList()
                            }
                        } else {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun sanitize(input: String): String {
        if (input.isBlank()) return input

        val lines = input.lines()
        val result = StringBuilder()
        var inCodeBlock = false

        for (line in lines) {
            var currentLine = line

            // Toggle fenced code blocks
            if (currentLine.trim().startsWith("```")) {
                inCodeBlock = !inCodeBlock
                result.appendLine(currentLine)
                continue
            }

            if (!inCodeBlock) {
                currentLine = fixHeaders(currentLine)
                currentLine = fixListSpacing(currentLine)
                currentLine = fixHorizontalRule(currentLine)
            }

            result.appendLine(currentLine)
        }

        return result.toString().trimEnd()
    }

    /**
     * Fix headers like:
     * ##Header -> ## Header
     */
    private fun fixHeaders(line: String): String {
        return line.replace(
            Regex("^(#{1,6})([^#\\s])"),
            "$1 $2"
        )
    }

    /**
     * Fix list items:
     * -item -> - item
     * *item -> * item
     */
    private fun fixListSpacing(line: String): String {
        return when {
            line.matches(Regex("^[-+]\\S.*")) ->
                line.replaceFirst(Regex("^([+-])(\\S)"), "$1 $2")

            line.matches(Regex("^\\*\\S.*")) &&
                    !line.matches(Regex("^\\*\\*.*\\*\\*$")) &&
                    !line.matches(Regex("^\\*.*\\*$")) ->
                line.replaceFirst(Regex("^(\\*)(\\S)"), "$1 $2")

            else -> line
        }
    }

    /**
     * Normalize horizontal rules:
     * ---text -> ---
     */
    private fun fixHorizontalRule(line: String): String {
        return if (line.trim().matches(Regex("^-{3,}$"))) {
            "---"
        } else {
            line
        }
    }
}

/**
 * Data class representing a comment with its depth in the tree
 * Useful for flattened list rendering
 */
data class CommentWithDepth(
    val comment: Comment,
    val depth: Int
)