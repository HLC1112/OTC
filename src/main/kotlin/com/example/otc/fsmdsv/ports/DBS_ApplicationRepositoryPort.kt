package com.example.otc.fsmdsv.ports
/**
 * 应用仓储端口：
 * - 定义创建申请与更新状态的持久化操作，供 FSM 使用。
 */

import com.example.otc.common.lang.OtcBigDecimal

interface ApplicationRepositoryPort {
    fun createApplication(
        applicationId: String,
        userId: String,
        depositAmount: OtcBigDecimal,
        depositCurrency: String,
        status: String
    ): Boolean

    fun updateStatus(applicationId: String, status: String): Boolean
}