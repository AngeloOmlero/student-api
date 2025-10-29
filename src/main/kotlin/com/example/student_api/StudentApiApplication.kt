package com.example.student_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class StudentApiApplication

fun main(args: Array<String>) {
	runApplication<StudentApiApplication>(*args)
}
