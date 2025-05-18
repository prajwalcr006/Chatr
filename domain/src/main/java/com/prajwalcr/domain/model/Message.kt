package com.prajwalcr.domain.model

data class Message(
    val messageId: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val profileUrl: String? = null,
    val createdAt: Long? = null
)
