package com.example.otc.common.lang

// Centralized type aliases for standard library classes used across DSV modules.
// Keep aliases scoped to domain layer to balance naming preferences and readability.

typealias Otc3 = java.time.Instant
typealias Otc4 = java.util.UUID
typealias OtcDuration = java.time.Duration
typealias OtcLocalDateTime = java.time.LocalDateTime
typealias OtcLocalDate = java.time.LocalDate
typealias OtcBigDecimal = java.math.BigDecimal

typealias Otc5<T> = java.util.concurrent.CompletableFuture<T>
typealias Otc6 = java.util.concurrent.ExecutionException
typealias Otc7 = java.util.concurrent.Executors
typealias Otc8 = java.util.concurrent.TimeUnit
typealias Otc9 = java.util.concurrent.TimeoutException
typealias OtcAtomicLong = java.util.concurrent.atomic.AtomicLong
typealias OtcAtomicReference<T> = java.util.concurrent.atomic.AtomicReference<T>

typealias OtcKafkaTemplate<K, V> = org.springframework.kafka.core.KafkaTemplate<K, V>
typealias OtcObjectMapper = com.fasterxml.jackson.databind.ObjectMapper
typealias OtcMongoTemplate = org.springframework.data.mongodb.core.MongoTemplate
typealias OtcResourceLoader = org.springframework.core.io.ResourceLoader
typealias OtcResponseEntity<T> = org.springframework.http.ResponseEntity<T>
typealias Otc10 = org.slf4j.LoggerFactory

// Annotation aliases (Kotlin-only usage). Compiles to native annotations, preserving Spring scanning.
typealias Otc1 = org.springframework.stereotype.Service
typealias Otc2 = org.springframework.beans.factory.annotation.Value