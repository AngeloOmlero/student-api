package com.example.student_api.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class UserPresenceService {
    private val onlineUsers: MutableSet<String> = ConcurrentHashMap.newKeySet()

    fun userConnected(username: String) {
        onlineUsers.add(username)
    }

    fun userDisconnected(username: String) {
        onlineUsers.remove(username)
    }

    fun isUserOnline(username: String): Boolean {
        return onlineUsers.contains(username)
    }

    fun getOnlineUsers(): Set<String> {
        return onlineUsers.toSet()
    }
}