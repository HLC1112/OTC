package com.example.otc.dsv.da_0
/**
 * DA 层写入服务（包含 Outbox 模式）：
 * - 授权用户权限并更新申请状态，在同一事务内写入 Outbox 事件。
 * - 失败时抛出统一的业务异常码，便于上层 FSM 做失败分支处理。
 */

import com.example.otc.common.doc.ApplicationApprovedDoc
import com.example.otc.common.evt.PermissionGrantedEvt
import com.example.otc.common.error.ErrorCodes
import com.example.otc.common.error.OtcException
import com.example.otc.dat1.entity.OutboxEvtEntity
import com.example.otc.dat1.mysql.OutboxEvtJpa
import com.example.otc.dsv.dbs.repository.ApplicationsRepository
import com.example.otc.dsv.dbs.repository.UserPermissionsRepository
import com.example.otc.common.lang.OtcObjectMapper
import com.example.otc.common.lang.Otc10
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.example.otc.common.lang.Otc3
import com.example.otc.common.lang.Otc4

/**
 * DA 层写操作：通过数据库访问执行权限授予，并采用 Outbox 模式保证事件可靠投递。
 */
@Service
class PermissionWriter(
    private val applicationsRepository: ApplicationsRepository,
    private val userPermissionsRepository: UserPermissionsRepository,
    private val outboxJpa: OutboxEvtJpa,
    private val objectMapper: OtcObjectMapper
) {
    private val logger = Otc10.getLogger(PermissionWriter::class.java)

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
                grantedAt = Otc3.now()
            )

            val approvedDoc = ApplicationApprovedDoc(
                applicationId = applicationId,
                userId = userId,
                approvedAt = grantedEvt.grantedAt,
                details = mapOf("permission" to permission)
            )

            // Outbox 写入：与权限授予处于同一事务，确保一致性
            val outbox = OutboxEvtEntity(
                id = Otc4.randomUUID().toString(),
                aggregateType = "Application",
                aggregateId = applicationId,
                eventType = "ApplicationApprovedDoc",
                payloadJson = objectMapper.writeValueAsString(approvedDoc),
                createdAt = Otc3.now(),
                sentAt = null
            )
            outboxJpa.save(outbox)
            logger.info("[DA0] Permission granted and outbox written. appId={}", applicationId)
            return grantedEvt
        } catch (ex: OtcException) {
            throw ex
        } catch (ex: Exception) {
            logger.error("[DA0] Permission write failed: {}", ex.message)
            throw OtcException(ErrorCodes.PERMISSION_WRITE_FAILED, ex)
        }
    }
}