package com.example.otc.fsmdsv.ports
/**
 * 权限写入端口：
 * - 授予用户指定权限，并返回 `PermissionGrantedEvt` 以供编排层后续处理。
 */

import com.example.otc.common.evt.PermissionGrantedEvt

interface PermissionWriterPort {
    fun grantPermission(applicationId: String, userId: String, permission: String): PermissionGrantedEvt
}