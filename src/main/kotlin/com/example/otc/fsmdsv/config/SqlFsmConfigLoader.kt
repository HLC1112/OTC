package com.example.otc.fsmdsv.config
/**
 * SQL 状态机配置加载器：
 * - 从 classpath 读取 `config/otc/fsm/<name>`（默认 `FSM_1.sql`）。
 * - 以 `-- step: <name>` 为分段标记，解析为步骤与 SQL 语句列表。
 */

import com.example.otc.common.lang.Otc1
import com.example.otc.common.lang.OtcResourceLoader

data class SqlFsmConfig(val steps: LinkedHashMap<String, List<String>>)

@Otc1
class SqlFsmConfigLoader(
    private val resourceLoader: OtcResourceLoader
) {
    fun load(name: String = "FSM_1.sql"): SqlFsmConfig {
        val res = resourceLoader.getResource("classpath:config/otc/fsm/$name")
        val temp = LinkedHashMap<String, MutableList<String>>()
        if (!res.exists()) return SqlFsmConfig(LinkedHashMap())
        res.inputStream.bufferedReader().use { br ->
            var current: String? = null
            val stepRegex = Regex("^\\s*--\\s*step\\s*:\\s*(.+)\\s*$", RegexOption.IGNORE_CASE)
            br.lineSequence().forEach { line ->
                val m = stepRegex.matchEntire(line)
                if (m != null) {
                    current = m.groupValues[1].trim()
                    temp.putIfAbsent(current!!, mutableListOf())
                } else {
                    if (current != null) temp[current]!!.add(line)
                }
            }
        }
        val steps = LinkedHashMap<String, List<String>>()
        temp.forEach { (k, v) -> steps[k] = v.toList() }
        return SqlFsmConfig(steps)
    }
}