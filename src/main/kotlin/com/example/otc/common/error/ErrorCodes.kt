package com.example.otc.common.error

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorCode(val code: String, val http: HttpStatus, val message: String)

object ErrorCodes {
    val AMOUNT_INSUFFICIENT = ErrorCode("E.OTC.4001", HttpStatus.BAD_REQUEST, "金额不足")
    val CURRENCY_NOT_ALLOWED = ErrorCode("E.OTC.4002", HttpStatus.BAD_REQUEST, "币种不允许")
    val PERMISSION_WRITE_FAILED = ErrorCode("E.OTC.5001", HttpStatus.INTERNAL_SERVER_ERROR, "授权写库失败")
    val PERMISSION_TIMEOUT = ErrorCode("E.OTC.5002", HttpStatus.GATEWAY_TIMEOUT, "授权超时")

    fun resolve(code: String): ErrorCode = listOf(
        AMOUNT_INSUFFICIENT, CURRENCY_NOT_ALLOWED, PERMISSION_WRITE_FAILED, PERMISSION_TIMEOUT
    ).firstOrNull { it.code == code } ?: ErrorCode(code, HttpStatus.INTERNAL_SERVER_ERROR, "未知错误")
}

class OtcException(val error: ErrorCode, cause: Throwable? = null) : RuntimeException(error.message, cause)

data class ErrorResponse(val code: String, val message: String)

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(OtcException::class)
    fun handle(e: OtcException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(e.error.http).body(ErrorResponse(e.error.code, e.error.message))
    }
}