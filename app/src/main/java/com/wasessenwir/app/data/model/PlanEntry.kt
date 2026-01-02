package com.wasessenwir.app.data.model

data class PlanEntry(
    val id: String,
    val householdId: String,
    val date: String,
    val mealSlot: MealSlot,
    val recipeId: String
)
