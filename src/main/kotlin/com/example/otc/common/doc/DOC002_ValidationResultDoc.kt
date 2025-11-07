package com.example.otc.common.doc

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ValidationResultDoc(
    @JsonProperty("applicationId") val applicationId: String,
    @JsonProperty("valid") val valid: Boolean,
    @JsonProperty("errorCode") val errorCode: String? = null,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("details") val details: Map<String, Any?> = emptyMap()
)