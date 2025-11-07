package com.example.otc.common.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApplyForAcceptorCmd(
    @JsonProperty("userId") val userId: String,
    @JsonProperty("depositAmount") val depositAmount: BigDecimal,
    @JsonProperty("depositCurrency") val depositCurrency: String
)