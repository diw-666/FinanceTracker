package com.example.financetracker.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.financetracker.model.Budget
import com.example.financetracker.model.Transaction
import com.example.financetracker.model.TransactionType
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages app preferences and transaction data using SharedPreferences
 */
class PreferenceManager(context: Context) {

    companion object {
        private const val TAG = "PreferenceManager"
        private const val PREFS_NAME = "finance_tracker_prefs"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_BUDGET = "budget"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_BUDGET_ALERTS_ENABLED = "budget_alerts_enabled"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        
        // Default values
        private const val DEFAULT_CURRENCY = "$"
        private const val DEFAULT_NOTIFICATIONS_ENABLED = true
        private const val DEFAULT_BUDGET_ALERTS_ENABLED = true
        private const val DEFAULT_REMINDER_ENABLED = false
        
        // Date format for GSON serialization
        private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Custom GSON instance with Date type adapter
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, object : JsonSerializer<Date>, JsonDeserializer<Date> {
            private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)
            
            override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                return JsonPrimitive(src?.let { dateFormat.format(it) })
            }
            
            override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date? {
                return try {
                    json?.asString?.let { dateFormat.parse(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Date() // Return current date as fallback
                }
            }
        })
        .registerTypeAdapter(TransactionType::class.java, object : JsonDeserializer<TransactionType> {
            override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): TransactionType {
                return try {
                    val value = json?.asString ?: "EXPENSE"
                    TransactionType.valueOf(value)
                } catch (e: Exception) {
                    e.printStackTrace()
                    TransactionType.EXPENSE // Default to expense if error
                }
            }
        })
        .create()
    
    // Currency preference
    fun setCurrency(currency: String) {
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
    }
    
    fun getCurrency(): String {
        return prefs.getString(KEY_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
    }
    
    // Transaction data
    fun saveTransactions(transactions: List<Transaction>) {
        try {
            val json = gson.toJson(transactions)
            Log.d(TAG, "Saving ${transactions.size} transactions, JSON size: ${json.length}")
            prefs.edit().putString(KEY_TRANSACTIONS, json).apply()
            
            // Verify save was successful
            val savedJson = prefs.getString(KEY_TRANSACTIONS, null)
            if (savedJson.isNullOrBlank()) {
                Log.e(TAG, "Failed to save transactions - saved JSON is null or empty")
            } else {
                Log.d(TAG, "Successfully saved transactions. Saved JSON size: ${savedJson.length}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving transactions", e)
            e.printStackTrace()
            // In case of failure, at least don't crash
        }
    }
    
    fun getTransactions(): List<Transaction> {
        try {
            val json = prefs.getString(KEY_TRANSACTIONS, null)
            if (json.isNullOrBlank()) {
                Log.d(TAG, "No transaction data found in preferences")
                return emptyList()
            }
            
            Log.d(TAG, "Retrieved JSON data, size: ${json.length}")
            
            val type = object : TypeToken<List<Transaction>>() {}.type
            val transactions = gson.fromJson<List<Transaction>>(json, type)
            
            // Additional validation for null list or individual items
            if (transactions == null) {
                Log.e(TAG, "Transactions list is null after deserialization")
                return emptyList()
            }
            
            Log.d(TAG, "Deserialized ${transactions.size} transactions")
            
            // Filter out any null transactions that might have slipped through
            // and validate each transaction has a valid date and type
            val validTransactions = transactions.filterNotNull().filter { transaction ->
                try {
                    // Verify transaction has valid date
                    if (transaction.date == null) {
                        Log.w(TAG, "Transaction ${transaction.id} has null date")
                        return@filter false
                    }
                    
                    // Verify transaction has valid type
                    val typeCheck = transaction.type
                    
                    // Verify transaction has valid amount
                    if (transaction.amount.isNaN() || transaction.amount.isInfinite()) {
                        Log.w(TAG, "Transaction ${transaction.id} has invalid amount: ${transaction.amount}")
                        return@filter false
                    }
                    
                    // All checks passed
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Error validating transaction ${transaction.id}", e)
                    e.printStackTrace()
                    false
                }
            }
            
            Log.d(TAG, "Returning ${validTransactions.size} valid transactions out of ${transactions.size}")
            return validTransactions
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON syntax error when deserializing transactions", e)
            e.printStackTrace()
            // Clear corrupted data and return empty list
            prefs.edit().remove(KEY_TRANSACTIONS).apply()
            return emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error retrieving transactions", e)
            e.printStackTrace()
            return emptyList()
        }
    }
    
    fun addTransaction(transaction: Transaction) {
        Log.d(TAG, "Adding new transaction: ${transaction.id}, amount: ${transaction.amount}, type: ${transaction.type}")
        val transactions = getTransactions().toMutableList()
        transactions.add(transaction)
        saveTransactions(transactions)
    }
    
    fun updateTransaction(transaction: Transaction) {
        Log.d(TAG, "Updating transaction: ${transaction.id}")
        val transactions = getTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            transactions[index] = transaction
            saveTransactions(transactions)
        } else {
            Log.w(TAG, "Transaction ${transaction.id} not found for update")
        }
    }
    
    fun deleteTransaction(transactionId: String) {
        Log.d(TAG, "Deleting transaction: $transactionId")
        val transactions = getTransactions().toMutableList()
        val initialSize = transactions.size
        transactions.removeIf { it.id == transactionId }
        if (transactions.size < initialSize) {
            Log.d(TAG, "Transaction $transactionId removed successfully")
            saveTransactions(transactions)
        } else {
            Log.w(TAG, "Transaction $transactionId not found for deletion")
        }
    }
    
    // Budget data
    fun saveBudget(budget: Budget) {
        try {
            val json = gson.toJson(budget)
            Log.d(TAG, "Saving budget: $json")
            prefs.edit().putString(KEY_BUDGET, json).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving budget", e)
            e.printStackTrace()
        }
    }
    
    fun getBudget(): Budget? {
        try {
            val json = prefs.getString(KEY_BUDGET, null)
            if (json == null) {
                Log.d(TAG, "No budget found")
                return null
            }
            
            Log.d(TAG, "Retrieved budget JSON: $json")
            return gson.fromJson(json, Budget::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "JSON syntax error when deserializing budget", e)
            e.printStackTrace()
            // Clear corrupted data
            prefs.edit().remove(KEY_BUDGET).apply()
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error retrieving budget", e)
            e.printStackTrace()
            return null
        }
    }
    
    // Notification preferences
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }
    
    fun areNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, DEFAULT_NOTIFICATIONS_ENABLED)
    }
    
    fun setBudgetAlertsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BUDGET_ALERTS_ENABLED, enabled).apply()
    }
    
    fun areBudgetAlertsEnabled(): Boolean {
        return prefs.getBoolean(KEY_BUDGET_ALERTS_ENABLED, DEFAULT_BUDGET_ALERTS_ENABLED)
    }
    
    fun setReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
    }
    
    fun isReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, DEFAULT_REMINDER_ENABLED)
    }
    
    // Helper methods
    fun clearAllData() {
        Log.w(TAG, "Clearing all preferences data")
        prefs.edit().clear().apply()
    }
    
    // Debug helper
    fun dumpPreferences(): String {
        val currency = getCurrency()
        val transactionCount = getTransactions().size
        val hasBudget = getBudget() != null
        val notificationsEnabled = areNotificationsEnabled()
        val budgetAlertsEnabled = areBudgetAlertsEnabled()
        val reminderEnabled = isReminderEnabled()
        
        return "PreferenceManager State:\n" +
               "- Currency: $currency\n" +
               "- Transaction count: $transactionCount\n" +
               "- Has budget: $hasBudget\n" +
               "- Notifications enabled: $notificationsEnabled\n" +
               "- Budget alerts enabled: $budgetAlertsEnabled\n" +
               "- Reminder enabled: $reminderEnabled"
    }
} 