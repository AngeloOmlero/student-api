package com.example.student_api.authentication

import com.example.student_api.dto.AuthRequestDto
import com.example.student_api.dto.AuthResponseDto
import com.example.student_api.dto.CreateUserDto
import com.example.student_api.dto.UserDto
import com.example.student_api.model.Users
import com.example.student_api.repository.UserRepository
import com.example.student_api.security.JWTUtil
import com.example.student_api.service.UserService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.PasswordEncoder

class UserServiceTest {
    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val authenticationManager: AuthenticationManager = mockk()
    private val jwtUtil: JWTUtil = mockk()
    private val userService = UserService(userRepository, passwordEncoder, jwtUtil, authenticationManager)


    @BeforeEach
    fun setup(){
        clearAllMocks()
    }

    @Test
    fun ` should register new user with valid role` (){
        val dto = CreateUserDto("angelo","omlero","ADMIN")

        every { passwordEncoder.encode(dto.password) } returns "encodePass"
        every { userRepository.save(any()) } answers { firstArg() as Users }

        val result : UserDto = userService.register(dto)

        assertEquals("angelo",result.username)
        assertEquals("ADMIN",result.role)

        verify {
            passwordEncoder.encode(dto.password)
            userRepository.save(any())
        }

    }

    @Test
    fun `should register user with default USER role when invalid role provided`(){
        val dto = CreateUserDto("angelo","omlero","INVALID")

        every { passwordEncoder.encode(dto.password) } returns "encodePass"
        every { userRepository.save(any()) } answers {firstArg() as Users}

        val result : UserDto = userService.register(dto)

        assertEquals("angelo",result.username)
        assertEquals("USER",result.role)

        verify {
            passwordEncoder.encode(dto.password)
            userRepository.save(any())
        }

    }

    @Test
    fun `should register user with null role defaults to USER`(){
        val dto = CreateUserDto("angelo","omlero","")

        every { passwordEncoder.encode(dto.password)} returns "encodePass"
        every {userRepository.save(any()) } answers {firstArg() as Users}

        val result : UserDto = userService.register(dto)

        assertEquals("angelo",result.username)
        assertEquals("USER",result.role)

        verify{
            passwordEncoder.encode(dto.password)
            userRepository.save(any())

        }
    }

    @Test
    fun `should login successfully and return token`(){
        val dto = AuthRequestDto("angelo","omlero")
        val auth: Authentication = mockk()
        val securityContext: SecurityContext = mockk(relaxed = true)
        SecurityContextHolder.setContext(securityContext)
        val userDetails = User("angelo", "omlero", emptyList())

        every { authenticationManager.authenticate(any()) } returns auth
        every { auth.principal } returns userDetails
        every { jwtUtil.generateToken(any()) } returns "Jwt-token"

        val response: AuthResponseDto = userService.login(dto)

        assertEquals("Jwt-token",response.token)
        verify {
            authenticationManager.authenticate(any())
            jwtUtil.generateToken(userDetails)
        }
    }

    @Test
    fun `should throw BadCredentialsException on invalid login`(){
        val dto = AuthRequestDto("test-user","wrong-pass")

        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("Bad Credentials")

        assertThrows<BadCredentialsException> {
            userService.login(dto)
        }
        verify {authenticationManager.authenticate(any()) }
        verify (exactly = 0){ jwtUtil.generateToken(any()) }

    }

    @Test
    fun `should handle empty password registration`() {
        val dto = CreateUserDto("angelo","","Admin")

        every{passwordEncoder.encode(dto.password)} returns "encodedPass"
        every { userRepository.save(any())} answers {firstArg() as Users}

        val result : UserDto = userService.register(dto)

        assertEquals("angelo",result.username)
        assertEquals("ADMIN",result.role)

        verify {
            passwordEncoder.encode("")
            userRepository.save(any())
        }


    }


}