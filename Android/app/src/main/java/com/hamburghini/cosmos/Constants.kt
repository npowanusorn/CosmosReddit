package com.hamburghini.cosmos

object Constants {
    const val REDDIT_CLIENT_ID = "DYwlBxiztc_NidTFw-yRaQ"
    const val REDDIT_REDIRECT_URI = "mysimprc://auth"
    const val REDDIT_RESPONSE_TYPE = "code"
    const val REDDIT_STATE = "RANDOM_STRING_TO_PREVENT_CSRF"
    const val REDDIT_DURATION = "permanent"
    const val REDDIT_SCOPE = "identity,read,submit,vote,privatemessages,mysubreddits"

    // Base URL for OAuth
    const val REDDIT_AUTH_BASE_URL = "https://www.reddit.com/api/v1/authorize"
    const val REDDIT_ACCESS_TOKEN_URL = "https://www.reddit.com/api/v1/access_token"

    // Base URL for authenticated API calls (different from public API)
    const val REDDIT_OAUTH_API_BASE_URL = "https://oauth.reddit.com/"

    const val FULLSCREEN_MEDIA_IS_IMAGE = "fullscreen_media_is_image"
    const val FULLSCREEN_MEDIA_IMG_THUMB_LIST = "fullscreen_media_img_thumb_list"
    const val FULLSCREEN_MEDIA_IMG_LIST = "fullscreen_media_img_list"
    const val FULLSCREEN_MEDIA_VIDEO_URL = "fullscreen_media_video_url"
    const val FULLSCREEN_MEDIA_VIDEO_URL_START_POS = "fullscreen_media_video_start_pos"

    const val FADE_DURATION = 200L

    const val PROFILE_MENU_PENDING = "PROFILE_MENU_PENDING"
    const val PROFILE_MENU_DRAFTS = "PROFILE_MENU_DRAFTS"
    const val PROFILE_MENU_HISTORY = "PROFILE_MENU_HISTORY"
    const val PROFILE_MENU_SAVED = "PROFILE_MENU_SAVED"
    const val PROFILE_MENU_SETTINGS = "PROFILE_MENU_SETTINGS"
}