package com.yasiruvithana.financetracker.database.utils

import android.content.Context
import android.util.Log
import com.yasiruvithana.financetracker.data.FileManager
import com.yasiruvithana.financetracker.data.PreferenceManager
import com.yasiruvithana.financetracker.database.repository.TransactionRepository
import com.yasiruvithana.financetracker.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class DataMigration(
    private val context: Context,
    private val transactionRepository: TransactionRepository
) {
    private val fileManager = FileManager(context)
    private val prefManager = PreferenceManager(context)
    private val TAG = "DataMigration"


    suspend fun migrateExistingData(): Int = withContext(Dispatchers.IO) {
        var migratedCount = 0
        
        try {
            // First check SharedPreferences for transactions
            val prefTransactions = prefManager.getTransactions()
            
            if (prefTransactions.isNotEmpty()) {
                Log.d(TAG, "Found ${prefTransactions.size} transactions in SharedPreferences")
                transactionRepository.insertAll(prefTransactions)
                migratedCount += prefTransactions.size
                
                // Clear the transactions from SharedPreferences to avoid duplicates on next run
                prefManager.saveTransactions(emptyList())
                Log.d(TAG, "Migrated ${prefTransactions.size} transactions from SharedPreferences")
            }
            
            // Then get all backup files
            val backupFiles = fileManager.getBackupFiles()
            
            if (backupFiles.isEmpty() && prefTransactions.isEmpty()) {
                Log.d(TAG, "No backup files or SharedPreferences data found for migration")
                return@withContext 0
            }
            
            // Process each backup file
            backupFiles.forEach { file ->
                try {
                    val transactions = fileManager.importData(file.absolutePath)
                    if (transactions.isNotEmpty()) {
                        transactionRepository.insertAll(transactions)
                        migratedCount += transactions.size
                        Log.d(TAG, "Migrated ${transactions.size} transactions from ${file.name}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error importing from ${file.name}: ${e.message}")
                }
            }
            
            Log.d(TAG, "Migration completed: $migratedCount transactions migrated")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during migration: ${e.message}")
        }
        
        return@withContext migratedCount
    }
    
    /**
     * Check if migration marker exists
     */
    fun isMigrationCompleted(): Boolean {
        val migrationMarker = File(context.filesDir, MIGRATION_MARKER)
        return migrationMarker.exists()
    }
    

    fun markMigrationCompleted() {
        try {
            val migrationMarker = File(context.filesDir, MIGRATION_MARKER)
            migrationMarker.createNewFile()
            Log.d(TAG, "Migration marked as completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating migration marker: ${e.message}")
        }
    }
    
    companion object {
        private const val MIGRATION_MARKER = ".migration_completed"
    }
} 