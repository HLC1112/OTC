package com.example.otc.dsv.dc
/**
 * 存款校验适配器：
 * - 将 DC 层校验实现适配为 FSM 层的 `DepositValidationPort`。
 * - 面向编排层提供统一的校验入口。
 */

import com.example.otc.common.doc.ValidationResultDoc
import com.example.otc.common.lang.OtcBigDecimal
import com.example.otc.common.lang.Otc1
import com.example.otc.fsmdsv.ports.DepositValidationPort
import com.example.otc.dsv.l.AcceptorRules

@Otc1
class DcDepositValidationAdapter(
    private val dc: DC_1
) : DepositValidationPort {
    override fun validate(amount: OtcBigDecimal, currency: String, rules: AcceptorRules): ValidationResultDoc {
        return dc.validate(amount, currency, rules)
    }
}