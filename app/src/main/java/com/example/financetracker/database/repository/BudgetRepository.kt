package com.example.financetracker.database.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.financetracker.database.dao.BudgetDao
import com.example.financetracker.database.entities.BudgetEntity
import com.example.financetracker.model.Budget
import java.util.Date


class BudgetRepository(private val budgetDao: BudgetDao) {

    // Get all budgets as LiveData
    val allBudgets: LiveData<List<Budget>> = budgetDao.getAllBudgets().map { entities ->
        entities.map { it.toBudget() }
    }

    // Get budgets in a date period
    fun getBudgetsInPeriod(startDate: Date, endDate: Date): LiveData<List<Budget>> {
        return budgetDao.getBudgetsInPeriod(startDate, endDate).map { entities ->
            entities.map { it.toBudget() }
        }
    }

    // Insert a budget
    suspend fun insert(budget: Budget) {
        budgetDao.insert(BudgetEntity.fromBudget(budget))
    }

    // Update a budget
    suspend fun update(budget: Budget) {
        budgetDao.update(BudgetEntity.fromBudget(budget))
    }

    // Delete a budget
    suspend fun delete(budget: Budget) {
        budgetDao.delete(BudgetEntity.fromBudget(budget))
    }

    // Delete a budget by ID
    suspend fun deleteById(budgetId: String) {
        budgetDao.deleteById(budgetId)
    }

    // Get a budget by ID
    suspend fun getBudgetById(budgetId: String): Budget? {
        return budgetDao.getBudgetById(budgetId)?.toBudget()
    }

    // Get the active budget for a date
    suspend fun getActiveBudget(date: Date = Date()): Budget? {
        return budgetDao.getActiveBudget(date)?.toBudget()
    }
} 