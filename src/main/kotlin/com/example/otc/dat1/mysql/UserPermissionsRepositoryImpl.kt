package com.example.otc.dat1.mysql

import com.example.otc.dat1.entity.UserPermissionEntity
import com.example.otc.dsv.dbs.repository.UserPermissionsRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Repository
class UserPermissionsRepositoryImpl(
    private val userPermissionJpa: UserPermissionJpa
) : UserPermissionsRepository {

    private val logger = LoggerFactory.getLogger(UserPermissionsRepositoryImpl::class.java)

    @Transactional
    override fun grant(userId: String, permission: String): Boolean {
        val entity = UserPermissionEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            permission = permission,
            grantedAt = Instant.now()
        )
        userPermissionJpa.save(entity)
        logger.info("[Repo] grant permission: userId={} permission={}", userId, permission)
        return true
    }
}