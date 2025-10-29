package com.example.student_api.controller

import com.example.student_api.dto.mapper.GenericResponse
import com.example.student_api.model.AuditLog
import com.example.student_api.service.AuditService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/audit")
class AuditController(private val auditService: AuditService) {

    @GetMapping("/logs")
    fun getAuditLogs(): ResponseEntity<GenericResponse<List<AuditLog>>> {
        val logs = auditService.getAllLogs()
        val response = GenericResponse(
            status = HttpStatus.OK.value(),
            message = "Audit logs retrieved successfully",
            data = logs
        )
        return ResponseEntity.ok(response)
    }
}
