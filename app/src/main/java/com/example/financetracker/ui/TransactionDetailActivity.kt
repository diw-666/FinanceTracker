package com.example.financetracker.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.financetracker.R
import com.example.financetracker.data.PreferenceManager
import com.example.financetracker.databinding.ActivityAddTransactionBinding
import com.example.financetracker.model.Category
import com.example.financetracker.model.Transaction
import com.example.financetracker.model.TransactionType
import com.example.financetracker.ui.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

class TransactionDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRANSACTION_ID = "transaction_id"
    }
    
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var prefManager: PreferenceManager
    private lateinit var transaction: Transaction
    
    private var selectedDate = Calendar.getInstance()
    
    // Add the ViewModel for database operations
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Transaction Details"
        
        prefManager = PreferenceManager(this)
        
        // Get transaction ID from intent
        val transactionId = intent.getStringExtra(EXTRA_TRANSACTION_ID)
            ?: throw IllegalArgumentException("Transaction ID is required")
        
        // Find transaction by ID using database
        lifecycleScope.launch {
            val fetchedTransaction = viewModel.getTransactionById(transactionId)
            if (fetchedTransaction != null) {
                transaction = fetchedTransaction
                setupUI()
                setupListeners()
            } else {
                Toast.makeText(this@TransactionDetailActivity, "Transaction not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun setupUI() {
        // Set title
        binding.textTitle.text = "Edit Transaction"
        
        // Set transaction type
        when (transaction.type) {
            TransactionType.EXPENSE -> binding.radioExpense.isChecked = true
            TransactionType.INCOME -> binding.radioIncome.isChecked = true
        }
        
        // Disable changing transaction type during edit
        binding.radioGroupType.isEnabled = false
        binding.radioExpense.isEnabled = false
        binding.radioIncome.isEnabled = false
        
        // Setup category dropdown
        val categories = if (transaction.type == TransactionType.EXPENSE) {
            Category.getExpenseCategories()
        } else {
            Category.getIncomeCategories()
        }
        
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        
        binding.dropdownCategories.setAdapter(adapter)
        
        // Set transaction details
        binding.editTitle.setText(transaction.title)
        binding.editAmount.setText(transaction.amount.toString())
        binding.dropdownCategories.setText(transaction.category, false)
        
        // Set transaction date
        selectedDate.time = transaction.date
        updateDateDisplay()
        
        // Set currency
        binding.inputLayoutAmount.prefixText = prefManager.getCurrency()
        
        // Change save button text to update
        binding.buttonSave.text = "Update"
        
        // Change cancel button text to delete
        binding.buttonCancel.text = "Delete"
    }
    
    private fun setupListeners() {
        // Setup date picker
        binding.editDate.setOnClickListener {
            showDatePicker()
        }
        
        // Setup save button
        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                updateTransaction()
            }
        }
        
        // Setup delete button
        binding.buttonCancel.setOnClickListener {
            showDeleteConfirmation()
        }
    }
    
    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.editDate.setText(dateFormat.format(selectedDate.time))
    }
    
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateDisplay()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        // Validate title
        val title = binding.editTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.inputLayoutTitle.error = "Title is required"
            isValid = false
        } else {
            binding.inputLayoutTitle.error = null
        }
        
        // Validate amount
        val amountStr = binding.editAmount.text.toString().trim()
        if (amountStr.isEmpty()) {
            binding.inputLayoutAmount.error = "Amount is required"
            isValid = false
        } else {
            try {
                val amount = amountStr.toDouble()
                if (amount <= 0) {
                    binding.inputLayoutAmount.error = "Amount must be greater than zero"
                    isValid = false
                } else {
                    binding.inputLayoutAmount.error = null
                }
            } catch (e: NumberFormatException) {
                binding.inputLayoutAmount.error = "Invalid amount"
                isValid = false
            }
        }
        
        // Validate category
        val category = binding.dropdownCategories.text.toString().trim()
        if (category.isEmpty()) {
            binding.inputLayoutCategory.error = "Category is required"
            isValid = false
        } else {
            binding.inputLayoutCategory.error = null
        }
        
        return isValid
    }
    
    private fun updateTransaction() {
        // Update transaction data
        transaction.title = binding.editTitle.text.toString().trim()
        transaction.amount = binding.editAmount.text.toString().toDouble()
        transaction.category = binding.dropdownCategories.text.toString().trim()
        transaction.date = selectedDate.time
        
        // Save updated transaction to database
        viewModel.updateTransaction(transaction)
        
        Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTransaction()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteTransaction() {
        // Delete transaction from database
        viewModel.deleteTransaction(transaction)
        
        Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 