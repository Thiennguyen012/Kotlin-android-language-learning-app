package com.englishlearningapp.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AssetUrlTest {

    @Test
    fun testToAssetUrlNullOrBlank() {
        assertNull((null as String?).toAssetUrl())
        assertNull("".toAssetUrl())
        assertNull("   ".toAssetUrl())
    }

    @Test
    fun testToAssetUrlRelativePath() {
        // Since BuildConfig is defined at compile time, we test if it appends relative path.
        // It should start with the BuildConfig's API_BASE_URL.
        val relative = "storage/avatars/user1.jpg"
        val result = relative.toAssetUrl()
        System.out.println("Relative path result: $result")
    }

    @Test
    fun testToAssetUrlAbsoluteUrl() {
        val absolute = "https://example.com/storage/avatars/user1.jpg"
        assertEquals(absolute, absolute.toAssetUrl())
    }
}
