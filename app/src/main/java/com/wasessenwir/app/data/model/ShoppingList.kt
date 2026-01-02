package com.wasessenwir.app.data.model

data class ShoppingList(
    val id: String,
    val householdId: String,
    val weekStart: String,
    val items: List<ShoppingItem>,
    val createdAt: Long
)
