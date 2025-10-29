package com.example.student_api.service

import com.example.student_api.dto.CreateStudentRequest
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals


class StudentServiceImplValidationTest {
    private lateinit var validator: Validator

    @BeforeEach
    fun setup(){
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `should pass validation when all required fields are valid`(){
        val request = CreateStudentRequest("Angelo","angelo@gmail.com",23,"BSCPE")
        val violations = validator.validate(request)
        assertTrue(violations.isEmpty())

    }
    @Test
    fun `should fail validation when name is blank`(){
        val request = CreateStudentRequest("","angelo@gmail.com",23,"BSCPE")
        val violations = validator.validate(request)
        assertTrue(violations.any { it.propertyPath.toString() == "name" })
        assertTrue(violations.any{it.message == "Name is required"})

    }
    @Test
    fun `should fail validation when name contains non letters`(){
        val request = CreateStudentRequest("Angelo123","angelo@gmail.com",23,"BSCPE")
        val violations = validator.validate(request)
        assertTrue(violations.any { it.propertyPath.toString() == "name" })
        assertEquals("Name must only contain letters and spaces",violations.first().message)

    }

    @Test
    fun `should fail validation when email is invalid`(){
        val request = CreateStudentRequest("Angelo","angelogmail.com",23,"BSCPE")
        val violations = validator.validate(request)
        assertTrue(violations.any { it.propertyPath.toString() == "email" })
        assertTrue (violations.any{it.message == "Email must be valid"})
    }

    @Test
    fun `should fail validation when email is blank`(){
        val request = CreateStudentRequest("Angelo","",23,"BSCPE")
        val violations = validator.validate(request)
        assertTrue(violations.any { it.propertyPath.toString() == "email" })
        assertTrue(violations.any{it.message == "Email is required"})
    }

    @Test
    fun `should fail validation when email contains invalid characters`(){
        val request = CreateStudentRequest("Angelo","angelo.gmail.com",23,"BSCPE")
        val violations = validator.validate(request)

        assertTrue(violations.any { it.propertyPath.toString()=="email"})
        assertEquals("Email contains invalid characters",violations.first().message)

    }

    @Test
    fun `should fail validation when age is less than 1`(){
        val request = CreateStudentRequest("Angelo","angelo@email.com",0,"BSCPE")
        val violations = validator.validate(request)
        assertTrue(violations.any { it.propertyPath.toString() == "age" })
        assertEquals("Age should be greater than 1",violations.first().message)
    }

    @Test
    fun `should fail validation when course name is blank`(){
        val request = CreateStudentRequest("Angelo","angelo@gmail.com",23,"")
        val violations = validator.validate(request)
        assertTrue(violations.any{it.propertyPath.toString() == "courseName"})
        assertEquals("Course name is required",violations.first().message)

    }
}