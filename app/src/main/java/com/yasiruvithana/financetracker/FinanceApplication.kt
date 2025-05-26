package com.yasiruvithana.financetracker

import android.app.Application
import android.util.Log
import com.yasiruvithana.financetracker.data.PreferenceManager
import com.yasiruvithana.financetracker.database.FinanceDatabase
import com.yasiruvithana.financetracker.database.repository.BudgetRepository
import com.yasiruvithana.financetracker.database.repository.CategoryRepository
import com.yasiruvithana.financetracker.database.repository.TransactionRepository
import com.yasiruvithana.financetracker.database.utils.DataMigration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Application class for initializing database and repositories
 */
class FinanceApplication : Application() {

    // Using lazy for database and repository initialization
    private val database by lazy { FinanceDatabase.getDatabase(this) }
    
    // Repositories
    val transactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    val budgetRepository by lazy { BudgetRepository(database.budgetDao()) }
    val categoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    
    // Data migration utility
    private val dataMigration by lazy { DataMigration(this, transactionRepository) }
    
    // Preference manager
    private val prefManager by lazy { PreferenceManager(this) }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database and repositories
        Log.d(TAG, "Initializing Finance Application")
        
        // Check if data migration is needed and perform it
        CoroutineScope(Dispatchers.IO).launch {
            val prefTransactions = prefManager.getTransactions()
            
            // Force migration if there are transactions in SharedPreferences
            if (prefTransactions.isNotEmpty() || !dataMigration.isMigrationCompleted()) {
                Log.d(TAG, "Starting data migration... (${prefTransactions.size} transactions in SharedPreferences)")
                val migratedCount = dataMigration.migrateExistingData()
                
                // Mark migration as completed
                if (migratedCount > 0) {
                    Log.d(TAG, "Migration completed successfully ($migratedCount records)")
                    dataMigration.markMigrationCompleted()
                } else {
                    Log.d(TAG, "No data to migrate or migration failed")
                    
                    // Still mark as completed to avoid future attempts
                    if (!dataMigration.isMigrationCompleted()) {
                        dataMigration.markMigrationCompleted()
                    }
                }
            } else {
                Log.d(TAG, "Data migration already completed")
            }
        }
    }
    
    companion object {
        private const val TAG = "FinanceApplication"
    }
} 