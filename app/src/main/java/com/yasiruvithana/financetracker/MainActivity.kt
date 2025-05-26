package com.yasiruvithana.financetracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.yasiruvithana.financetracker.data.PreferenceManager
import com.yasiruvithana.financetracker.databinding.ActivityMainBinding
import com.yasiruvithana.financetracker.model.Transaction
import com.yasiruvithana.financetracker.model.TransactionType
import com.yasiruvithana.financetracker.ui.AddTransactionActivity
import com.yasiruvithana.financetracker.ui.BudgetFragment
import com.yasiruvithana.financetracker.ui.DashboardFragment
import com.yasiruvithana.financetracker.ui.DispatchersProfileFragment
import com.yasiruvithana.financetracker.ui.SettingsActivity
import com.yasiruvithana.financetracker.ui.SimpleTransactionsFragment
import com.yasiruvithana.financetracker.ui.StatisticsFragment
import com.yasiruvithana.financetracker.ui.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.Date

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefManager: PreferenceManager
    private lateinit var auth: FirebaseAuth
    private var hasEncounteredError = false
    
    // ViewModel for data operations
    private val viewModel: MainViewModel by viewModels()
    
    // Make bottomNavigation accessible to fragments
    val bottomNavigation get() = binding.bottomNavigation
    
    // Current displayed fragment
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setSupportActionBar(binding.toolbar)
            
            prefManager = PreferenceManager(this)
            auth = Firebase.auth
            
            // Check if user is logged in
            if (auth.currentUser == null) {
                startLoginActivity()
                return
            }
            
            // Check if app was crashing and reset data if needed
            if (hasCrashed()) {
                showResetDataDialog()
                return
            }
            
            // Debug: Check transaction data
            debugTransactionData()
            
            // Setup bottom navigation
            setupBottomNavigation()
            
            // Setup floating action button
            setupFab()
            
            // Show dashboard as default
            if (savedInstanceState == null) {
                loadFragment(DashboardFragment())
                binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
            }
            
            // Mark app as successfully launched
            setAppLaunchSuccess(true)
        } catch (e: Exception) {
            // Log or handle the exception
            Log.e(TAG, "Error during app initialization", e)
            e.printStackTrace()
            setAppLaunchSuccess(false)
            showResetDataDialog()
        }
    }

    private fun debugTransactionData() {
        try {
            val transactions = prefManager.getTransactions()
            Log.d(TAG, "Transaction count: ${transactions.size}")
            
            if (transactions.isEmpty()) {
                Log.d(TAG, "No transactions found. Adding a sample transaction.")
                // Add a sample transaction for testing if none exist
                val sampleTransaction = Transaction(
                    title = "Sample Income",
                    amount = 1000.0,
                    category = "Salary",
                    date = Date(),
                    type = TransactionType.INCOME,
                    notes = "Sample transaction added for testing"
                )
                prefManager.addTransaction(sampleTransaction)
                
                val sampleExpense = Transaction(
                    title = "Sample Expense",
                    amount = 500.0,
                    category = "Food",
                    date = Date(),
                    type = TransactionType.EXPENSE,
                    notes = "Sample expense added for testing"
                )
                prefManager.addTransaction(sampleExpense)
                
                // Verify transactions were added
                val updatedTransactions = prefManager.getTransactions()
                Log.d(TAG, "After adding samples - Transaction count: ${updatedTransactions.size}")
                
                // Also log full preferences state
                Log.d(TAG, prefManager.dumpPreferences())
            } else {
                Log.d(TAG, "Found ${transactions.size} transactions")
                transactions.forEachIndexed { index, transaction ->
                    Log.d(TAG, "Transaction $index: ${transaction.title}, ${transaction.amount}, ${transaction.type}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error debugging transaction data", e)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment())
                    true
                }
                R.id.nav_transactions -> {
                    loadFragment(SimpleTransactionsFragment())
                    true
                }
                R.id.nav_statistics -> {
                    loadFragment(StatisticsFragment())
                    true
                }
                R.id.nav_budget -> {
                    loadFragment(BudgetFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(DispatchersProfileFragment())
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
            
        // Hide fab on statistics and profile screens
        when (fragment) {
            is StatisticsFragment, is DispatchersProfileFragment -> binding.fabAddTransaction.hide()
            else -> binding.fabAddTransaction.show()
        }
    }

    private fun hasCrashed(): Boolean {
        val prefs = getSharedPreferences("app_crash_detection", MODE_PRIVATE)
        return !prefs.getBoolean("last_launch_successful", true)
    }
    
    private fun setAppLaunchSuccess(success: Boolean) {
        val prefs = getSharedPreferences("app_crash_detection", MODE_PRIVATE)
        prefs.edit().putBoolean("last_launch_successful", success).apply()
    }
    
    private fun showResetDataDialog() {
        if (hasEncounteredError) return // Prevent multiple dialogs
        
        hasEncounteredError = true
        AlertDialog.Builder(this)
            .setTitle("App Issue Detected")
            .setMessage("The app has encountered an issue that might be caused by corrupted data. Would you like to reset the app data to fix the problem?")
            .setPositiveButton("Reset Data") { _, _ ->
                // Clear all preferences and restart app
                resetAppData()
                Toast.makeText(this, "Data has been reset", Toast.LENGTH_SHORT).show()
                recreate()
            }
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun resetAppData() {
        // Clear preferences
        try {
            prefManager.clearAllData()
            // Reset crash detection
            setAppLaunchSuccess(true)
        } catch (e: Exception) {
            e.printStackTrace()
            // If even this fails, clear all app data directly
            getSharedPreferences("finance_tracker_prefs", MODE_PRIVATE).edit().clear().apply()
            getSharedPreferences("app_crash_detection", MODE_PRIVATE).edit().clear().apply()
        }
    }

    private fun setupFab() {
        binding.fabAddTransaction.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        auth.signOut()
        startLoginActivity()
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 