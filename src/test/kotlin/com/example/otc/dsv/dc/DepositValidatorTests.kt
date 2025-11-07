package com.example.otc.dsv.dc

import com.example.otc.common.error.ErrorCodes
import com.example.otc.common.error.OtcException
import com.example.otc.dsv.l.AcceptorRules
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class DepositValidatorTests {
    private val validator = DepositValidator()

    @Test
    fun `金额不足`() {
        val rules = AcceptorRules(BigDecimal(1000), listOf("USDT", "BTC"))
        val ex = assertThrows(OtcException::class.java) {
            validator.validate(BigDecimal(999), "USDT", rules)
        }
        assertEquals(ErrorCodes.AMOUNT_INSUFFICIENT.code, ex.error.code)
    }

    @Test
    fun `币种不允许`() {
        val rules = AcceptorRules(BigDecimal(1000), listOf("USDT", "BTC"))
        val ex = assertThrows(OtcException::class.java) {
            validator.validate(BigDecimal(1500), "ETH", rules)
        }
        assertEquals(ErrorCodes.CURRENCY_NOT_ALLOWED.code, ex.error.code)
    }

    @Test
    fun `全部通过`() {
        val rules = AcceptorRules(BigDecimal(1000), listOf("USDT", "BTC"))
        val doc = validator.validate(BigDecimal(1500), "USDT", rules)
        assertEquals(true, doc.valid)
    }
}