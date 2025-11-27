package com.example.student_api

import com.example.student_api.config.StorageProperties
import com.example.student_api.service.StorageService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(StorageProperties::class)
class StudentApiApplication{
	@Bean
	fun init(storageService: StorageService) = CommandLineRunner {
		storageService.init()
	}
}

fun main(args: Array<String>) {
	runApplication<StudentApiApplication>(*args)
}
