package com.englishlearningapp.core.permission

import com.englishlearningapp.data.remote.dto.auth.PermissionDto
import com.englishlearningapp.data.remote.dto.auth.UserDto
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionCheckerTest {

    @Test
    fun superAdminBypassesPermissionCheck() {
        val permissionHelper = PermissionHelper(createUser(isSuperAdmin = true))

        assertTrue(permissionHelper.checkPermission("permission.not_assigned"))
    }

    @Test
    fun regularUserCanAccessAssignedPermission() {
        val permissionHelper = PermissionHelper(
            createUser(
                permissions = listOf(
                    PermissionDto(id = 1, permissionName = "user.view")
                )
            )
        )

        assertTrue(permissionHelper.checkPermission("user.view"))
    }

    @Test
    fun regularUserCannotAccessUnassignedPermission() {
        val permissionHelper = PermissionHelper(createUser())

        assertFalse(permissionHelper.checkPermission("user.view"))
    }

    @Test
    fun missingUserCannotAccessPermission() {
        val permissionHelper = PermissionHelper(null)

        assertFalse(permissionHelper.checkPermission("user.view"))
    }

    private fun createUser(
        isSuperAdmin: Boolean = false,
        permissions: List<PermissionDto> = emptyList()
    ) = UserDto(
        id = 1,
        name = "Test User",
        phone = null,
        email = "test@example.com",
        birthday = null,
        address = null,
        avatar = null,
        status = 1,
        isSuperAdmin = isSuperAdmin,
        permissions = permissions
    )
}
