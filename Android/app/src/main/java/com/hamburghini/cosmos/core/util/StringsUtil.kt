package com.hamburghini.cosmos.core.util

fun String.decodeHtml(): String = replace("&amp;", "&")