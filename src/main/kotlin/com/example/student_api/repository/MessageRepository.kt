package com.example.student_api.repository

import com.example.student_api.model.Message
import com.example.student_api.model.Users
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MessageRepository: JpaRepository<Message, Long> {
    @Query("""
        select m from Message m
        where (m.sender = :userA and m.receiver = :userB)
        or(m.sender = :userB and m.receiver = :userA)
        order by m.createdAt asc
    """)
    fun findConversation(userA: Users, userB: Users,pageable: Pageable): Page<Message>

}