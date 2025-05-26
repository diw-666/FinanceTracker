package com.yasiruvithana.financetracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yasiruvithana.financetracker.database.repository.TransactionRepository
import com.yasiruvithana.financetracker.model.Transaction
import com.yasiruvithana.financetracker.model.TransactionType
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for Transaction data operations
 * Provides a clean API for the UI to interact with the database
 */
class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    // LiveData of all transactions
    val allTransactions: LiveData<List<Transaction>> = repository.allTransactions
    
    // Get transactions by type
    fun getTransactionsByType(type: TransactionType): LiveData<List<Transaction>> {
        return repository.getTransactionsByType(type)
    }
    
    // Get transactions by date range
    fun getTransactionsBetweenDates(startDate: Date, endDate: Date): LiveData<List<Transaction>> {
        return repository.getTransactionsBetweenDates(startDate, endDate)
    }
    
    // Get transactions by category
    fun getTransactionsByCategory(category: String): LiveData<List<Transaction>> {
        return repository.getTransactionsByCategory(category)
    }
    
    // Insert a transaction
    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }
    
    // Insert multiple transactions
    fun insertAll(transactions: List<Transaction>) = viewModelScope.launch {
        repository.insertAll(transactions)
    }
    
    // Update a transaction
    fun update(transaction: Transaction) = viewModelScope.launch {
        repository.update(transaction)
    }
    
    // Delete a transaction
    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }
    
    // Delete a transaction by ID
    fun deleteById(transactionId: String) = viewModelScope.launch {
        repository.deleteById(transactionId)
    }
    
    // Get total amount by transaction type
    suspend fun getTotalByType(type: TransactionType): Double {
        return repository.getTotalByType(type)
    }
    
    // Get total amount by transaction type in date range
    suspend fun getTotalByTypeInDateRange(type: TransactionType, startDate: Date, endDate: Date): Double {
        return repository.getTotalByTypeInDateRange(type, startDate, endDate)
    }
    
    /**
     * Factory for creating a TransactionViewModel with a constructor that takes a TransactionRepository
     */
    class TransactionViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TransactionViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 