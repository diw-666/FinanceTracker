package com.example.financetracker.model

import java.util.Date


data class Budget(
    val amount: Double,
    val startDate: Date,
    val endDate: Date,
    val warningThreshold: Int = 80, // Default warning at 80% of budget
    val categories: List<String> = emptyList() // Empty means all categories
) 