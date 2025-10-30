package com.example.student_api.authentication

import com.example.student_api.dto.CreateUserDto
import com.example.student_api.model.Users
import com.example.student_api.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers

class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    companion object{

        @Container
        private val postgres = PostgreSQLContainer("postgres:latest").apply {
            withDatabaseName("test_db")
            withUsername("test")
            withPassword("test-pass")
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry){
            registry.add("spring.datasource.url",postgres::getJdbcUrl)
            registry.add("spring.datasource.username",postgres::getUsername)
            registry.add("spring.datasource.password",postgres::getPassword)
        }


    }

    @BeforeEach
    fun setup(){
        userRepository.deleteAll()
    }

    @Test
    fun `should register a new user successfully`(){
        val user = CreateUserDto(
            username = "admin",
            password = "admin123",
            role = "ADMIN"
        )

        mockMvc.post("/api/auth/register"){
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                "username": "${user.username}",
                "password": "${user.password}",
                "role": "${user.role}"
            }
            """
        }
            .andExpect {
                status { isCreated()}
                jsonPath("$.username"){value("admin")}
                jsonPath("$.role"){value("ADMIN")}
            }

        assertTrue(userRepository.findByUsername("admin") != null)

    }

    @Test
    fun `should login and return JWT token`(){
        mockMvc.post("/api/auth/register"){
            contentType = MediaType.APPLICATION_JSON
            content = """{"username":"admin","password":"admin123","role":"ADMIN"}"""
        }.andExpect { status { isCreated() }  }

        mockMvc.post("/api/auth/login"){
            contentType = MediaType.APPLICATION_JSON
            content = """{"username":"admin","password":"admin123"}"""
        }.andExpect {
            status { isOk()}
            jsonPath("$.token"){exists()}
        }

    }

    @Test
    fun `should return error for invalid credentials`(){
        mockMvc.post("/api/auth/login"){
            contentType = MediaType.APPLICATION_JSON
            content = """{"username":"unknown","password":"wrong"}"""
        }.andExpect {
            status { isUnauthorized()}
        }
    }
}