package com.example.student_api.controller

import com.example.student_api.dto.CreateStudentRequest
import com.example.student_api.dto.UpdateStudentRequest
import com.example.student_api.dto.mapper.toPageResponse
import com.example.student_api.repository.StudentFilter
import com.example.student_api.service.StudentService
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import org.apache.commons.logging.LogFactory
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins = ["http://localhost:5173", "http://127.0.0.1:5500"])
@RestController
@RequestMapping("/api/students")
@Validated

class StudentController(private val service: StudentService) {
    private val logs = LoggerFactory.getLogger(StudentController::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun save(@Valid @RequestBody request: CreateStudentRequest) =
        logs.info("POST /students - Request: $request").let {
            val student = service.save(request)
            logs.info("POST /students - Response: ${request.name} added")
            student
        }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAll(
        @ModelAttribute
        @Parameter(hidden = true) filter: StudentFilter,
        @PageableDefault(sort = ["id"],
            direction = Sort.Direction.DESC) pageable: Pageable
    ) = logs.info("GET /students - Filter: $filter, Pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}").let {
        val result = service.getAll(filter, pageable).toPageResponse()
        logs.info("GET /students - Fetched ${result.meta.totalElements} students")
        result
    }


    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getById(@PathVariable id: Long) =
        logs.info("GET /students/$id").let {
            val student = service.findById(id)
            logs.info("GET /students/$id - Response: $student")
            student
        }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateStudentRequest) =
        logs.info("PUT /students/$id - Request: $request").let {
            val updated = service.update(id, request)
            logs.info("PUT /students/$id - Response: $updated")
            updated
        }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) =
        logs.info("DELETE /students/$id").let {
            service.delete(id)
            logs.info("DELETE /students/$id - Completed")
        }

    @GetMapping("/courses")
    @ResponseStatus(HttpStatus.OK)
    fun groupByCourse() =
        logs.info("GET /students/courses").let {
            val grouped = service.groupByCourse()
            logs.info("GET /students/courses - Grouped ${grouped.size} courses")
            grouped
        }


}