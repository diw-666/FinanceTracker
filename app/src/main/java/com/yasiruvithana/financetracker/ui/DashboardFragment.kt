package com.yasiruvithana.financetracker.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.yasiruvithana.financetracker.R
import com.yasiruvithana.financetracker.adapter.SimpleTransactionAdapter
import com.yasiruvithana.financetracker.data.PreferenceManager
import com.yasiruvithana.financetracker.databinding.FragmentDashboardBinding
import com.yasiruvithana.financetracker.model.Transaction
import com.yasiruvithana.financetracker.model.TransactionType
import com.yasiruvithana.financetracker.ui.viewmodels.MainViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefManager: PreferenceManager
    private lateinit var transactionAdapter: SimpleTransactionAdapter
    
    // Use the shared MainViewModel
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefManager = PreferenceManager(requireContext())
        
        // Observe transactions from the ViewModel
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            updateUI(transactions)
        }
        
        // Observe the active budget
        viewModel.activeBudget.observe(viewLifecycleOwner) { budget ->
            updateBudgetCard(budget)
        }
        
        // Setup view all button
        binding.textViewAll.setOnClickListener {
            (requireActivity() as com.yasiruvithana.financetracker.MainActivity).bottomNavigation.selectedItemId = 
                R.id.nav_transactions
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshActiveBudget()
    }

    private fun setupTransactionsList(transactions: List<Transaction>) {
        val recentTransactions = transactions
            .sortedByDescending { it.date }
            .take(5) // Only show 5 most recent transactions
            
        transactionAdapter = SimpleTransactionAdapter(requireContext(), recentTransactions, prefManager.getCurrency())
        
        // Change RecyclerView to ListView
        val listView = binding.recyclerTransactions as ListView
        listView.adapter = transactionAdapter
        
        // Setup item click listener
        listView.setOnItemClickListener { _, _, position, _ ->
            val transaction = recentTransactions[position]
            openTransactionDetails(transaction)
        }
        
        // Show/hide empty state
        updateEmptyState(recentTransactions)
    }
    
    private fun openTransactionDetails(transaction: Transaction) {
        val intent = Intent(requireContext(), TransactionDetailActivity::class.java).apply {
            putExtra(TransactionDetailActivity.EXTRA_TRANSACTION_ID, transaction.id)
        }
        startActivity(intent)
    }
    
    private fun updateEmptyState(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            binding.textNoTransactions.visibility = View.VISIBLE
            binding.recyclerTransactions.visibility = View.GONE
        } else {
            binding.textNoTransactions.visibility = View.GONE
            binding.recyclerTransactions.visibility = View.VISIBLE
        }
    }

    private fun updateUI(transactions: List<Transaction>) {
        setupTransactionsList(transactions)
        updateBalanceCard(transactions)
        updateCategorySummary(transactions)
    }

    private fun updateBalanceCard(transactions: List<Transaction>) {
        lifecycleScope.launch {
            val currency = prefManager.getCurrency()
            val numberFormat = NumberFormat.getCurrencyInstance()
            
            // Calculate total income, expenses, and balance using the ViewModel
            val totalIncome = viewModel.getTotalIncome()
            val totalExpenses = viewModel.getTotalExpense()
            val balance = viewModel.getBalance()
            
            // Format and display values
            val formattedIncome = numberFormat.format(totalIncome)
                .replace(numberFormat.currency?.symbol ?: "$", currency)
            val formattedExpenses = numberFormat.format(totalExpenses)
                .replace(numberFormat.currency?.symbol ?: "$", currency)
            val formattedBalance = numberFormat.format(balance)
                .replace(numberFormat.currency?.symbol ?: "$", currency)
            
            binding.textIncome.text = formattedIncome
            binding.textExpenses.text = formattedExpenses
            binding.textBalance.text = formattedBalance
        }
    }

    private fun updateBudgetCard(budget: com.yasiruvithana.financetracker.model.Budget?) {
        lifecycleScope.launch {
            val currency = prefManager.getCurrency()
            val numberFormat = NumberFormat.getCurrencyInstance()
            
            if (budget != null) {
                // Calculate current month's expenses
                val calendar = Calendar.getInstance()
                val startOfMonth = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                val endOfMonth = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                
                // Get total expenses for current month
                val currentMonthExpenses = viewModel.getTotalExpenseInPeriod(startOfMonth, endOfMonth)
                
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
                
                binding.textBudgetSpent.text = "$formattedSpent used"
                binding.textBudgetTotal.text = "of $formattedTotal"
                
                // Set progress bar color based on percentage
                val colorRes = when {
                    percentUsed >= 100 -> R.color.budget_danger
                    percentUsed >= budget.warningThreshold -> R.color.budget_warning
                    else -> R.color.budget_safe
                }
                binding.progressBudget.setIndicatorColor(requireContext().getColor(colorRes))
            } else {
                // No budget set
                binding.progressBudget.progress = 0
                binding.textBudgetSpent.text = "$currency 0.00 used"
                binding.textBudgetTotal.text = "of $currency 0.00"
            }
        }
    }

    private fun updateCategorySummary(transactions: List<Transaction>) {
        // Filter expense transactions only
        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
        
        if (expenseTransactions.isEmpty()) {
            binding.textNoExpenses.visibility = View.VISIBLE
            binding.chartCategorySummary.visibility = View.GONE
            return
        }
        
        binding.textNoExpenses.visibility = View.GONE
        binding.chartCategorySummary.visibility = View.VISIBLE
        
        // Group transactions by category and sum amounts
        val categoryTotals = expenseTransactions
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            
        // Create pie chart entries
        val entries = categoryTotals.map { (category, amount) -> 
            // Format category name to capitalize first letter
            val formattedCategory = category.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase() else it.toString() 
            }
            PieEntry(amount.toFloat(), formattedCategory)
        }
        
        if (entries.isEmpty()) {
            binding.textNoExpenses.visibility = View.VISIBLE
            binding.chartCategorySummary.visibility = View.GONE
            return
        }
        
        // Set up dataset
        val dataSet = PieDataSet(entries, "")
        
        // Add colors
        val colors = ColorTemplate.MATERIAL_COLORS.toList() + 
                     ColorTemplate.VORDIPLOM_COLORS.toList()
        dataSet.colors = colors
        
        // Configure the data set
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        
        // Create pie data
        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(binding.chartCategorySummary))
        pieData.setValueTextSize(11f)
        pieData.setValueTextColor(Color.WHITE)
        
        // Configure the chart
        binding.chartCategorySummary.apply {
            data = pieData
            description.isEnabled = false
            setUsePercentValues(true)
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(11f)
            setDrawCenterText(false)
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            setDrawEntryLabels(false)
            legend.isEnabled = true
            setExtraOffsets(20f, 0f, 20f, 0f)
            
            // Animate
            animateY(1400, Easing.EaseInOutQuad)
            
            // Refresh
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 