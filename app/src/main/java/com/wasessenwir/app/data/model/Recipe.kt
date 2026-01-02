package com.wasessenwir.app.data.model

data class Recipe(
    val id: String,
    val householdId: String,
    val name: String,
    val servings: Int,
    val ingredients: List<Ingredient>,
    val createdAt: Long,
    val updatedAt: Long
)
