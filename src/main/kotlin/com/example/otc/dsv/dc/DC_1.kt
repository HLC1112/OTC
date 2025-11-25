package com.example.otc.dsv.dc

import com.example.otc.common.doc.ValidationResultDoc
import com.example.otc.common.error.ErrorCodes
import com.example.otc.common.error.OtcException
import com.example.otc.dsv.l.AcceptorRules
import com.example.otc.infra.log.DistributedLogger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DC_1(
    private val distributedLogger: DistributedLogger
) {
    private val logger = LoggerFactory.getLogger(DC_1::class.java)

    fun validate(applicationId: String, traceId: String?, amount: BigDecimal, currency: String, rules: AcceptorRules): ValidationResultDoc {
        if (amount < rules.minDepositAmount) {
            distributedLogger.info("DC_1", "deposit amount insufficient: amount=${amount} min=${rules.minDepositAmount} currency=${currency}", applicationId)
            throw OtcException(ErrorCodes.AMOUNT_INSUFFICIENT)
        }
        if (!rules.allowedCurrencies.contains(currency.uppercase())) {
            distributedLogger.info("DC_1", "currency not allowed: ${currency} allowed=${rules.allowedCurrencies}", applicationId)
            throw OtcException(ErrorCodes.CURRENCY_NOT_ALLOWED)
        }
        distributedLogger.info("DC_1", "Validation passed for currency=${currency}", traceId)
        return ValidationResultDoc(applicationId = "", valid = true, details = mapOf("amount" to amount, "currency" to currency))
    }
}
