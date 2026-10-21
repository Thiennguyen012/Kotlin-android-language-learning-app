package com.englishlearningapp.core.network

import com.englishlearningapp.BuildConfig

fun String?.toAssetUrl(): String? {
    var path = this?.trim().orEmpty()
    if (path.isBlank()) return null
    
    // If it starts with http/https, we check if it points to localhost/127.0.0.1
    // and replace it with the base URL domain so that emulator/physical devices can access it.
    if (path.startsWith("http://") || path.startsWith("https://")) {
        val baseUri = try {
            android.net.Uri.parse(BuildConfig.API_BASE_URL)
        } catch (e: Exception) {
            null
        }
        if (baseUri != null) {
            val baseHost = baseUri.host
            val basePort = baseUri.port
            
            // Replace localhost/127.0.0.1 with the configured base URL host & port
            if (path.contains("://localhost") || path.contains("://127.0.0.1")) {
                val replacement = if (basePort != -1) "$baseHost:$basePort" else baseHost.orEmpty()
                path = path.replace("localhost:8000", replacement)
                    .replace("localhost", replacement)
                    .replace("127.0.0.1:8000", replacement)
                    .replace("127.0.0.1", replacement)
            }
        }
        return path
    }
    
    val cleanPath = path.trimStart('/')
    val finalPath = if (!cleanPath.startsWith("storage/", ignoreCase = true)) {
        "storage/$cleanPath"
    } else {
        cleanPath
    }
    
    return BuildConfig.API_BASE_URL.trimEnd('/') + "/" + finalPath
}
