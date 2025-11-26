package com.example.student_api.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "messages")
class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var content: String = "",

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var delivered: Boolean = false,

    @Column(nullable = false)
    var read: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    var sender: Users,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    var receiver: Users,


){
    constructor(): this (0, "", Instant.now(), false, false, Users(), Users())
}