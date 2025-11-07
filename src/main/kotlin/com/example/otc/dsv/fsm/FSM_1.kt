package com.example.otc.dsv.fsm

import com.example.otc.common.cmd.ApplyForAcceptorCmd
import com.example.otc.common.cmd.GrantPermissionCmd
import com.example.otc.common.cmd.ValidateDepositCmd
import com.example.otc.common.doc.ApplicationApprovedDoc
import com.example.otc.common.doc.ValidationResultDoc
import com.example.otc.common.evt.PermissionGrantFailedEvt
import com.example.otc.common.evt.PermissionGrantedEvt
import com.example.otc.common.error.ErrorCodes
import com.example.otc.common.error.OtcException
import com.example.otc.dsv.adapter.EventBusPublisher
import com.example.otc.dsv.dc.DC_1
import com.example.otc.dsv.da_0.PermissionWriter
import com.example.otc.dsv.dbs.aggregate.ApplicationStatus
import com.example.otc.dsv.dbs.repository.ApplicationsRepository
import com.example.otc.dsv.l.L_AcceptorRules
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * FSM_1 (FSM001):
 * link-001 ApplyReq → link-002 ValidatingDeposit → link-003 ValidateDeposit(cmd)
 * → link-004 ValidationResult(doc) → link-005 GrantingPermission → link-006 GrantPermission(cmd)
 * → link-007 PermissionGranted(evt) → link-008 ApplicationApproved(doc)
 * failure branches: timeout → E.OTC.5002; write error → PermissionGrantFailed + E.OTC.5001
 */
@Service
class FSM_1(
    private val applicationsRepository: ApplicationsRepository,
    private val DC1: DC_1,
    private val rules: L_AcceptorRules,
    private val permissionWriter: PermissionWriter,
    private val eventBus: EventBusPublisher,
    @Value("\${fsm.grant_permission.timeout_ms:30000}") private val permissionTimeoutMs: Long
) {
    private val logger = LoggerFactory.getLogger(FSM_1::class.java)
    private val exec = Executors.newFixedThreadPool(2)

    fun apply(cmd: ApplyForAcceptorCmd): String {
        // Step: ApplyReq
        val applicationId = UUID.randomUUID().toString()
        applicationsRepository.createApplication(
            applicationId = applicationId,
            userId = cmd.userId,
            depositAmount = cmd.depositAmount,
            depositCurrency = cmd.depositCurrency,
            status = ApplicationStatus.Requested.name
        )
        logger.info("[FSM] ApplyReq accepted appId={} userId={}", applicationId, cmd.userId)

        // Step: ValidatingDeposit
        applicationsRepository.updateStatus(applicationId, ApplicationStatus.ValidatingDeposit.name)
        logger.info("[FSM] ValidatingDeposit appId={}", applicationId)

        // Step: ValidateDeposit(cmd)
        val vcmd = ValidateDepositCmd(applicationId, cmd.depositAmount, cmd.depositCurrency)
        val validationDoc: ValidationResultDoc = try {
            DC1.validate(vcmd.amount, vcmd.currency, rules.current())
                .copy(applicationId = applicationId)
        } catch (ex: OtcException) {
            val doc = ValidationResultDoc(
                applicationId = applicationId,
                valid = false,
                errorCode = ex.error.code,
                message = ex.error.message,
                details = mapOf("amount" to vcmd.amount, "currency" to vcmd.currency)
            )
            logger.info("[FSM] ValidationResult: invalid code={} appId={}", ex.error.code, applicationId)
            applicationsRepository.updateStatus(applicationId, ApplicationStatus.Failed.name)
            throw ex
        }
        logger.info("[FSM] ValidationResult valid appId={}", applicationId)

        // Step: GrantingPermission
        applicationsRepository.updateStatus(applicationId, ApplicationStatus.GrantingPermission.name)
        logger.info("[FSM] GrantingPermission appId={}", applicationId)

        val gcmd = GrantPermissionCmd(applicationId, cmd.userId, permission = "OTC_ACCEPTOR")

        // Step: GrantPermission(cmd) with timeout
        val fut = CompletableFuture.supplyAsync({
            permissionWriter.grantPermission(gcmd.applicationId, gcmd.userId, gcmd.permission)
        }, exec)

        val grantedEvt: PermissionGrantedEvt = try {
            fut.get(permissionTimeoutMs, TimeUnit.MILLISECONDS)
        } catch (ex: TimeoutException) {
            fut.cancel(true)
            val evt = PermissionGrantFailedEvt(
                applicationId = applicationId,
                userId = cmd.userId,
                permission = gcmd.permission,
                errorCode = ErrorCodes.PERMISSION_TIMEOUT.code,
                message = ErrorCodes.PERMISSION_TIMEOUT.message,
                occurredAt = Instant.now()
            )
            logger.error("[FSM] Permission timeout appId={} code={}", applicationId, evt.errorCode)
            applicationsRepository.updateStatus(applicationId, ApplicationStatus.Failed.name)
            throw OtcException(ErrorCodes.PERMISSION_TIMEOUT, ex)
        } catch (ex: ExecutionException) {
            fut.cancel(true)
            applicationsRepository.updateStatus(applicationId, ApplicationStatus.Failed.name)
            val cause = ex.cause
            if (cause is OtcException) {
                logger.error("[FSM] Permission write failed appId={} code={}", applicationId, cause.error.code)
                throw cause
            } else {
                logger.error("[FSM] Permission write failed appId={} cause={}"
                    , applicationId, cause?.message)
                throw OtcException(ErrorCodes.PERMISSION_WRITE_FAILED, cause)
            }
        } catch (ex: InterruptedException) {
            fut.cancel(true)
            Thread.currentThread().interrupt()
            applicationsRepository.updateStatus(applicationId, ApplicationStatus.Failed.name)
            logger.error("[FSM] Permission interrupted appId={}", applicationId)
            throw OtcException(ErrorCodes.PERMISSION_TIMEOUT, ex)
        }

        // Step: PermissionGranted(evt)
        logger.info("[FSM] PermissionGranted appId={} userId={} permission={} at={}",
            grantedEvt.applicationId, grantedEvt.userId, grantedEvt.permission, grantedEvt.grantedAt)

        // Step: ApplicationApproved(doc)
        val approvedDoc = ApplicationApprovedDoc(
            applicationId = applicationId,
            userId = cmd.userId,
            approvedAt = grantedEvt.grantedAt,
            details = mapOf("validated" to true, "permission" to grantedEvt.permission)
        )
        eventBus.publishApplicationApproved(approvedDoc)
        logger.info("[FSM] ApplicationApproved appId={} userId={}", applicationId, cmd.userId)

        return applicationId
    }
}