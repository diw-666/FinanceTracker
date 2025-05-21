package com.example.financetracker.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.financetracker.database.converters.DateConverter
import com.example.financetracker.database.converters.StringListConverter
import com.example.financetracker.model.Budget
import java.util.Date
import java.util.UUID

@Entity(tableName = "budgets")
@TypeConverters(DateConverter::class, StringListConverter::class)
data class BudgetEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val startDate: Date,
    val endDate: Date,
    val warningThreshold: Int,
    val categories: List<String>
) {
    fun toBudget(): Budget {
        return Budget(
            amount = amount,
            startDate = startDate,
            endDate = endDate,
            warningThreshold = warningThreshold,
            categories = categories
        )
    }
    
    companion object {
        fun fromBudget(budget: Budget): BudgetEntity {
            return BudgetEntity(
                amount = budget.amount,
                startDate = budget.startDate,
                endDate = budget.endDate,
                warningThreshold = budget.warningThreshold,
                categories = budget.categories
            )
        }
    }
} 