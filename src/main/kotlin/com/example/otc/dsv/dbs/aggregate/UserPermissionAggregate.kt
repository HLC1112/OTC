package com.example.otc.dsv.dbs.aggregate
/**
 * 用户权限聚合：
 * - 记录用户获得的具体权限以及授予时间，便于审计与查询。
 */

import com.example.otc.common.lang.Otc3

data class UserPermissionAggregate(
    val userId: String,
    val permission: String,
    val grantedAt: Otc3
)