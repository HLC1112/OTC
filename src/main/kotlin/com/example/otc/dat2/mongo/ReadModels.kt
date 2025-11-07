package com.example.otc.dat2.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import com.example.otc.common.lang.Otc3

@Document("application_approvals")
data class ApplicationApprovalDoc(
    @Id val id: String,
    val applicationId: String,
    val userId: String,
    val status: String,
    val permission: String?,
    val approvedAt: Otc3,
    val updatedAt: Otc3
)

@Document("user_permissions")
data class UserPermissionDoc(
    @Id val id: String,
    val userId: String,
    val permission: String,
    val grantedAt: Otc3,
    val updatedAt: Otc3
)