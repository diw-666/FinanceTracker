package com.yasiruvithana.financetracker.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yasiruvithana.financetracker.FinanceApplication
import com.yasiruvithana.financetracker.model.Budget
import com.yasiruvithana.financetracker.model.Transaction
import com.yasiruvithana.financetracker.model.TransactionType
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for MainActivity and its fragments
 * Manages data operations using repositories
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    // Get repositories from application
    private val transactionRepository = (application as FinanceApplication).transactionRepository
    private val budgetRepository = (application as FinanceApplication).budgetRepository
    private val categoryRepository = (application as FinanceApplication).categoryRepository
    
    // LiveData for transactions
    val allTransactions = transactionRepository.allTransactions
    
    // LiveData for budgets
    val allBudgets = budgetRepository.allBudgets
    
    // LiveData for categories by type
    val expenseCategories = categoryRepository.getCategoriesByType(TransactionType.EXPENSE)
    val incomeCategories = categoryRepository.getCategoriesByType(TransactionType.INCOME)
    
    // LiveData for currently active budget
    private val _activeBudget = MutableLiveData<Budget?>()
    val activeBudget: LiveData<Budget?> get() = _activeBudget
    
    init {
        refreshActiveBudget()
    }
    
    // Transactions methods
    fun getTransactionsByType(type: TransactionType): LiveData<List<Transaction>> {
        return transactionRepository.getTransactionsByType(type)
    }
    
    fun getTransactionsBetweenDates(startDate: Date, endDate: Date): LiveData<List<Transaction>> {
        return transactionRepository.getTransactionsBetweenDates(startDate, endDate)
    }
    
    fun getTransactionsByCategory(category: String): LiveData<List<Transaction>> {
        return transactionRepository.getTransactionsByCategory(category)
    }
    
    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        transactionRepository.insert(transaction)
    }
    
    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        transactionRepository.update(transaction)
    }
    
    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        transactionRepository.delete(transaction)
    }
    
    suspend fun getTransactionById(id: String): Transaction? {
        return transactionRepository.getTransactionById(id)
    }
    
    // Budget methods
    fun insertBudget(budget: Budget) = viewModelScope.launch {
        budgetRepository.insert(budget)
    }
    
    fun updateBudget(budget: Budget) = viewModelScope.launch {
        budgetRepository.update(budget)
    }
    
    fun deleteBudget(budget: Budget) = viewModelScope.launch {
        budgetRepository.delete(budget)
    }
    
    fun refreshActiveBudget() = viewModelScope.launch {
        _activeBudget.postValue(budgetRepository.getActiveBudget())
    }
    
    // Financial summary methods
    suspend fun getTotalIncome(): Double {
        return transactionRepository.getTotalByType(TransactionType.INCOME)
    }
    
    suspend fun getTotalExpense(): Double {
        return transactionRepository.getTotalByType(TransactionType.EXPENSE)
    }
    
    suspend fun getBalance(): Double {
        return getTotalIncome() - getTotalExpense()
    }
    
    suspend fun getTotalIncomeInPeriod(startDate: Date, endDate: Date): Double {
        return transactionRepository.getTotalByTypeInDateRange(TransactionType.INCOME, startDate, endDate)
    }
    
    suspend fun getTotalExpenseInPeriod(startDate: Date, endDate: Date): Double {
        return transactionRepository.getTotalByTypeInDateRange(TransactionType.EXPENSE, startDate, endDate)
    }
} 