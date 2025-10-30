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
    private lateinit var studentService: StudentServiceImpl

    private lateinit var courseCS: Course
    private lateinit var courseIT: Course
    private lateinit var student: Student

    companion object{
        const val EVENT_CREATE = "CREATE_STUDENT"
        const val EVENT_DELETE = "DELETE_STUDENT"
        const val EVENT_UPDATE = "UPDATE_STUDENT"
        const val EVENT_GET_ALL = "GET_ALL_STUDENTS"
        const val EVENT_GROUP = "GROUP_BY_COURSE"
    }


    @BeforeEach
    fun setup(){
        studentService = StudentServiceImpl(studentRepository,courseRepository,auditService)
        courseCS = Course(id = 1, name = "CS")
        courseIT = Course(id = 2, name = "IT")
        student = Student(
            id = 1,
            name = "Angelo",
            email = "angelo@gmail.com",
            course = courseCS
        )
    }

    @Test
    fun `should create new student when course exists`(){
        val request = CreateStudentRequest("Angelo","angelo@gmail.com",23,"CS")
        every { courseRepository.findByNameIgnoreCase(request.courseName) } returns courseCS
        every { studentRepository.save(any()) }  returnsArgument 0

        val result = studentService.save(request)

        assertEquals("Angelo",result.name)
        assertEquals("CS",result.courseName)
        verify {
            auditService.logEvent(EVENT_CREATE, any(),eq("system"))
            studentRepository.save(any())
        }


    }

    @Test
    fun `should create new student and new course when course does not exist`(){
        val request = CreateStudentRequest("Luna","luna@email.com",20,"BSCPE")


        every { courseRepository.findByNameIgnoreCase(request.courseName) } returns null
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
            auditService.logEvent(EVENT_CREATE,any(),eq("system"))
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
        verify { auditService.logEvent(EVENT_GET_ALL,any(),eq("system")) }
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
        verify { auditService.logEvent(EVENT_GROUP, any()) }
    }
    @Test
    fun `should update student when found`() {
        val request = UpdateStudentRequest("Angelo","angelonew@gmail.com",23,"IT")

        every { studentRepository.findById(1L) } returns Optional.of(student)
        every { courseRepository.findByNameIgnoreCase("IT") } returns courseIT
        every { studentRepository.save(any()) } answers { firstArg() }

        val result = studentService.update(1L, request)

        assertEquals("Angelo", result.name)
        assertEquals("IT", result.courseName)
        verify {
            auditService.logEvent(EVENT_UPDATE, any())
            studentRepository.save(any())
        }
    }

    @Test
    fun `should create new course if not found when updating student`() {
        val request = UpdateStudentRequest("Mina","mina@email.com",21,"BSCE")

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
    fun `should throw StudentNotFoundException when updating non-existent student by id`() {
        val request = UpdateStudentRequest("Ghost","ghost@email.com",22,"BSCPE")

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
            auditService.logEvent(EVENT_DELETE, any())
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