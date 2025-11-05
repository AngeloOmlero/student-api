package com.example.student_api.service

import com.example.student_api.dto.CreateStudentRequest
import com.example.student_api.dto.UpdateStudentRequest
import com.example.student_api.exception.StudentNotFoundException
import com.example.student_api.model.Course
import com.example.student_api.model.Student
import com.example.student_api.repository.CourseRepository
import com.example.student_api.repository.StudentFilter
import com.example.student_api.repository.StudentRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import java.util.*

class StudentServiceImplTest {
    private val studentRepository: StudentRepository = mockk()
    private val courseRepository: CourseRepository = mockk()
    private val auditService: AuditService = mockk(relaxed = true)
    private lateinit var studentService: StudentServiceImpl

    private lateinit var courseCS: Course
    private lateinit var courseIT: Course
    private lateinit var students: List<Student>

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
        students = listOf(
            Student(id = 1, name = "Angelo", email = "angelo@gmail.com", age = 23, course = courseCS),
            Student(id = 2, name = "Luna", email = "luna@email.com", age = 20, course = courseIT)
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


        every { studentRepository.findAll(any(),pageable) } returns PageImpl(students)

        val result = studentService.getAll(mockk(relaxed = true),pageable)
        assertEquals(2,result.totalElements)
        assertEquals("Angelo", result.content.first().name)
        verify { auditService.logEvent(EVENT_GET_ALL,any(),eq("system")) }
    }

    @Test
    fun `should find student by ID`(){
        every { studentRepository.findById(1L) } returns Optional.of(students.first())

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
    fun `should filter student by name`(){

        val filterParams = StudentFilter(name = "Angelo")

        every { studentRepository.findAll(any<Specification<Student>>(), any<Pageable>()) } answers {

            val filtered = students.filter {
                it.name.contains(filterParams.name!!, ignoreCase = true)
            }
            PageImpl(filtered, secondArg(), filtered.size.toLong())
        }

        val result = studentService.getAll(filterParams, PageRequest.of(0, 10))

        assertEquals(1, result.totalElements)
        assertEquals("Angelo", result.content.first().name)
        verify { auditService.logEvent(EVENT_GET_ALL, any(), eq("system")) }
    }

    @Test
    fun `should filter student by age`(){

        val filterParams = StudentFilter(age = 23)

        every { studentRepository.findAll(any<Specification<Student>>(), any<Pageable>()) } answers {

            val filtered = students.filter {
                it.age.equals(filterParams.age!!)
            }
            PageImpl(filtered, secondArg(), filtered.size.toLong())
        }

        val result = studentService.getAll(filterParams, PageRequest.of(0, 10))

        assertEquals(1, result.totalElements)
        assertEquals(23, result.content.first().age)
        verify { auditService.logEvent(EVENT_GET_ALL, any(), eq("system")) }
    }

    @Test
    fun `should filter student by email`(){

        val filterParams = StudentFilter(email = "angelo@gmail.com")

        every { studentRepository.findAll(any<Specification<Student>>(), any<Pageable>()) } answers {

            val filtered = students.filter {
                it.email.contains(filterParams.email!!, ignoreCase = true)
            }
            PageImpl(filtered, secondArg(), filtered.size.toLong())
        }

        val result = studentService.getAll(filterParams, PageRequest.of(0, 10))

        assertEquals(1, result.totalElements)
        assertEquals("angelo@gmail.com", result.content.first().email)
        verify { auditService.logEvent(EVENT_GET_ALL, any(), eq("system")) }
    }

    @Test
    fun `should return empty list when no students match filter`(){
        val filterParam = StudentFilter(name = "No Match",age = 11,email = "nomatch@email.com",course = "NMCourse")
        every{ studentRepository.findAll(any<Specification<Student>>(),any<Pageable>()) } returns PageImpl(emptyList())
        val result = studentService.getAll(filterParam,PageRequest.of(0,10))

        assertEquals(0,result.totalElements)
        assertTrue(result.content.isEmpty())
    }

    @Test
    fun `should group students by course name`() {

        val itStudent = Student(id = 2, name = "Alex", age = 21, email = "alex@example.com", course = courseIT)

        every { studentRepository.findAll() } returns students + itStudent

        val grouped = studentService.groupByCourse()

        assertEquals(2, grouped.size)
        assertTrue(grouped.containsKey("CS"))
        assertTrue(grouped.containsKey("IT"))
        verify { auditService.logEvent(EVENT_GROUP, any()) }
    }

    @Test
    fun `should update student when found`() {
        val studentToUpdate = students.first()
        val request = UpdateStudentRequest("Angelo", "angelonew@gmail.com", 23, "IT")

        every { studentRepository.findById(1L) } returns Optional.of(studentToUpdate)
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
        val studentsToUpdate = students.first()
        val request = UpdateStudentRequest("Mina", "mina@email.com", 21, "BSCE")

        every { studentRepository.findById(1L) } returns Optional.of(studentsToUpdate)
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
        every { studentRepository.findById(1L) } returns Optional.of(students.first())
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