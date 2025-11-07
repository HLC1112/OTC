package com.example.otc.dsv.dbs.repository
/**
 * 用户权限仓储接口：
 * - 定义权限授予的持久化操作，返回布尔值表示写入是否成功。
 */

interface UserPermissionsRepository {
    fun grant(userId: String, permission: String): Boolean
}