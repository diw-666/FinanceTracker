package com.yasiruvithana.financetracker.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yasiruvithana.financetracker.database.entities.BudgetEntity
import java.util.Date

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity)

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :budgetId")
    suspend fun deleteById(budgetId: String)

    @Query("SELECT * FROM budgets ORDER BY startDate DESC")
    fun getAllBudgets(): LiveData<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    suspend fun getBudgetById(budgetId: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE :date BETWEEN startDate AND endDate")
    suspend fun getActiveBudget(date: Date): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE startDate <= :endDate AND endDate >= :startDate")
    fun getBudgetsInPeriod(startDate: Date, endDate: Date): LiveData<List<BudgetEntity>>
} 