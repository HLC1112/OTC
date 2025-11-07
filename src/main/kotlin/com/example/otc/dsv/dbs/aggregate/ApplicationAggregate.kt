package com.example.otc.dsv.dbs.aggregate

import java.math.BigDecimal

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
    val depositAmount: BigDecimal,
    val depositCurrency: String,
    var status: ApplicationStatus
) {
    fun transitionTo(newStatus: ApplicationStatus) {
        // basic invariant: no backward transitions from terminal states
        if (status == ApplicationStatus.Approved || status == ApplicationStatus.Failed) {
            throw IllegalStateException("Cannot transition from terminal state $status to $newStatus")
        }
        // allowed transitions
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