package com.yasiruvithana.financetracker.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.yasiruvithana.financetracker.FinanceApplication
import com.yasiruvithana.financetracker.LoginActivity
import com.yasiruvithana.financetracker.R
import com.yasiruvithana.financetracker.data.PreferenceManager
import com.yasiruvithana.financetracker.databinding.FragmentProfileBinding
import com.yasiruvithana.financetracker.model.Transaction
import com.yasiruvithana.financetracker.model.TransactionType
import com.yasiruvithana.financetracker.ui.viewmodels.TransactionViewModel
import java.text.NumberFormat
import java.util.*

class DispatchersProfileFragment : Fragment() {

    companion object {
        private const val TAG = "ProfileFragment"
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefManager: PreferenceManager
    private lateinit var auth: FirebaseAuth
    private var transactions: List<Transaction> = emptyList()
    
    // Create ViewModel to interact with database
    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModel.TransactionViewModelFactory(
            (requireActivity().application as FinanceApplication).transactionRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d(TAG, "onViewCreated: Initializing profile view")
        prefManager = PreferenceManager(requireContext())
        auth = FirebaseAuth.getInstance()
        
        setupProfile()
        setupSettings()
        setupLogoutButton()
        
        // Observe transactions from database through ViewModel
        transactionViewModel.allTransactions.observe(viewLifecycleOwner) { transactionList ->
            transactions = transactionList
            Log.d(TAG, "Observed ${transactions.size} transactions from database")
            refreshFinancialSummary()
        }
    }

    private fun setupProfile() {
        // Get current user details
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.textUsername.text = currentUser.displayName ?: "User"
            binding.textEmail.text = currentUser.email
        } else {
            binding.textUsername.text = "Guest User"
            binding.textEmail.text = "Not signed in"
        }
    }
    
    private fun setupSettings() {
        binding.cardSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }
        
        binding.cardAbout.setOnClickListener {
            showAboutDialog()
        }
        
        binding.cardHelp.setOnClickListener {
            showHelpDialog()
        }
    }

    private fun setupLogoutButton() {
        binding.buttonLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
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
        // Navigate to login screen
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }
    
    private fun refreshFinancialSummary() {
        try {
            Log.d(TAG, "refreshFinancialSummary: Processing ${transactions.size} transactions")
            setupFinancialSummary()
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing financial summary", e)
            Toast.makeText(requireContext(), "Failed to load financial summary", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupFinancialSummary() {
        Log.d(TAG, "setupFinancialSummary: Processing ${transactions.size} transactions")
        
        val currency = prefManager.getCurrency()
        val numberFormat = NumberFormat.getCurrencyInstance()
        
        // Calculate total income, expenses, and balance
        var totalIncome = 0.0
        var totalExpenses = 0.0
        
        transactions.forEach { transaction ->
            when (transaction.type) {
                TransactionType.INCOME -> totalIncome += transaction.amount
                TransactionType.EXPENSE -> totalExpenses += transaction.amount
            }
        }
        
        Log.d(TAG, "setupFinancialSummary: Total income: $totalIncome, Total expenses: $totalExpenses")
        
        val balance = totalIncome - totalExpenses
        
        // Format and display values
        val formattedIncome = numberFormat.format(totalIncome)
            .replace(numberFormat.currency?.symbol ?: "$", currency)
        val formattedExpenses = numberFormat.format(totalExpenses)
            .replace(numberFormat.currency?.symbol ?: "$", currency)
        val formattedBalance = numberFormat.format(balance)
            .replace(numberFormat.currency?.symbol ?: "$", currency)
        
        binding.textTotalIncome.text = formattedIncome
        binding.textTotalExpenses.text = formattedExpenses
        binding.textTotalBalance.text = formattedBalance
        
        // Calculate savings rate
        val savingsRate = if (totalIncome > 0) {
            ((totalIncome - totalExpenses) / totalIncome * 100).toInt()
        } else {
            0
        }
        
        binding.textSavingsRate.text = "$savingsRate%"
        
        // Show transaction counts
        binding.textTransactionCount.text = "${transactions.size} transactions"
        
        val incomeCount = transactions.count { it.type == TransactionType.INCOME }
        val expenseCount = transactions.count { it.type == TransactionType.EXPENSE }
        
        binding.textIncomeCount.text = "$incomeCount income"
        binding.textExpenseCount.text = "$expenseCount expenses"
        
        Log.d(TAG, "setupFinancialSummary: Financial summary updated successfully")
    }
    
    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("About")
            .setMessage("FinanceTracker v1.0\n\nA modern personal finance tracking application built with Material Design 3.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showHelpDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Help")
            .setMessage("Need assistance with the app?\n\n" +
                    "• Dashboard: View your financial overview\n" +
                    "• Transactions: Manage your income and expenses\n" +
                    "• Statistics: Analyze your spending habits\n" +
                    "• Budget: Set and track your monthly budget\n\n" +
                    "Tap the '+' button to add a new transaction.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 