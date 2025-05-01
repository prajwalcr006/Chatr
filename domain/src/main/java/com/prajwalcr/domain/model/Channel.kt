package com.prajwalcr.domain.model

data class Channel(
    val key: String? = null,
    val value: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)