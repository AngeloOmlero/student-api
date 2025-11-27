package com.example.student_api.controller

import com.example.student_api.service.StorageService
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@RestController
@RequestMapping("/api/files")
class FileController(
    private val storageService: StorageService
) {

    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, String>> {
        val filename = storageService.store(file)
        val url = "/api/files/$filename"
        return ResponseEntity.ok().body(mapOf("url" to url))
    }

    @GetMapping("/{filename:.+}")
    @ResponseBody
    fun serveFile(@PathVariable filename: String): ResponseEntity<Resource> {
        val file = storageService.loadAsResource(filename)
        return ResponseEntity.ok().header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"${file.filename}\""
        ).body(file)
    }
}