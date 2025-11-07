package com.example.otc.dsv.da_0
/**
 * DA 层读取服务：
 * - 访问应用仓储，获取申请的快照数据并包装为文档对象。
 * - 统一对外提供读取接口，隐藏底层存储细节。
 */

import com.example.otc.common.doc.ApplicationDataSnapshotDoc
import com.example.otc.dsv.dbs.repository.ApplicationsRepository
import org.springframework.stereotype.Service

@Service
class DA0_1_Read(
    private val applicationsRepository: ApplicationsRepository
) {
    fun getSnapshot(applicationId: String): ApplicationDataSnapshotDoc? {
        val snap = applicationsRepository.findSnapshot(applicationId) ?: return null
        return ApplicationDataSnapshotDoc(
            applicationId = snap.applicationId,
            userId = snap.userId,
            depositAmount = snap.depositAmount,
            depositCurrency = snap.depositCurrency,
            status = snap.status,
            details = mapOf("source" to "mysql")
        )
    }
}