package com.example.otc.dat2.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("application_approvals")
data class ApplicationApprovalDoc(
    @Id val id: String,
    val applicationId: String,
    val userId: String,
    val status: String,
    val permission: String?,
    val approvedAt: Instant,
    val updatedAt: Instant
)

@Document("user_permissions")
data class UserPermissionDoc(
    @Id val id: String,
    val userId: String,
    val permission: String,
    val grantedAt: Instant,
    val updatedAt: Instant
)