package com.example.otc.common.doc

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApplicationApprovedDoc(
    @JsonProperty("applicationId") val applicationId: String,
    @JsonProperty("userId") val userId: String,
    @JsonProperty("approvedAt") val approvedAt: Instant,
    @JsonProperty("details") val details: Map<String, Any?> = emptyMap()
)