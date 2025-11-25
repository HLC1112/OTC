package com.example.otc.dsv.da_0

import com.example.otc.common.doc.ApplicationApprovedDoc
import com.example.otc.common.evt.PermissionGrantedEvt
import com.example.otc.common.error.ErrorCodes
import com.example.otc.common.error.OtcException
import com.example.otc.dat1.entity.OutboxEvtEntity
import com.example.otc.dat1.mysql.OutboxEvtJpa
import com.example.otc.dsv.dbs.repository.ApplicationsRepository
import com.example.otc.dsv.dbs.repository.UserPermissionsRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.example.otc.infra.log.DistributedLogger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * DA0 - performs write operations with DB access, using Outbox pattern.
 */
@Service
class PermissionWriter(
    private val applicationsRepository: ApplicationsRepository,
    private val userPermissionsRepository: UserPermissionsRepository,
    private val outboxJpa: OutboxEvtJpa,
    private val objectMapper: ObjectMapper,
    private val distributedLogger: DistributedLogger
) {
    private val logger = LoggerFactory.getLogger(PermissionWriter::class.java)

    @Transactional
    fun grantPermission(applicationId: String, userId: String, permission: String): PermissionGrantedEvt {
        try {
            val ok = userPermissionsRepository.grant(userId, permission)
            if (!ok) throw OtcException(ErrorCodes.PERMISSION_WRITE_FAILED)
            val updOk = applicationsRepository.updateStatus(applicationId, "Approved")
            if (!updOk) throw OtcException(ErrorCodes.PERMISSION_WRITE_FAILED)

            val grantedEvt = PermissionGrantedEvt(
                applicationId = applicationId,
                userId = userId,
                permission = permission,
                grantedAt = Instant.now()
            )

            val approvedDoc = ApplicationApprovedDoc(
                applicationId = applicationId,
                userId = userId,
                approvedAt = grantedEvt.grantedAt,
                details = mapOf("permission" to permission)
            )

            // Outbox write in same TX
            val outbox = OutboxEvtEntity(
                id = UUID.randomUUID().toString(),
                aggregateType = "Application",
                aggregateId = applicationId,
                eventType = "ApplicationApprovedDoc",
                payloadJson = objectMapper.writeValueAsString(approvedDoc),
                createdAt = Instant.now(),
                sentAt = null
            )
            outboxJpa.save(outbox)
            distributedLogger.info("PermissionWriter", "Permission granted and outbox written. appId=${applicationId}", applicationId)
            return grantedEvt
        } catch (ex: OtcException) {
            throw ex
        } catch (ex: Exception) {
            logger.error("[DA0] Permission write failed: {}", ex.message)
            throw OtcException(ErrorCodes.PERMISSION_WRITE_FAILED, ex)
        }
    }
}
