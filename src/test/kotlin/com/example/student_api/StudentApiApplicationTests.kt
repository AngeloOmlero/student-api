package com.example.student_api

import com.example.student_api.controller.StudentController
import com.example.student_api.repository.StudentRepository
import com.example.student_api.service.StudentServiceImpl
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertNotNull

@SpringBootTest
class StudentApiApplicationTests {

	@Autowired
	private lateinit var studentController: StudentController

	@Autowired
	private lateinit var studentServiceImpl: StudentServiceImpl

	@Autowired
	private lateinit var studentRepository: StudentRepository

	@Test
	fun contextLoads() {
		assertNotNull(studentController)
		assertNotNull(studentServiceImpl)
		assertNotNull(studentRepository)
	}


}
