package com.example.otc.dsv.dbs.repository
/**
 * 应用仓储接口：
 * - 定义申请数据的持久化操作（创建、更新状态、查询快照）。
 * - `ApplicationSnapshot` 用于对外暴露只读视图，避免直接暴露实体实现。
 */

import com.example.otc.common.lang.OtcBigDecimal

data class ApplicationSnapshot(
    val applicationId: String,
    val userId: String,
    val depositAmount: OtcBigDecimal,
    val depositCurrency: String,
    val status: String
)

interface ApplicationsRepository {
    fun createApplication(
        applicationId: String,
        userId: String,
        depositAmount: OtcBigDecimal,
        depositCurrency: String,
        status: String
    ): Boolean

    fun findSnapshot(applicationId: String): ApplicationSnapshot?

    fun updateStatus(applicationId: String, status: String): Boolean
}