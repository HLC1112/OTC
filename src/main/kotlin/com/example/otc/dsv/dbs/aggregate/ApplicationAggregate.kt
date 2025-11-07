package com.example.otc.dsv.dbs.aggregate
/**
 * 申请聚合模型：
 * - 描述申请在生命周期中的状态与允许的状态迁移。
 * - 强制执行不变式，防止从终止状态回退或非法迁移。
 */

import com.example.otc.common.lang.OtcBigDecimal

enum class ApplicationStatus {
    Requested,
    ValidatingDeposit,
    GrantingPermission,
    Approved,
    Failed
}

data class ApplicationAggregate(
    val id: String,
    val userId: String,
    val depositAmount: OtcBigDecimal,
    val depositCurrency: String,
    var status: ApplicationStatus
) {
    fun transitionTo(newStatus: ApplicationStatus) {
        // 基本不变式：从终止状态（Approved/Failed）不可回退到其他状态
        if (status == ApplicationStatus.Approved || status == ApplicationStatus.Failed) {
            throw IllegalStateException("Cannot transition from terminal state $status to $newStatus")
        }
        // 允许的状态迁移集合
        val allowed = when (status) {
            ApplicationStatus.Requested -> setOf(ApplicationStatus.ValidatingDeposit)
            ApplicationStatus.ValidatingDeposit -> setOf(ApplicationStatus.GrantingPermission, ApplicationStatus.Failed)
            ApplicationStatus.GrantingPermission -> setOf(ApplicationStatus.Approved, ApplicationStatus.Failed)
            else -> emptySet()
        }
        require(newStatus in allowed) { "Invalid transition: $status -> $newStatus" }
        status = newStatus
    }
}