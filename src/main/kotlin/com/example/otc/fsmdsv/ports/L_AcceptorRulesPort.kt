package com.example.otc.fsmdsv.ports
/**
 * 承兑规则端口：
 * - 提供当前有效的承兑规则（最低保证金与允许币种）。
 */

import com.example.otc.dsv.l.AcceptorRules

interface AcceptorRulesPort {
    fun current(): AcceptorRules
}