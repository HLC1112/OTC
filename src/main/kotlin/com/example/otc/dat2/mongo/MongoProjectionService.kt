package com.example.otc.dat2.mongo

import com.example.otc.common.doc.ApplicationApprovedDoc
import com.example.otc.common.lang.Otc3
import com.example.otc.common.lang.OtcMongoTemplate
import com.example.otc.common.lang.Otc10
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

@Service
class MongoProjectionService(
    private val mongoTemplate: OtcMongoTemplate
) {
    private val logger = Otc10.getLogger(MongoProjectionService::class.java)

    fun projectApproved(doc: ApplicationApprovedDoc) {
        val permission = doc.details["permission"]?.toString()
        upsertApplicationApproval(doc, permission)
        if (permission != null) {
            upsertUserPermission(doc.userId, permission, doc.approvedAt)
        }
    }

    private fun upsertApplicationApproval(doc: ApplicationApprovedDoc, permission: String?) {
        val query = Query.query(Criteria.where("_id").`is`(doc.applicationId))
        val update = Update()
            .setOnInsert("applicationId", doc.applicationId)
            .setOnInsert("userId", doc.userId)
            .set("status", "Approved")
            .set("permission", permission)
            .set("approvedAt", doc.approvedAt)
            .set("updatedAt", Otc3.now())

        val result = mongoTemplate.upsert(query, update, ApplicationApprovalDoc::class.java)
        logger.info("[Projection] upsert application_approvals id={} matched={} modified={}", doc.applicationId, result.matchedCount, result.modifiedCount)
    }

    private fun upsertUserPermission(userId: String, permission: String, grantedAt: Otc3) {
        val id = "$userId:$permission"
        val query = Query.query(Criteria.where("_id").`is`(id))
        val update = Update()
            .setOnInsert("userId", userId)
            .setOnInsert("permission", permission)
            .set("grantedAt", grantedAt)
            .set("updatedAt", Otc3.now())

        val result = mongoTemplate.upsert(query, update, UserPermissionDoc::class.java)
        logger.info("[Projection] upsert user_permissions id={} matched={} modified={}", id, result.matchedCount, result.modifiedCount)
    }
}