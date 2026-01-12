package com.hamburghini.cosmos.util

import android.util.Log

object Logger {
    private const val TAG = "CosmosApp"

    enum class Severity {
        VERBOSE, DEBUG, INFO, WARNING, ERROR
    }

    fun v(message: String? = null, throwable: Throwable? = null) = log(Severity.VERBOSE, message, throwable)
    fun d(message: String? = null, throwable: Throwable? = null) = log(Severity.DEBUG, message, throwable)
    fun i(message: String? = null, throwable: Throwable? = null) = log(Severity.INFO, message, throwable)
    fun w(message: String? = null, throwable: Throwable? = null) = log(Severity.WARNING, message, throwable)
    fun e(message: String? = null, throwable: Throwable? = null) = log(Severity.ERROR, message, throwable)

    private fun log(severity: Severity, message: String?, throwable: Throwable?) {
        val stackTrace = Throwable().stackTrace

        val element = stackTrace.firstOrNull { it.className != Logger::class.java.name }

        val fileName = element?.fileName ?: "Unknown"
        val lineNumber = element?.lineNumber ?: 0

        val fullMessage = "[$severity] $fileName:$lineNumber - ${message ?: ""}"

        when (severity) {
            Severity.VERBOSE -> Log.v(TAG, fullMessage, throwable)
            Severity.DEBUG -> Log.d(TAG, fullMessage, throwable)
            Severity.INFO -> Log.i(TAG, fullMessage, throwable)
            Severity.WARNING -> Log.w(TAG, fullMessage, throwable)
            Severity.ERROR -> Log.e(TAG, fullMessage, throwable)
        }
    }
}