package com.example.otc.dsv.fsm

import com.example.otc.common.cmd.ApplyForAcceptorCmd
import com.example.otc.common.cmd.GrantPermissionCmd
import com.example.otc.common.cmd.ValidateDepositCmd
import com.example.otc.common.doc.ValidationResultDoc
import com.example.otc.common.evt.PermissionGrantFailedEvt
import com.example.otc.common.evt.PermissionGrantedEvt
import com.example.otc.common.error.ErrorCodes
import com.example.otc.common.error.OtcException
import com.example.otc.dsv.dbs.aggregate.ApplicationStatus
import com.example.otc.fsmdsv.config.SqlFsmConfigLoader
import com.example.otc.fsmdsv.ports.AcceptorRulesPort
import com.example.otc.fsmdsv.ports.ApplicationRepositoryPort
import com.example.otc.fsmdsv.ports.DepositValidationPort
import com.example.otc.fsmdsv.ports.PermissionWriterPort
import com.example.otc.common.lang.Otc10
import com.example.otc.common.lang.Otc2
import com.example.otc.common.lang.Otc1
import com.example.otc.common.lang.Otc3
import com.example.otc.common.lang.Otc4
import com.example.otc.common.lang.Otc5
import com.example.otc.common.lang.Otc6
import com.example.otc.common.lang.Otc7
import com.example.otc.common.lang.Otc8
import com.example.otc.common.lang.Otc9

/**
 * FSM_1（FSM001）流程：
 * link-001 申请受理 → link-002 校验保证金 → link-003 发送校验命令
 * → link-004 接收校验结果文档 → link-005 进入权限授予 → link-006 发送授予命令
 * → link-007 接收“权限已授予”事件 → link-008 通过 Outbox 发布“申请已批准”文档
 * 失败分支：超时 → E.OTC.5002；写入错误 → PermissionGrantFailed + E.OTC.5001
 */
@Otc1
class FSM_1(
    private val applicationsRepository: ApplicationRepositoryPort,
    private val validator: DepositValidationPort,
    private val rulesProvider: AcceptorRulesPort,
    private val permissionWriter: PermissionWriterPort,
    private val sqlFsmConfigLoader: SqlFsmConfigLoader,
    @Otc2("\${fsm.grant_permission.timeout_ms:30000}") private val permissionTimeoutMs: Long
) {
    private val logger = Otc10.getLogger(FSM_1::class.java)
    private val exec = Otc7.newFixedThreadPool(2)
    private val plan by lazy { sqlFsmConfigLoader.load("FSM_1.sql") }

    fun apply(cmd: ApplyForAcceptorCmd): String {
        if (plan.steps.isNotEmpty()) {
            logger.info("[FSM] SQL plan loaded steps={}", plan.steps.keys.joinToString(","))
        }
        // Step: ApplyReq
        val applicationId = Otc4.randomUUID().toString()
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
            validator.validate(vcmd.amount, vcmd.currency, rulesProvider.current())
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
        val fut = Otc5.supplyAsync({
            permissionWriter.grantPermission(gcmd.applicationId, gcmd.userId, gcmd.permission)
        }, exec)

        val grantedEvt: PermissionGrantedEvt = try {
            fut.get(permissionTimeoutMs, Otc8.MILLISECONDS)
        } catch (ex: Otc9) {
            fut.cancel(true)
            val evt = PermissionGrantFailedEvt(
                applicationId = applicationId,
                userId = cmd.userId,
                permission = gcmd.permission,
                errorCode = ErrorCodes.PERMISSION_TIMEOUT.code,
                message = ErrorCodes.PERMISSION_TIMEOUT.message,
                occurredAt = Otc3.now()
            )
            logger.error("[FSM] Permission timeout appId={} code={}", applicationId, evt.errorCode)
            applicationsRepository.updateStatus(applicationId, ApplicationStatus.Failed.name)
            throw OtcException(ErrorCodes.PERMISSION_TIMEOUT, ex)
        } catch (ex: Otc6) {
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

        // Step: ApplicationApproved(doc) - published via Outbox by PermissionWriter
        logger.info(
            "[FSM] ApplicationApproved via Outbox appId={} userId={} permission={}",
            applicationId,
            cmd.userId,
            grantedEvt.permission
        )

        return applicationId
    }
}