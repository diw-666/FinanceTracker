package com.example.financetracker.data

import android.content.Context
import com.example.financetracker.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages file operations for data backup and restore
 */
class FileManager(private val context: Context) {
    
    companion object {
        private const val BACKUP_FILENAME = "finance_tracker_backup.json"
        private const val DATETIME_FORMAT = "yyyyMMdd_HHmmss"
    }
    
    /**
     * Exports transactions to a JSON file in internal storage
     */
    fun exportData(transactions: List<Transaction>): String {
        val gson = Gson()
        val jsonData = gson.toJson(transactions)
        
        // Create a timestamped filename
        val dateFormat = SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        val filename = "backup_$timestamp.json"
        
        try {
            val file = File(context.filesDir, filename)
            FileOutputStream(file).use { 
                it.write(jsonData.toByteArray())
            }
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }
    
    /**
     * Imports transactions from a JSON file from internal storage
     */
    fun importData(filePath: String): List<Transaction> {
        try {
            val file = File(filePath)
            val jsonData = FileInputStream(file).bufferedReader().use { it.readText() }
            
            val gson = Gson()
            val type = object : TypeToken<List<Transaction>>() {}.type
            return gson.fromJson(jsonData, type)
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }
    
    /**
     * Lists all backup files in internal storage
     */
    fun getBackupFiles(): List<File> {
        val files = context.filesDir.listFiles { file ->
            file.isFile && file.name.startsWith("backup_") && file.name.endsWith(".json")
        }
        return files?.toList() ?: emptyList()
    }
    
    /**
     * Deletes a backup file
     */
    fun deleteBackup(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists() && file.delete()
    }
} 