package com.hamburghini.cosmos.core.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object DownloadUtils {
    suspend fun downloadImageToDownloads(
        context: Context,
        imageUrl: String,
        fileName: String,
        appFolderName: String
    ): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()

                val request = Request.Builder()
                    .url(imageUrl.replace("&amp;", "&"))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null

                    val body = response.body ?: return@withContext null
                    val bytes = body.bytes()
                    val mimeType = body.contentType()?.toString()
                        ?: "image/jpeg"

                    saveBytesToDownloads(
                        context = context,
                        bytes = bytes,
                        mimeType = mimeType,
                        fileName = fileName,
                        appFolderName = appFolderName
                    )
                }
            } catch (e: Exception) {
                Logger.e("Download failed", e)
                null
            }
        }
    }

    fun saveBytesToDownloads(
        context: Context,
        bytes: ByteArray,
        mimeType: String,
        fileName: String,
        appFolderName: String
    ): Uri? {
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?: mimeType.substringAfter("/", "jpg")

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.$extension")
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${Environment.DIRECTORY_DOWNLOADS}/$appFolderName"
            )
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            values
        ) ?: return null

        resolver.openOutputStream(uri)?.use {
            it.write(bytes)
        }

        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        return uri
    }


    fun openImage(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
}