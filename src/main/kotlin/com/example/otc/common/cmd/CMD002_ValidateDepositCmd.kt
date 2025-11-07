package com.example.otc.common.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ValidateDepositCmd(
    @JsonProperty("applicationId") val applicationId: String,
    @JsonProperty("amount") val amount: BigDecimal,
    @JsonProperty("currency") val currency: String
)