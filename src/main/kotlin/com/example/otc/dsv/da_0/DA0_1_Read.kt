package com.example.otc.dsv.da_0

import com.example.otc.common.doc.ApplicationDataSnapshotDoc
import com.example.otc.dsv.dbs.repository.ApplicationsRepository
import com.example.otc.infra.log.DistributedLogger
import org.springframework.stereotype.Service

@Service
class DA0_1_Read(
    private val applicationsRepository: ApplicationsRepository,
    private val distributedLogger: DistributedLogger
) {
    fun getSnapshot(applicationId: String): ApplicationDataSnapshotDoc? {
        distributedLogger.info("DA0_1_Read", "Reading snapshot for ${applicationId}", applicationId)
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
