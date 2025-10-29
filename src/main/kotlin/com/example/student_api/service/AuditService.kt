package com.example.student_api.service

import com.example.student_api.model.AuditLog
import com.example.student_api.repository.AuditLogRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Service
class AuditService(private val auditLogRepository: AuditLogRepository) {

    private fun getCurrentRequest(): HttpServletRequest? {
        return (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
    }

    fun logEvent(action: String, details: String, user: String = "system") {
        val request = getCurrentRequest()
        val endpoint = request?.let { "${it.method} ${it.requestURI}" } ?: "N/A"

        val log = AuditLog(
            action = action,
            endpoint = endpoint,
            details = details,
            user = user
        )
        auditLogRepository.save(log)
    }

    fun getAllLogs(): List<AuditLog> = auditLogRepository.findAll()
}
