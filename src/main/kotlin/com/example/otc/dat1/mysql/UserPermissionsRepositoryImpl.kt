package com.example.otc.dat1.mysql

import com.example.otc.dat1.entity.UserPermissionEntity
import com.example.otc.dsv.dbs.repository.UserPermissionsRepository
import com.example.otc.common.lang.Otc10
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import com.example.otc.common.lang.Otc3
import com.example.otc.common.lang.Otc4

@Repository
class UserPermissionsRepositoryImpl(
    private val userPermissionJpa: UserPermissionJpa
) : UserPermissionsRepository {

    private val logger = Otc10.getLogger(UserPermissionsRepositoryImpl::class.java)

    @Transactional
    override fun grant(userId: String, permission: String): Boolean {
        val entity = UserPermissionEntity(
            id = Otc4.randomUUID().toString(),
            userId = userId,
            permission = permission,
            grantedAt = Otc3.now()
        )
        userPermissionJpa.save(entity)
        logger.info("[Repo] grant permission: userId={} permission={}", userId, permission)
        return true
    }
}