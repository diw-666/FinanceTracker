package com.example.financetracker.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.financetracker.database.converters.TransactionTypeConverter
import com.example.financetracker.model.Category
import com.example.financetracker.model.TransactionType

@Entity(tableName = "categories")
@TypeConverters(TransactionTypeConverter::class)
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: TransactionType,
    val iconResId: Int?
) {
    fun toCategory(): Category {
        return Category(
            id = id,
            name = name,
            type = type,
            iconResId = iconResId
        )
    }
    
    companion object {
        fun fromCategory(category: Category): CategoryEntity {
            return CategoryEntity(
                id = category.id,
                name = category.name,
                type = category.type,
                iconResId = category.iconResId
            )
        }
    }
} 