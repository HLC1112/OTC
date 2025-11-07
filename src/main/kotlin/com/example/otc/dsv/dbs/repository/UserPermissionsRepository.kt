package com.example.otc.dsv.dbs.repository

interface UserPermissionsRepository {
    fun grant(userId: String, permission: String): Boolean
}