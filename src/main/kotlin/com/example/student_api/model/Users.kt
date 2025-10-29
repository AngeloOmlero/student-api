package com.example.student_api.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class Users(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val username: String = "",

    @Column(nullable = false)
    val password: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.USER
)

enum class Role {
    ADMIN,
    USER
}
