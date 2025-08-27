package com.furkan.sanayi.error

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                mapOf(
                    "error" to "validation",
                    "details" to ex.bindingResult.fieldErrors.map { it.field to it.defaultMessage })
            )

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArg(ex: IllegalArgumentException) =
        ResponseEntity.badRequest().body(mapOf("error" to ex.message))

    @ExceptionHandler(DataIntegrityViolationException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun onDataIntegrity(ex: DataIntegrityViolationException) =
        mapOf("error" to (ex.rootCause?.message ?: "Veri bütünlüğü hatası"))
}