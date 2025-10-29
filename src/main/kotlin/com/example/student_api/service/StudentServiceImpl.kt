package com.example.student_api.service

import com.example.student_api.dto.CreateStudentRequest
import com.example.student_api.dto.mapper.StudentDTO
import com.example.student_api.dto.UpdateStudentRequest
import com.example.student_api.dto.mapper.toDTO
import com.example.student_api.dto.toCreate
import com.example.student_api.dto.toUpdate
import com.example.student_api.exception.StudentNotFoundException
import com.example.student_api.model.Course
import com.example.student_api.repository.CourseRepository
import com.example.student_api.repository.StudentFilter
import com.example.student_api.repository.StudentRepository
import com.example.student_api.repository.StudentSpecification
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class StudentServiceImpl(
    private val studentRepository: StudentRepository,
    private val courseRepository: CourseRepository,
    private val auditService: AuditService
    ) : StudentService {

    private val logs = LoggerFactory.getLogger(StudentServiceImpl::class.java)

    @Transactional
    override fun save(request: CreateStudentRequest) : StudentDTO{

        val course = courseRepository.findByNameIgnoreCase(request.courseName)
            ?: courseRepository.save(Course(name = request.courseName))
        
        val student = request.toCreate(course)
        val savedStudent = studentRepository.save(student)
        auditService.logEvent("CREATE_STUDENT", "Student created with ID: ${savedStudent.id}")
        logs.info("Response: Student added: ${request.name}")
        return savedStudent.toDTO()

    }

    override fun getAll(filter: StudentFilter, pageable: Pageable): Page<StudentDTO> {
        auditService.logEvent("GET_ALL_STUDENTS", "Student fetched  list with filter: $filter")
        logs.info("Request: Fetching students with filter: $filter, pageable: page=${pageable.pageNumber}, size=${pageable.pageSize}")
        val result = studentRepository.findAll(
            StudentSpecification.filter(filter),
            pageable
        ).map { it.toDTO() }

        logs.info("Response: Fetched ${result.totalElements} students")

        return result
    }

    override fun findById(id:Long): StudentDTO {
        val student = studentRepository.findById(id)
            .orElseThrow {
                logs.warn("Student with ID $id not found")
                StudentNotFoundException("Student with ID $id not found")
            }
        logs.info("Student with ID $id found")
          return  student.toDTO()
    }

    override fun groupByCourse(): Map<String?, List<StudentDTO>> {
        val grouped = studentRepository.findAll()
            .groupBy { it.course?.name }
            .mapValues { (_, students) -> students.map { it.toDTO() } }
        auditService.logEvent("GROUP_BY_COURSE", "Student grouped students by course")
        logs.info("Grouped students by course")
        return grouped
    }

    @Transactional
    override fun update(id:Long,request: UpdateStudentRequest) : StudentDTO{

        val existingStudent = studentRepository.findById(id)
            .orElseThrow {
                logs.warn("Response: Student with ID $id not found for update")
                StudentNotFoundException("Student with ID $id not found for update")
            }

        val course = courseRepository.findByNameIgnoreCase(request.courseName)
            ?: courseRepository.save(Course(name = request.courseName))

        val updatedStudent = request.toUpdate(existingStudent,course)
        val savedStudent = studentRepository.save(updatedStudent)
        auditService.logEvent("UPDATE_STUDENT", "Student updated with ID: ${savedStudent.id}")
        logs.info("Response: Student updated: $id")
        return savedStudent.toDTO()

    }

    @Transactional
    override fun delete(id:Long){
         studentRepository.findById(id)
            .orElseThrow {
                logs.warn("Student with ID $id not found for update")
                StudentNotFoundException("Student with ID $id not found for update")
            }
        studentRepository.deleteById(id)
        auditService.logEvent("DELETE_STUDENT", "Student deleted with ID: $id")
        logs.info("Student with ID $id successfully deleted")
    }

}