package com.englishlearningapp.core.network

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap

/**
 * Extension function to safely resolve file display name and extension from a Uri.
 * Handles content:// and other Uri schemes.
 */
fun Uri.getFileName(context: Context): String {
    val mimeType = context.contentResolver.getType(this)
    val extension = mimeType?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
    
    var fileName: String? = null
    
    // Attempt to get the name from the ContentResolver
    if (scheme == "content") {
        try {
            context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        fileName = cursor.getString(index)
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to basic Uri parsing on error
        }
    }
    
    // Fallback to last path segment if database query fails or didn't return a name
    if (fileName.isNullOrBlank()) {
        fileName = lastPathSegment?.substringAfterLast('/')
    }
    
    // Ultimate fallback if no name was resolved
    if (fileName.isNullOrBlank()) {
        fileName = "avatar"
    }
    
    // Ensure we have a valid extension if we found one from the mime type
    val dotIndex = fileName.lastIndexOf('.')
    val hasExtension = dotIndex != -1 && dotIndex < fileName.length - 1
    if (!hasExtension && extension != null) {
        fileName = "$fileName.$extension"
    }
    
    return fileName
}
