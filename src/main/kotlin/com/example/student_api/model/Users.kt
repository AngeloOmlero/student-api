package com.example.student_api.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
class Users(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, unique = true)
    var  username: String = "",

    @Column(nullable = false)
    var  password: String = "",

    @Column(name="full_name",nullable = false)
    var  fullName: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var  role: Role = Role.USER
)

enum class Role {
    ADMIN,
    USER
}
