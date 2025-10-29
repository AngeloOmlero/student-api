package com.example.student_api.dto.mapper

import org.springframework.data.domain.Page

data class PageResponse<T>(
    val data: List<T>,
    val meta: PageMeta
)

data class PageMeta(
    val page: Int,
    val size: Int,
    val totalPage:Int,
    val totalElements: Long,
    val isFirst: Boolean,
    val isLast: Boolean,
)

fun <T> Page <T>.toPageResponse(): PageResponse<T> =
    PageResponse(
        data = content,
        meta = PageMeta(
            page = number,
            size = size,
            totalPage = totalPages,
            totalElements = totalElements,
            isFirst = isFirst,
            isLast = isFirst
        )
    )