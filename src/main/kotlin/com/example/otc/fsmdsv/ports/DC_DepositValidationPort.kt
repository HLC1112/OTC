package com.example.otc.fsmdsv.ports
/**
 * 存款校验端口：
 * - 基于承兑规则校验保证金金额与币种，并返回结果文档。
 */

import com.example.otc.common.doc.ValidationResultDoc
import com.example.otc.common.lang.OtcBigDecimal
import com.example.otc.dsv.l.AcceptorRules

interface DepositValidationPort {
    fun validate(amount: OtcBigDecimal, currency: String, rules: AcceptorRules): ValidationResultDoc
}