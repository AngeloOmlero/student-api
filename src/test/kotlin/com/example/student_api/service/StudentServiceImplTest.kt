package com.example.student_api.service

import com.example.student_api.dto.CreateStudentRequest
import com.example.student_api.dto.UpdateStudentRequest
import com.example.student_api.exception.StudentNotFoundException
import com.example.student_api.model.Course
import com.example.student_api.model.Student
import com.example.student_api.repository.CourseRepository
import com.example.student_api.repository.StudentRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

class StudentServiceImplTest {
    private val studentRepository: StudentRepository = mockk()
    private val courseRepository: CourseRepository = mockk()
    private val auditService: AuditService = mockk(relaxed = true)
    private val studentService = StudentServiceImpl(studentRepository,courseRepository,auditService)

    private val courseCS = Course(id = 1, name = "CS")
    private val courseIT = Course(id = 2, name = "IT")
    private val student = Student(
        id = 1,
        name = "Angelo",
        email = "angelo@gmail.com",
        course = courseCS
    )

    @Test
    fun `should create new student when course exists`(){
        val request = CreateStudentRequest("Angelo","angelo@gmail.com",23,"CS")
        every { courseRepository.findByNameIgnoreCase(request.courseName) } returns courseCS
        every { studentRepository.save(any()) }  answers {firstArg()}

        val result = studentService.save(request)

        assertEquals("Angelo",result.name)
        assertEquals("CS",result.courseName)
        verify {
            auditService.logEvent("CREATE_STUDENT", any(),eq("system"))
            studentRepository.save(any())
        }


    }

    @Test
    fun `should create new student and new course when course does not exist`(){
        val request = CreateStudentRequest("Luna","luna@email.com",20,"BSCPE")
        val newCourse = Course(id = 3, name = "BSCPE")

        every { courseRepository.findByNameIgnoreCase("BSCPE") } returns null
        every { courseRepository.save(any()) } returns Course(id = 3, name = "BSCPE")
        every { studentRepository.save(any()) } answers {firstArg()}
        every { auditService.logEvent(any(), any(), any()) } just Runs

        val result = studentService.save(request)

        assertEquals("Luna", result.name)
        assertEquals("BSCPE",result.courseName)
        verifySequence {
            courseRepository.findByNameIgnoreCase("BSCPE")
            courseRepository.save(any())
            studentRepository.save(any())
            auditService.logEvent("CREATE_STUDENT",any(),eq("system"))
        }

    }


    @Test
    fun `should fetch paginated student list`(){
        val pageable = PageRequest.of(0,5)
        val student = listOf(student)

        every { studentRepository.findAll(any(),pageable) } returns PageImpl(student)

        val result = studentService.getAll(mockk(relaxed = true),pageable)
        assertEquals(1,result.totalElements)
        assertEquals("Angelo", result.content.first().name)
        verify { auditService.logEvent("GET_ALL_STUDENTS",any(),eq("system")) }
    }

    @Test
    fun `should find student by ID`(){
        every { studentRepository.findById(1L) } returns Optional.of(student)

        val result = studentService.findById(1L)

        assertEquals("Angelo",result.name)
        assertEquals("angelo@gmail.com",result.email)
    }

    @Test
    fun `should throw StudentNotFoundException when student ID does not exist`(){
        every { studentRepository.findById(99L) } returns Optional.empty()

        val ex = assertThrows<StudentNotFoundException> {
            studentService.findById(99L)
        }
        assertTrue { ex.message!!.contains("not found") }

    }


    @Test
    fun `should group students by course name`() {
        val csStudent = student
        val itStudent = Student(id = 2, name = "Alex", age = 21, email = "alex@example.com", course = courseIT)

        every { studentRepository.findAll() } returns listOf(csStudent, itStudent)

        val grouped = studentService.groupByCourse()

        assertEquals(2, grouped.size)
        assertTrue(grouped.containsKey("CS"))
        assertTrue(grouped.containsKey("IT"))
        verify { auditService.logEvent("GROUP_BY_COURSE", any()) }
    }
    @Test
    fun `should update student when found`() {
        val request = UpdateStudentRequest(
            name = "Angelo Updated",
            age = 21,
            email = "angelo.updated@example.com",
            courseName = "IT"
        )

        every { studentRepository.findById(1L) } returns Optional.of(student)
        every { courseRepository.findByNameIgnoreCase("IT") } returns courseIT
        every { studentRepository.save(any()) } answers { firstArg() }

        val result = studentService.update(1L, request)

        assertEquals("Angelo Updated", result.name)
        assertEquals("IT", result.courseName)
        verify {
            auditService.logEvent("UPDATE_STUDENT", any())
            studentRepository.save(any())
        }
    }

    @Test
    fun `should create new course if not found when updating student`() {
        val request = UpdateStudentRequest(
            name = "Mina",
            age = 22,
            email = "mina@example.com",
            courseName = "BSCE"
        )

        every { studentRepository.findById(1L) } returns Optional.of(student)
        every { courseRepository.findByNameIgnoreCase("BSCE") } returns null
        every { courseRepository.save(any()) } returns Course(id = 4, name = "BSCE")
        every { studentRepository.save(any()) } answers { firstArg() }

        val result = studentService.update(1L, request)

        assertEquals("BSCE", result.courseName)
        verifySequence {
            studentRepository.findById(1L)
            courseRepository.findByNameIgnoreCase("BSCE")
            courseRepository.save(any())
            studentRepository.save(any())
        }
    }

    @Test
    fun `should throw StudentNotFoundException when updating non-existent student`() {
        val request = UpdateStudentRequest(
            name = "Ghost",
            age = 25,
            email = "ghost@example.com",
            courseName = "IT"
        )

        every { studentRepository.findById(100L) } returns Optional.empty()

        val ex = assertThrows<StudentNotFoundException> {
            studentService.update(100L, request)
        }

        assertTrue(ex.message!!.contains("not found"))
        verify(exactly = 0) { studentRepository.save(any()) }
    }
    @Test
    fun `should delete existing student`() {
        every { studentRepository.findById(1L) } returns Optional.of(student)
        every { studentRepository.deleteById(1L) } just Runs

        studentService.delete(1L)

        verify {
            auditService.logEvent("DELETE_STUDENT", any())
            studentRepository.deleteById(1L)
        }
    }

    @Test
    fun `should throw StudentNotFoundException when deleting non-existent student`() {
        every { studentRepository.findById(999L) } returns Optional.empty()

        val ex = assertThrows<StudentNotFoundException> {
            studentService.delete(999L)
        }

        assertTrue(ex.message!!.contains("not found"))
        verify(exactly = 0) { studentRepository.deleteById(any()) }
    }

}