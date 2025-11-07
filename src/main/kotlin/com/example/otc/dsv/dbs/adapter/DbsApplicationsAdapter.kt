package com.example.otc.dsv.dbs.adapter
/**
 * 应用仓储适配器：
 * - 将具体的 `ApplicationsRepository` 实现适配为 FSM 层的 `ApplicationRepositoryPort`。
 * - 负责创建申请与更新状态的持久化操作。
 */

import com.example.otc.common.lang.OtcBigDecimal
import com.example.otc.common.lang.Otc1
import com.example.otc.dsv.dbs.repository.ApplicationsRepository
import com.example.otc.fsmdsv.ports.ApplicationRepositoryPort

@Otc1
class DbsApplicationsAdapter(
    private val repo: ApplicationsRepository
) : ApplicationRepositoryPort {
    override fun createApplication(
        applicationId: String,
        userId: String,
        depositAmount: OtcBigDecimal,
        depositCurrency: String,
        status: String
    ): Boolean {
        return repo.createApplication(applicationId, userId, depositAmount, depositCurrency, status)
    }

    override fun updateStatus(applicationId: String, status: String): Boolean {
        return repo.updateStatus(applicationId, status)
    }
}