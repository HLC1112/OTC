package com.example.otc.dsv.dc

import com.example.otc.common.doc.ValidationResultDoc
import com.example.otc.common.error.ErrorCodes
import com.example.otc.common.error.OtcException
import com.example.otc.dsv.l.AcceptorRules
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DC_1 {
    private val logger = LoggerFactory.getLogger(DC_1::class.java)

    fun validate(amount: BigDecimal, currency: String, rules: AcceptorRules): ValidationResultDoc {
        if (amount < rules.minDepositAmount) {
            logger.info("[DC] deposit amount insufficient: amount={} min={} currency={}", amount, rules.minDepositAmount, currency)
            throw OtcException(ErrorCodes.AMOUNT_INSUFFICIENT)
        }
        if (!rules.allowedCurrencies.contains(currency.uppercase())) {
            logger.info("[DC] currency not allowed: {} allowed={}", currency, rules.allowedCurrencies)
            throw OtcException(ErrorCodes.CURRENCY_NOT_ALLOWED)
        }
        return ValidationResultDoc(applicationId = "", valid = true, details = mapOf("amount" to amount, "currency" to currency))
    }
}