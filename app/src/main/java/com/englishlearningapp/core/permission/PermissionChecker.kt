package com.englishlearningapp.core.permission

import com.englishlearningapp.data.remote.dto.auth.UserDto

class PermissionHelper(
    private val user: UserDto?
) {
    fun checkPermission(permissionName: String): Boolean {
        val currentUser = user ?: return false
        if (currentUser.isSuperAdmin) return true

        return currentUser.permissions.any { permission ->
            permission.permissionName == permissionName
        }
    }
}
