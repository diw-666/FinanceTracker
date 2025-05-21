package com.example.financetracker.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.financetracker.database.converters.DateConverter
import com.example.financetracker.database.converters.TransactionTypeConverter
import com.example.financetracker.model.Transaction
import com.example.financetracker.model.TransactionType
import java.util.Date

@Entity(tableName = "transactions")
@TypeConverters(DateConverter::class, TransactionTypeConverter::class)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val type: TransactionType,
    val notes: String
) {
    fun toTransaction(): Transaction {
        return Transaction(
            id = id,
            title = title,
            amount = amount,
            category = category,
            date = date,
            type = type,
            notes = notes
        )
    }
    
    companion object {
        fun fromTransaction(transaction: Transaction): TransactionEntity {
            return TransactionEntity(
                id = transaction.id,
                title = transaction.title,
                amount = transaction.amount,
                category = transaction.category,
                date = transaction.date,
                type = transaction.type,
                notes = transaction.notes
            )
        }
    }
} 