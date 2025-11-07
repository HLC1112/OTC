package com.example.otc.dsv.da_0.adapter
/**
 * 权限写入适配器：
 * - 将 DA 层 `PermissionWriter` 适配为 FSM 层的 `PermissionWriterPort`。
 * - 以端口形式暴露写入能力，解耦业务编排与数据访问实现。
 */

import com.example.otc.common.evt.PermissionGrantedEvt
import com.example.otc.common.lang.Otc1
import com.example.otc.dsv.da_0.PermissionWriter
import com.example.otc.fsmdsv.ports.PermissionWriterPort

@Otc1
class DaPermissionAdapter(
    private val writer: PermissionWriter
) : PermissionWriterPort {
    override fun grantPermission(applicationId: String, userId: String, permission: String): PermissionGrantedEvt {
        return writer.grantPermission(applicationId, userId, permission)
    }
}