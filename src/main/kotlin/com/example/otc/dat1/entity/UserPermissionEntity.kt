package com.example.otc.dat1.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "T_USER_PERMISSION")
class UserPermissionEntity(
    @Id
    var id: String = "",

    @Column(name = "user_id", nullable = false)
    var userId: String = "",

    @Column(name = "permission", nullable = false)
    var permission: String = "",

    @Column(name = "granted_at", nullable = false)
    var grantedAt: Instant = Instant.now()
)