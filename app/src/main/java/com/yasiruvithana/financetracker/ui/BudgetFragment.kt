package com.yasiruvithana.financetracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.yasiruvithana.financetracker.R
import com.yasiruvithana.financetracker.data.PreferenceManager
import com.yasiruvithana.financetracker.databinding.FragmentBudgetBinding
import com.yasiruvithana.financetracker.model.Budget
import com.yasiruvithana.financetracker.model.Category
import com.yasiruvithana.financetracker.model.Transaction
import com.yasiruvithana.financetracker.model.TransactionType
import com.yasiruvithana.financetracker.ui.viewmodels.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import java.text.NumberFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefManager: PreferenceManager
    
    // Use the shared MainViewModel
    private val viewModel: MainViewModel by activityViewModels()
    
    // Store current expenses and budget for UI updates
    private var currentMonthExpenses: Double = 0.0
    private var currentBudget: Budget? = null
    private var allTransactions: List<Transaction> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefManager = PreferenceManager(requireContext())
        
        // Observe transactions from database
        viewModel.allTransactions.observe(viewLifecycleOwner, Observer { transactions ->
            allTransactions = transactions
            calculateCurrentMonthExpenses()
            updateBudgetUI()
        })
        
        // Observe active budget from database
        viewModel.activeBudget.observe(viewLifecycleOwner, Observer { budget ->
            currentBudget = budget
            updateBudgetUI()
        })
        
        setupEditBudgetButton()
        setupCategoryBudgets()
        
        // Refresh active budget
        viewModel.refreshActiveBudget()
    }
    
    private fun calculateCurrentMonthExpenses() {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        currentMonthExpenses = allTransactions
            .filter { transaction -> 
                val transactionCal = Calendar.getInstance().apply {
                    time = transaction.date
                }
                transaction.type == TransactionType.EXPENSE &&
                transactionCal.get(Calendar.MONTH) == currentMonth &&
                transactionCal.get(Calendar.YEAR) == currentYear
            }
            .sumOf { it.amount }
    }
    
    private fun updateBudgetUI() {
        val budget = currentBudget
        val currency = prefManager.getCurrency()
        val numberFormat = NumberFormat.getCurrencyInstance()
        
        if (budget != null) {
            // Calculate percentage of budget used
            val percentUsed = if (budget.amount > 0) {
                (currentMonthExpenses / budget.amount * 100).toInt().coerceAtMost(100)
            } else {
                0
            }
            
            // Update progress bar and text
            binding.progressBudget.progress = percentUsed
            
            // Format and display values
            val formattedSpent = numberFormat.format(currentMonthExpenses)
                .replace(numberFormat.currency?.symbol ?: "$", currency)
            val formattedTotal = numberFormat.format(budget.amount)
                .replace(numberFormat.currency?.symbol ?: "$", currency)
            
            binding.textSpentAmount.text = formattedSpent
            binding.textTotalAmount.text = formattedTotal
            binding.textPercentUsed.text = "$percentUsed%"
            
            // Set progress bar color based on percentage
            val colorRes = when {
                percentUsed >= 100 -> R.color.budget_danger
                percentUsed >= budget.warningThreshold -> R.color.budget_warning
                else -> R.color.budget_safe
            }
            binding.progressBudget.setIndicatorColor(requireContext().getColor(colorRes))
            
            // Update warning threshold
            binding.textWarningThreshold.text = "Warning at ${budget.warningThreshold}%"
            
        } else {
            // No budget set - show default state
            binding.progressBudget.progress = 0
            binding.textSpentAmount.text = "${currency}0"
            binding.textTotalAmount.text = "${currency}0"
            binding.textPercentUsed.text = "0%"
            binding.textWarningThreshold.text = "Warning at 80%"
        }
    }
    
    private fun setupEditBudgetButton() {
        binding.buttonEditBudget.setOnClickListener {
            showBudgetDialog()
        }
        
        binding.sliderWarningThreshold.addOnChangeListener { _, value, _ ->
            binding.textWarningThreshold.text = "Warning at ${value.toInt()}%"
        }
    }
    
    private fun showBudgetDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_edit_budget, null)
        val amountInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_budget_amount)
        val thresholdSlider = view.findViewById<Slider>(R.id.slider_threshold)
        
        // Set current values if budget exists
        if (currentBudget != null) {
            amountInput.setText(currentBudget!!.amount.toString())
            thresholdSlider.value = currentBudget!!.warningThreshold.toFloat()
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Monthly Budget")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val amountText = amountInput.text.toString()
                if (amountText.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                try {
                    val amount = amountText.toDouble()
                    val warningThreshold = thresholdSlider.value.toInt()
                    
                    if (amount <= 0) {
                        Toast.makeText(requireContext(), "Amount must be greater than zero", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    // Create new budget
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.DAY_OF_MONTH, 1) // First day of month
                    val startDate = calendar.time
                    
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                    val endDate = calendar.time
                    
                    val budget = Budget(
                        amount = amount,
                        startDate = startDate,
                        endDate = endDate,
                        warningThreshold = warningThreshold
                    )
                    
                    // Save to database
                    // If we already have a budget, delete it first, then insert the new one
                    if (currentBudget != null) {
                        // Since we can't directly update the budget (no direct ID access),
                        // we'll handle it in two steps
                        viewModel.deleteBudget(currentBudget!!)
                        viewModel.insertBudget(budget)
                    } else {
                        // Insert new budget
                        viewModel.insertBudget(budget)
                    }
                    
                    Toast.makeText(requireContext(), "Budget updated", Toast.LENGTH_SHORT).show()
                } catch (e: NumberFormatException) {
                    Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun setupCategoryBudgets() {
        // Handle category-specific budgets in a future update
        binding.buttonSetCategoryBudgets.setOnClickListener {
            Toast.makeText(requireContext(), "Coming in the next update!", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 