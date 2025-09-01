package com.furkan.sanayi.error

import com.furkan.sanayi.common.exceptions.InvalidRequestException
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class GlobalExceptionHandler {

    /**
     * Bean Validation (@Valid) hataları.
     * Aynı alan için birden fazla kural ihlalinde hepsini array olarak döndürüyoruz.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun onValidException(ex: MethodArgumentNotValidException): Map<String, Map<String, List<String>>> {
        val errors: Map<String, List<String>> = ex.bindingResult
            .fieldErrors
            .groupBy(FieldError::getField)
            .mapValues { entry -> entry.value.mapNotNull { it.defaultMessage } }

        return mapOf("errors" to errors)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArg(ex: IllegalArgumentException) =
        ResponseEntity.badRequest().body(mapOf("error" to ex.message))

    /**
     * DB constraint ihlalleri (ör: unique index).
     * Postgres mesajı ham olarak dönmek yerine, index adına göre özelleştirilmiş mesaj üretiyoruz.
     */
    @ExceptionHandler(DataIntegrityViolationException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun onDataIntegrity(ex: DataIntegrityViolationException): Map<String, String> {
        val rootMsg = ex.rootCause?.message ?: ex.message ?: ""

        val friendlyMsg = when {
            rootMsg.contains("uq_rating_user", ignoreCase = true) ->
                "Bu sağlayıcıyı zaten oyladınız. (kullanıcı)"

            rootMsg.contains("uq_rating_anon", ignoreCase = true) ->
                "Bu sağlayıcıyı zaten oyladınız. (anonim)"

            else ->
                "Veri bütünlüğü hatası: ${rootMsg.take(200)}"
        }
        return mapOf("error" to friendlyMsg, "error_detail" to rootMsg.take(1000))
    }

    /**
     * Custom business validation hataları (ör: userId ve anonymousId aynı anda/null).
     */
    @ExceptionHandler(InvalidRequestException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun onInvalidRequest(ex: InvalidRequestException): Map<String, String> =
        mapOf("error" to (ex.message ?: "Invalid request"))

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onConstraintViolation(ex: ConstraintViolationException): Map<String, List<String>> {
        val msg = ex.constraintViolations.mapNotNull { it.message }
        return mapOf("errors" to msg)
    }

    @ExceptionHandler(value = [NoSuchElementException::class, EntityNotFoundException::class, IllegalStateException::class])
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onNotFound(ex: Exception): Map<String, String> =
        mapOf("error" to (ex.message ?: "Not found"))

}