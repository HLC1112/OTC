package com.example.otc.common.evt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PermissionGrantedEvt(
    @JsonProperty("applicationId") val applicationId: String,
    @JsonProperty("userId") val userId: String,
    @JsonProperty("permission") val permission: String,
    @JsonProperty("grantedAt") val grantedAt: Instant
)