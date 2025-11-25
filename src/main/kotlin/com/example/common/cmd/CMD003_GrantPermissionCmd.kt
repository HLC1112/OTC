package com.example.otc.common.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class GrantPermissionCmd(
    @JsonProperty("applicationId") val applicationId: String,
    @JsonProperty("userId") val userId: String,
    @JsonProperty("permission") val permission: String
)