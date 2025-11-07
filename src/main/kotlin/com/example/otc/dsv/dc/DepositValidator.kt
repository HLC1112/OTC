package com.example.otc.dsv.dc

import com.example.otc.common.doc.ValidationResultDoc
import com.example.otc.dsv.l.AcceptorRules
import com.example.otc.common.lang.OtcBigDecimal

/**
 * 存款校验轻量封装：
 * - 为兼容测试代码保留 `com.example.otc.dsv.dc.DepositValidator` 名称。
 * - 内部委托给 `DC_1.validate`，保持唯一校验逻辑来源。
 */
class DepositValidator {
    private val delegate = DC_1()

    /**
     * 执行存款校验并返回结果文档。
     */
    fun validate(amount: OtcBigDecimal, currency: String, rules: AcceptorRules): ValidationResultDoc {
        return delegate.validate(amount, currency, rules)
    }
}