package com.example.student_api.controller

import com.example.student_api.dto.CreateStudentRequest
import com.example.student_api.dto.UpdateStudentRequest
import com.example.student_api.model.Course
import com.example.student_api.model.Student
import com.example.student_api.repository.CourseRepository
import com.example.student_api.repository.StudentRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
class StudentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var studentRepository: StudentRepository

    @Autowired
    private lateinit var courseRepository: CourseRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var sampleStudents: List<CreateStudentRequest>

    companion object {
        @Container
        private val postgres = PostgreSQLContainer("postgres:latest").apply {
            withDatabaseName("test_db")
            withUsername("test")
            withPassword("test-pass")
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @BeforeEach
    fun setup() {
        studentRepository.deleteAll()
        courseRepository.deleteAll()
        sampleStudents = listOf(
            CreateStudentRequest("Angelo", "angelo@email.com", 23, "CS"),
            CreateStudentRequest("Luna", "luna@email.com", 20, "CS"),
            CreateStudentRequest("Mina", "mina@email.com", 21, "CS")
        )
    }

    private fun jsonOf(obj: Any) = objectMapper.writeValueAsString(obj)

    private fun createCourse(name: String): Course =
        courseRepository.findByNameIgnoreCase(name) ?: courseRepository.save(Course(name = name))

    private fun createStudent(request: CreateStudentRequest) =
        mockMvc.perform(
            post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonOf(request))
        ).andExpect(status().isCreated)
            .andReturn()
            .response
            .let { objectMapper.readTree(it.contentAsString)["student added"] }


    @Test
    fun `POST should create a new student successfully`() {
        val student = sampleStudents.first()
        val created = createStudent(student)

        mockMvc.perform(get("/api/students/${created["id"].asLong()}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value(student.name))
            .andExpect(jsonPath("$.email").value(student.email))
            .andExpect(jsonPath("$.age").value(student.age))
            .andExpect(jsonPath("$.courseName").value(student.courseName))
            .andDo(print())
    }

    @Test
    fun `POST should return 409 for duplicate email`(){

        val student = createStudent(sampleStudents.first())

        mockMvc.perform(
            post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonOf(student))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("Creation failed: A student with the email 'angelo@email.com' already exists."))
            .andDo(print())
    }

    @Test
    fun `POST should return 400 for invalid student`() {
        val invalidStudent = CreateStudentRequest("", "invalid-email", 0, "")
        mockMvc.perform(
            post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonOf(invalidStudent))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
            .andDo(print())
    }

    @Test
    fun `GET should fetch all students with pagination`() {
        sampleStudents.forEach { createStudent(it) }

        mockMvc.perform(
            get("/api/students")
                .param("page", "0")
                .param("size", "2")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.meta.page").value(0))
            .andExpect(jsonPath("$.meta.size").value(2))
            .andExpect(jsonPath("$.meta.totalElements").value(3))
            .andDo(print())
    }

    @Test
    fun `GET should filter students based on parameters`() {
        sampleStudents.forEach { createStudent(it) }

        val student = sampleStudents.first()

        mockMvc.perform(
            get("/api/students")
                .param("name", student.name)
                .param("email", student.email)
                .param("age", student.age.toString())
                .param("courseName", student.courseName)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data[0].name").value(student.name))
            .andExpect(jsonPath("$.data[0].email").value(student.email))
            .andExpect(jsonPath("$.data[0].age").value(student.age))
            .andExpect(jsonPath("$.data[0].courseName").value(student.courseName))
            .andDo(print())
    }

    @Test
    fun `GET should filter students by name only`() {
        sampleStudents.forEach { createStudent(it) }

        mockMvc.perform(
            get("/api/students")
                .param("name", "Angelo")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data[0].name").value("Angelo"))
            .andExpect(jsonPath("$.data[0].age").value(23))
            .andExpect(jsonPath("$.data[0].email").value("angelo@email.com"))
            .andExpect(jsonPath("$.data[0].courseName").value("CS"))
            .andDo(print())
    }

    @Test
    fun `GET should filter students by courseName only`() {
        sampleStudents.forEach { createStudent(it) }

        mockMvc.perform(
            get("/api/students")
                .param("courseName", "CS")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data[0].name").value("Angelo"))
            .andExpect(jsonPath("$.data[0].courseName").value("CS"))
            .andExpect(jsonPath("$.data[1].name").value("Luna"))
            .andExpect(jsonPath("$.data[1].courseName").value("CS"))
            .andExpect(jsonPath("$.data[2].name").value("Mina"))
            .andExpect(jsonPath("$.data[2].courseName").value("CS"))
            .andDo(print())
    }

    @Test
    fun `GET should fetch student by id successfully`() {
        val created = createStudent(sampleStudents.first())
        val id = created["id"].asLong()

        mockMvc.perform(get("/api/students/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value("Angelo"))
            .andExpect(jsonPath("$.email").value("angelo@email.com"))
            .andExpect(jsonPath("$.age").value(23))
            .andExpect(jsonPath("$.courseName").value("CS"))
            .andDo(print())
    }

    @Test
    fun `GET should return 404 for non-existent student`() {
        mockMvc.perform(get("/api/students/999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").exists())
            .andDo(print())
    }

    @Test
    fun `PUT should update student successfully`() {
        val course = createCourse("CS")
        val student = studentRepository.save(Student(name = "Old Name", email =  "old@email.com", age =  22, course = course))

        val updateRequest = UpdateStudentRequest("Angelo", "angelo@email.com", 23, "CS")
        mockMvc.perform(
            put("/api/students/${student.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonOf(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$['Updated student'].name").value(updateRequest.name))
            .andExpect(jsonPath("$['Updated student'].email").value(updateRequest.email))
            .andExpect(jsonPath("$['Updated student'].age").value(updateRequest.age))
            .andExpect(jsonPath("$['Updated student'].courseName").value(updateRequest.courseName))
            .andDo(print())
    }

    @Test
    fun `PUT should return 404 for non-existent student`() {
        val updateRequest = UpdateStudentRequest("Angelo", "angelo@email.com", 23, "CS")
        mockMvc.perform(
            put("/api/students/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonOf(updateRequest))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").exists())
            .andDo(print())
    }

    @Test
    fun `PUT should return 400 for invalid update`() {
        val created = createStudent(sampleStudents.first())
        val id = created["id"].asLong()
        val invalidRequest = UpdateStudentRequest("", "", 0, "")

        mockMvc.perform(
            put("/api/students/$id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonOf(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
            .andDo(print())
    }

    @Test
    fun `PUT should return 409 for duplicate email`(){
        createStudent(sampleStudents.first())
        val studentToUpdate = createStudent(sampleStudents[1])
        val update = UpdateStudentRequest(
            name = "Luna",
            email = "angelo@email.com",
            age = 20,
            courseName = "CS"
        )

        mockMvc.perform(
            put("/api/students/${studentToUpdate["id"].asLong()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonOf(update))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("Creation failed: A student with the email '${update.email}' already exists."))
            .andDo(print())

    }

    @Test
    fun `DELETE should delete student successfully`() {
        val course = createCourse("CS")
        val student = studentRepository.save(Student(name = "Angelo", email = "angelo@email.com", age=23, course =  course))

        mockMvc.perform(delete("/api/students/${student.id}"))
            .andExpect(status().isNoContent)
            .andDo(print())
    }

    @Test
    fun `DELETE should return 404 for non-existent student`() {
        mockMvc.perform(delete("/api/students/999"))
            .andExpect(status().isNotFound)
            .andDo(print())
    }

    @Test
    fun `GET should group students by course successfully`() {
        sampleStudents.forEach { createStudent(it) }

        mockMvc.perform(get("/api/students/courses"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isNotEmpty)
            .andDo(print())
    }

    @Test
    fun `GET should return empty grouping when no students exist`() {
        mockMvc.perform(get("/api/students/courses"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isEmpty)
            .andDo(print())
    }
}
