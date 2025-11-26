package com.example.student_api.service

import com.example.student_api.dto.PrivateMessageDto
import com.example.student_api.model.Message
import com.example.student_api.repository.MessageRepository
import com.example.student_api.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MessageService(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) {
    fun savePrivateMessage(dto: PrivateMessageDto): Message{
        val sender = userRepository.findByUsername(dto.sender)
            ?: throw EntityNotFoundException("Sender ${dto.sender} not found")

        val receiver = userRepository.findByUsername(dto.receiver)
            ?: throw EntityNotFoundException("Receiver ${dto.receiver} not found")


        val message = Message(
            sender = sender,
            receiver = receiver,
            content = dto.content
        )
        return messageRepository.save(message)

    }

    @Transactional(readOnly = true)
    fun getConversationBetween(userA: String,userB:String, page: Int, size: Int): Page<Message> {
        val a = userRepository.findByUsername(userA)
            ?: throw EntityNotFoundException("Username $userA not found")

        val b = userRepository.findByUsername(userB)
            ?: throw EntityNotFoundException("Username $userB not found")

        return messageRepository.findConversation(a,b, PageRequest.of(page,size))

    }

    fun markAsDelivered(messageId: Long) {
        val msg = messageRepository.findById(messageId).orElseThrow { EntityNotFoundException("Msg not found") }
        msg.delivered = true
        messageRepository.save(msg)
    }

    fun markAllAsRead(receiverUsername: String, senderUsername: String) {
        val receiver = userRepository.findByUsername(receiverUsername) ?: return
        val sender = userRepository.findByUsername(senderUsername) ?: return
        val message = messageRepository.findConversation(sender, receiver, PageRequest.of(0, Integer.MAX_VALUE)).content
        message.forEach {
            if (!it.read) it.read = true
        }
        messageRepository.saveAll(message)
    }


}