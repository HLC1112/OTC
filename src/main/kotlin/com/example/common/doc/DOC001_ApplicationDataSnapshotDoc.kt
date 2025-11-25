package com.example.otc.common.doc

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApplicationDataSnapshotDoc(
    @JsonProperty("applicationId") val applicationId: String,
    @JsonProperty("userId") val userId: String,
    @JsonProperty("depositAmount") val depositAmount: BigDecimal,
    @JsonProperty("depositCurrency") val depositCurrency: String,
    @JsonProperty("status") val status: String,
    @JsonProperty("details") val details: Map<String, Any?> = emptyMap()
)