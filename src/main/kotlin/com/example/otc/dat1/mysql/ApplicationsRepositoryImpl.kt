package com.example.otc.dat1.mysql

import com.example.otc.dat1.entity.MerchantApplicationEntity
import com.example.otc.dsv.dbs.repository.ApplicationSnapshot
import com.example.otc.dsv.dbs.repository.ApplicationsRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

@Repository
class ApplicationsRepositoryImpl(
    private val appJpa: MerchantApplicationJpa
) : ApplicationsRepository {

    private val logger = LoggerFactory.getLogger(ApplicationsRepositoryImpl::class.java)

    @Transactional
    override fun createApplication(
        applicationId: String,
        userId: String,
        depositAmount: BigDecimal,
        depositCurrency: String,
        status: String
    ): Boolean {
        val entity = MerchantApplicationEntity(
            id = applicationId,
            userId = userId,
            status = status,
            depositAmount = depositAmount,
            depositCurrency = depositCurrency,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        appJpa.save(entity)
        logger.info("[Repo] create application: id={} status={}", applicationId, status)
        return true
    }

    override fun findSnapshot(applicationId: String): ApplicationSnapshot? {
        val entity = appJpa.findById(applicationId).orElse(null) ?: return null
        return ApplicationSnapshot(
            applicationId = entity.id,
            userId = entity.userId,
            depositAmount = entity.depositAmount,
            depositCurrency = entity.depositCurrency,
            status = entity.status
        )
    }

    @Transactional
    override fun updateStatus(applicationId: String, status: String): Boolean {
        val entity = appJpa.findById(applicationId).orElse(null) ?: return false
        entity.status = status
        entity.updatedAt = Instant.now()
        appJpa.save(entity)
        logger.info("[Repo] update status: id={} status={}", applicationId, status)
        return true
    }
}