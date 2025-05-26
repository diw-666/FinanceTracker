package com.yasiruvithana.financetracker.model

import java.util.Date
import java.util.UUID


data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var amount: Double,
    var category: String,
    var date: Date,
    var type: TransactionType,
    var notes: String = ""
)

/**
 * Enum class representing types of transactions
 */
enum class TransactionType {
    INCOME, EXPENSE
} 