package com.example.financetracker.database.converters

import androidx.room.TypeConverter
import com.example.financetracker.model.TransactionType


class TransactionTypeConverter {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
} 