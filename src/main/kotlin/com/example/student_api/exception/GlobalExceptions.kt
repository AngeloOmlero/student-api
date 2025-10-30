package com.example.student_api.exception

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.lang.RuntimeException
import java.time.LocalDateTime


class StudentNotFoundException(message: String) : RuntimeException(message)



@ControllerAdvice
class GlobalExceptions {

    @ExceptionHandler(StudentNotFoundException::class)
    fun handleNotFound(ex : StudentNotFoundException)=
        errorResponse(HttpStatus.NOT_FOUND,ex.message?:"Student Not Found")


    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return errorResponse(HttpStatus.BAD_REQUEST, "Validation Error", errors)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<Map<String, Any>> =
        errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.message ?: "Unexpected error occurred")

    @ExceptionHandler(BadCredentialsException::class)
    fun handleCredentials(ex: BadCredentialsException) =
        errorResponse(HttpStatus.UNAUTHORIZED,ex.message?:"Invalid Credentials")

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<Map<String, Any>> {
        val message = ex.rootCause?.message?.let { detail ->
            Regex("\\(email\\)=\\((.*?)\\)").find(detail)?.groups?.get(1)?.value
                ?.let { "Creation failed: A student with the email '$it' already exists." }
        } ?: "Invalid data provided. Please check input requirements."

        val status = if ("duplicate key value violates unique constraint" in (ex.rootCause?.message ?: ""))
            HttpStatus.CONFLICT else HttpStatus.BAD_REQUEST

        return errorResponse(status, message)
    }
    private fun errorResponse(
        status: HttpStatus,
        message: String,
        details:Any? = null
    ): ResponseEntity<Map<String, Any>>{
        val body = mutableMapOf<String, Any>(
            "timestamp" to LocalDateTime.now(),
            "error" to status.reasonPhrase,
            "status" to status.value(),
            "message" to message
        )
        details?.let { body["details"]= it }
        return ResponseEntity(body,status)
    }

}