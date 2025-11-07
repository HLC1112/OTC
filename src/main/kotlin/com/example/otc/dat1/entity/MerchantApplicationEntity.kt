package com.example.otc.dat1.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import com.example.otc.common.lang.OtcBigDecimal
import com.example.otc.common.lang.Otc3

@Entity
@Table(name = "T_MERCHANT_APPLICATION")
class MerchantApplicationEntity(
    @Id
    var id: String = "",

    @Column(name = "user_id", nullable = false)
    var userId: String = "",

    @Column(name = "status", nullable = false)
    var status: String = "",

    @Column(name = "deposit_amount", nullable = false, precision = 19, scale = 4)
    var depositAmount: OtcBigDecimal = OtcBigDecimal.ZERO,

    @Column(name = "deposit_currency", nullable = false)
    var depositCurrency: String = "",

    @Column(name = "created_at", nullable = false)
    var createdAt: Otc3 = Otc3.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Otc3 = Otc3.now()
)