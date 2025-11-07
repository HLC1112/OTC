package com.example.otc.dsv.dbs.aggregate

import java.time.Instant

data class UserPermissionAggregate(
    val userId: String,
    val permission: String,
    val grantedAt: Instant
)