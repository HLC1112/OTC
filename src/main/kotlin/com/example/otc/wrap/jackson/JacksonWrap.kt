package com.example.otc.wrap.jackson
/**
 * Jackson 封装 V1：
 * - 保留默认配置；后续可集中设置模块与序列化策略的扩展点。
 */

import com.example.otc.common.lang.OtcObjectMapper

object JacksonWrapV1 {
    // 试点封装：保留默认配置，后续可集中设置模块与策略
    fun configure(mapper: OtcObjectMapper): OtcObjectMapper {
        return mapper
    }
}