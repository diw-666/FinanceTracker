package com.example.financetracker.database.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.financetracker.database.dao.TransactionDao
import com.example.financetracker.database.entities.TransactionEntity
import com.example.financetracker.model.Transaction
import com.example.financetracker.model.TransactionType
import java.util.Date


class TransactionRepository(private val transactionDao: TransactionDao) {

    // Get all transactions as LiveData
    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions().map { entities ->
        entities.map { it.toTransaction() }
    }

    // Get transactions by type
    fun getTransactionsByType(type: TransactionType): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByType(type).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    // Get transactions by date range
    fun getTransactionsBetweenDates(startDate: Date, endDate: Date): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    // Get transactions by category
    fun getTransactionsByCategory(category: String): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(category).map { entities ->
            entities.map { it.toTransaction() }
        }
    }

    // Insert a transaction
    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(TransactionEntity.fromTransaction(transaction))
    }

    // Insert multiple transactions
    suspend fun insertAll(transactions: List<Transaction>) {
        val entities = transactions.map { TransactionEntity.fromTransaction(it) }
        transactionDao.insertAll(entities)
    }

    // Update a transaction
    suspend fun update(transaction: Transaction) {
        transactionDao.update(TransactionEntity.fromTransaction(transaction))
    }

    // Delete a transaction
    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(TransactionEntity.fromTransaction(transaction))
    }

    // Delete a transaction by ID
    suspend fun deleteById(transactionId: String) {
        transactionDao.deleteById(transactionId)
    }

    // Get a transaction by ID
    suspend fun getTransactionById(transactionId: String): Transaction? {
        return transactionDao.getTransactionById(transactionId)?.toTransaction()
    }

    // Get total amount by transaction type
    suspend fun getTotalByType(type: TransactionType): Double {
        return transactionDao.getTotalByType(type) ?: 0.0
    }

    // Get total amount by transaction type in date range
    suspend fun getTotalByTypeInDateRange(type: TransactionType, startDate: Date, endDate: Date): Double {
        return transactionDao.getTotalByTypeInDateRange(type, startDate, endDate) ?: 0.0
    }
} 