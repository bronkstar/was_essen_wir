package com.wasessenwir.app.data.model

data class Household(
    val id: String,
    val name: String,
    val members: List<String>,
    val createdAt: Long,
    val updatedAt: Long
)
