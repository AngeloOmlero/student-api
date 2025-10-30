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
    private fun validate(request: CreateStudentRequest) =
        validator.validate(request).map { it.propertyPath.toString() to it.message }

    @Test
    fun `should pass when all required fields are valid`(){
        val request = CreateStudentRequest("Angelo","angelo@gmail.com",23,"BSCPE")
        val violations = validator.validate(request)
        assertTrue(violations.isEmpty())

    }
    @Test
    fun `should fail when name is blank`(){
        val errors = validate(CreateStudentRequest("","angelo@gmail.com",23,"BSCPE"))

        assertAll(
               {assertTrue (errors.any { it.first == "name" } )},
               {assertTrue(errors.any{it.second == "Name is required"})},
               {assertEquals(2,errors.size)}
           )
    }

    @Test
    fun `should fail  when name contains non letters`(){
        val errors = validate(CreateStudentRequest("Angelo123","angelo@gmail.com",23,"BSCPE"))
        assertAll(
            {assertTrue(errors.any{it.first == "name"})},
            {assertTrue(errors.any{it.second == "Name must only contain letters and spaces"})},
            {assertEquals(1,errors.size)}
        )

    }

    @Test
    fun `should fail  when email is invalid`(){
        val errors = validate( CreateStudentRequest("Angelo","angelogmail.com",23,"BSCPE"))
        assertAll(
            {assertTrue(errors.any{it.first == "email"})},
            {assertTrue(errors.any{it.second == "Email must be valid"})}

        )
    }

    @Test
    fun `should fail when email is blank`(){
        val errors = validate(CreateStudentRequest("Angelo","",23,"BSCPE"))
        assertAll(
            {assertTrue(errors.any{it.first == "email"})},
            {assertTrue(errors.any{it.second == "Email is required"})}
        )
    }

    @Test
    fun `should fail when email contains invalid characters`(){
        val errors = validate(CreateStudentRequest("Angelo","angelo.gmail.com",23,"BSCPE"))
       assertAll(
           {assertTrue(errors.any{it.first == "email"})},
           {assertTrue(errors.any{it.second == "Email contains invalid characters"})}

       )
    }

    @Test
    fun `should fail when age is less than 1`(){
        val errors = validate(CreateStudentRequest("Angelo","angelo@email.com",0,"BSCPE"))
        assertAll(
            {assertTrue(errors.any{it.first=="age"})},
            {assertTrue(errors.any{it.second=="Age should be greater than 1"})}
        )
    }

    @Test
    fun `should fail when course name is blank`(){
        val errors = validate(CreateStudentRequest("Angelo","angelo@gmail.com",23,""))
        assertAll(
            {assertTrue(errors.any{it.first=="courseName"})},
            {assertTrue(errors.any{it.second == "Course name is required"})}
        )
    }
}