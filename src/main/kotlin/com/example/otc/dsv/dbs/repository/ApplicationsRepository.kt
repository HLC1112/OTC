package com.example.otc.dsv.dbs.repository

import java.math.BigDecimal

data class ApplicationSnapshot(
    val applicationId: String,
    val userId: String,
    val depositAmount: BigDecimal,
    val depositCurrency: String,
    val status: String
)

interface ApplicationsRepository {
    fun createApplication(
        applicationId: String,
        userId: String,
        depositAmount: BigDecimal,
        depositCurrency: String,
        status: String
    ): Boolean

    fun findSnapshot(applicationId: String): ApplicationSnapshot?

    fun updateStatus(applicationId: String, status: String): Boolean
}