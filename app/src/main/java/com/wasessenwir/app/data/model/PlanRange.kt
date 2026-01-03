package com.wasessenwir.app.data.model

data class PlanRange(
    val id: String,
    val householdId: String,
    val startDate: String,
    val endDate: String,
    val updatedAt: Long
)
