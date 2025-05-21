package com.example.financetracker.ui

import android.app.DatePickerDialog
import android.os.Bundle
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
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var prefManager: PreferenceManager
    private var selectedDate = Calendar.getInstance()
    private var selectedTransactionType = TransactionType.EXPENSE
    
    // Add the ViewModel for database operations
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefManager = PreferenceManager(this)
        
        setupTransactionTypeRadioGroup()
        setupCategoryDropdown()
        setupDateField()
        setupButtons()
        
        // Set amount currency prefix
        binding.inputLayoutAmount.prefixText = prefManager.getCurrency()
    }
    
    private fun setupTransactionTypeRadioGroup() {
        binding.radioGroupType.setOnCheckedChangeListener { _, checkedId ->
            selectedTransactionType = when (checkedId) {
                R.id.radio_expense -> TransactionType.EXPENSE
                R.id.radio_income -> TransactionType.INCOME
                else -> TransactionType.EXPENSE
            }
            
            // Update the categories dropdown based on selected type
            setupCategoryDropdown()
        }
    }
    
    private fun setupCategoryDropdown() {
        val categories = if (selectedTransactionType == TransactionType.EXPENSE) {
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
        
        // Clear selection if changing between income/expense
        binding.dropdownCategories.setText("", false)
    }
    
    private fun setupDateField() {
        // Set current date as default
        updateDateDisplay()
        
        binding.editDate.setOnClickListener {
            showDatePicker()
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
        
        // Show dialog
        datePickerDialog.show()
    }
    
    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                saveTransaction()
            }
        }
        
        binding.buttonCancel.setOnClickListener {
            finish()
        }
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
    
    private fun saveTransaction() {
        val title = binding.editTitle.text.toString().trim()
        val amount = binding.editAmount.text.toString().toDouble()
        val category = binding.dropdownCategories.text.toString().trim()
        val date = selectedDate.time
        
        val transaction = Transaction(
            title = title,
            amount = amount,
            category = category,
            date = date,
            type = selectedTransactionType
        )
        
        // Instead of using prefManager, use the database
        viewModel.insertTransaction(transaction)
        
        // Check budget and show warning if needed
        checkBudgetWarning()
        
        Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun checkBudgetWarning() {
        if (selectedTransactionType != TransactionType.EXPENSE) {
            return
        }
        
        lifecycleScope.launch {
            // Get the active budget from the database
            val budget = viewModel.activeBudget.value ?: return@launch
            
            // Calculate current month dates
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)
            
            // First day of month
            val startCal = Calendar.getInstance()
            startCal.set(currentYear, currentMonth, 1, 0, 0, 0)
            
            // Last day of month
            val endCal = Calendar.getInstance()
            endCal.set(currentYear, currentMonth, endCal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            
            // Get total expense for the month using suspend function
            val currentMonthExpenses = viewModel.getTotalExpenseInPeriod(startCal.time, endCal.time)
            
            // Calculate percentage of budget used
            val percentUsed = (currentMonthExpenses / budget.amount * 100).toInt()
            
            // Check if budget warning or alert needed
            if (prefManager.areBudgetAlertsEnabled()) {
                if (percentUsed >= 100) {
                    showBudgetExceededNotification()
                } else if (percentUsed >= budget.warningThreshold) {
                    showBudgetWarningNotification(percentUsed)
                }
            }
        }
    }
    
    private fun showBudgetWarningNotification(percent: Int) {
        Toast.makeText(
            this,
            getString(R.string.budget_warning, percent),
            Toast.LENGTH_LONG
        ).show()
    }
    
    private fun showBudgetExceededNotification() {
        Toast.makeText(
            this,
            getString(R.string.budget_exceeded),
            Toast.LENGTH_LONG
        ).show()
    }
}