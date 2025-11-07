package com.example.otc.dsv.l.adapter
/**
 * 承兑规则适配器：
 * - 将 L 层的 `L_AcceptorRules` 适配为 FSM 层的 `AcceptorRulesPort`。
 * - 屏蔽规则来源与刷新机制，面向编排层提供稳定接口。
 */

import com.example.otc.common.lang.Otc1
import com.example.otc.fsmdsv.ports.AcceptorRulesPort
import com.example.otc.dsv.l.L_AcceptorRules
import com.example.otc.dsv.l.AcceptorRules

@Otc1
class LRulesAdapter(
    private val rules: L_AcceptorRules
) : AcceptorRulesPort {
    override fun current(): AcceptorRules = rules.current()
}