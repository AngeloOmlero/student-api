package com.example.student_api.service.integration

import com.example.student_api.dto.CreateStudentRequest
import com.example.student_api.model.Course
import com.example.student_api.repository.CourseRepository
import com.example.student_api.repository.StudentRepository
import com.example.student_api.service.StudentServiceImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals

@SpringBootTest
@Testcontainers
@Transactional
class StudentServiceImplIntegrationTest{
    companion object {
        @Container
        private val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("student_db_test")
            withUsername("test_user")
            withPassword("test_pass")
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Autowired
    private lateinit var studentService: StudentServiceImpl

    @Autowired
    private lateinit var studentRepository: StudentRepository

    @Autowired
    private lateinit var courseRepository: CourseRepository

    @BeforeEach
    fun setup() {
        studentRepository.deleteAll()
        courseRepository.deleteAll()
    }
    @Test
    fun `should create new student`(){
        val existingCourse = courseRepository.save(Course(name = "BSIT"))

        val request = CreateStudentRequest(
            name = "Luna",
            email = "luna@email.com",
            age = 21,
            courseName = "BSIT"
        )

        val result = studentService.save(request)

        assertNotNull(result.id)
        assertEquals("Luna", result.name)
        assertEquals("BSIT", result.courseName)

        val studentInDb = studentRepository.findAll().first()
        assertEquals(existingCourse.id, studentInDb.course?.id)
    }
}