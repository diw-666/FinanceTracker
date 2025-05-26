package com.yasiruvithana.financetracker.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yasiruvithana.financetracker.FinanceApplication
import com.yasiruvithana.financetracker.R
import com.yasiruvithana.financetracker.data.PreferenceManager
import com.yasiruvithana.financetracker.databinding.FragmentStatisticsBinding
import com.yasiruvithana.financetracker.model.Transaction
import com.yasiruvithana.financetracker.model.TransactionType
import com.yasiruvithana.financetracker.ui.viewmodels.TransactionViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class StatisticsFragment : Fragment() {

    companion object {
        private const val TAG = "StatisticsFragment"
    }

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefManager: PreferenceManager
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
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefManager = PreferenceManager(requireContext())
        
        Log.d(TAG, "onViewCreated: Initializing statistics view")
        
        // Observe transactions from database through ViewModel
        transactionViewModel.allTransactions.observe(viewLifecycleOwner) { transactionList ->
            transactions = transactionList
            Log.d(TAG, "Observed ${transactions.size} transactions from database")
            refreshData()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Refreshing data")
        if (::prefManager.isInitialized) {
            refreshData()
        } else {
            Log.e(TAG, "onResume: prefManager not initialized")
            prefManager = PreferenceManager(requireContext())
            refreshData()
        }
    }
    
    private fun refreshData() {
        try {
            Log.d(TAG, "refreshData: Processing ${transactions.size} transactions")
            
            setupCategoryChart()
            setupMonthlyTrendChart()
            setupIncomeVsExpensesChart()
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing statistics data", e)
            Toast.makeText(requireContext(), "Failed to load statistics", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupCategoryChart() {
        // Filter expense transactions
        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
        
        if (expenseTransactions.isEmpty()) {
            binding.textNoDataCategories.visibility = View.VISIBLE
            binding.chartCategories.visibility = View.GONE
            return
        }
        
        binding.textNoDataCategories.visibility = View.GONE
        binding.chartCategories.visibility = View.VISIBLE
        
        // Group transactions by category and sum amounts
        val categoryTotals = expenseTransactions
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            
        // Create pie chart entries
        val entries = categoryTotals.map { (category, amount) -> 
            val formattedCategory = category.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase() else it.toString() 
            }
            PieEntry(amount.toFloat(), formattedCategory)
        }
        
        // Set up dataset
        val dataSet = PieDataSet(entries, "")
        
        // Set colors
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList() + 
                         ColorTemplate.VORDIPLOM_COLORS.toList()
                         
        // Configure pie data
        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(binding.chartCategories))
        pieData.setValueTextSize(11f)
        pieData.setValueTextColor(Color.WHITE)
        
        // Configure chart
        binding.chartCategories.apply {
            data = pieData
            description.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            holeRadius = 35f
            transparentCircleRadius = 40f
            setDrawCenterText(true)
            centerText = getString(R.string.expenses_by_category)
            setDrawEntryLabels(false)
            legend.isEnabled = true
            animateY(1000, Easing.EaseInOutQuad)
        }
        
        // Refresh the chart
        binding.chartCategories.invalidate()
    }
    
    private fun setupMonthlyTrendChart() {
        if (transactions.isEmpty()) {
            binding.textNoDataMonthly.visibility = View.VISIBLE
            binding.chartMonthlyTrend.visibility = View.GONE
            return
        }
        
        binding.textNoDataMonthly.visibility = View.GONE
        binding.chartMonthlyTrend.visibility = View.VISIBLE
        
        // Get the last 6 months
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val months = mutableListOf<String>()
        val monthlyExpenses = mutableListOf<Float>()
        val monthlyIncome = mutableListOf<Float>()
        
        // Go back 5 months to show 6 months total (including current)
        calendar.add(Calendar.MONTH, -5)
        
        for (i in 0 until 6) {
            val startOfMonth = Calendar.getInstance().apply {
                time = calendar.time
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            val endOfMonth = Calendar.getInstance().apply {
                time = calendar.time
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            
            // Get transactions for this month
            val monthTransactions = transactions.filter { 
                val date = it.date
                date in startOfMonth.time..endOfMonth.time
            }
            
            // Calculate income and expenses
            val income = monthTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
                .toFloat()
                
            val expenses = monthTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
                .toFloat()
                
            // Add data for this month
            months.add(monthFormat.format(calendar.time))
            monthlyIncome.add(income)
            monthlyExpenses.add(expenses)
            
            // Move to next month
            calendar.add(Calendar.MONTH, 1)
        }
        
        // Create bar entries
        val incomeEntries = monthlyIncome.mapIndexed { index, amount -> 
            BarEntry(index.toFloat(), amount)
        }
        
        val expenseEntries = monthlyExpenses.mapIndexed { index, amount -> 
            BarEntry(index.toFloat(), amount)
        }
        
        // Create datasets
        val incomeDataSet = BarDataSet(incomeEntries, getString(R.string.income))
        incomeDataSet.color = requireContext().getColor(R.color.income)
        
        val expenseDataSet = BarDataSet(expenseEntries, getString(R.string.expenses))
        expenseDataSet.color = requireContext().getColor(R.color.expense)
        
        // Combine datasets
        val barData = BarData(incomeDataSet, expenseDataSet)
        barData.barWidth = 0.3f
        
        // Group bars
        val groupSpace = 0.4f
        val barSpace = 0f
        barData.groupBars(0f, groupSpace, barSpace)
        
        // Configure chart
        binding.chartMonthlyTrend.apply {
            data = barData
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            isDoubleTapToZoomEnabled = false
            setPinchZoom(false)
            
            // X-axis setup
            xAxis.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return months.getOrNull(value.toInt()) ?: ""
                    }
                }
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setCenterAxisLabels(true)
                axisMinimum = 0f
                axisMaximum = months.size.toFloat()
            }
            
            // Format Y-axis
            axisLeft.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value == 0f) "0" else "${value.toInt()}"
                    }
                }
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            
            // Animate
            animateY(1000)
        }
        
        // Refresh chart
        binding.chartMonthlyTrend.invalidate()
    }
    
    private fun setupIncomeVsExpensesChart() {
        val currency = prefManager.getCurrency()
        val numberFormat = NumberFormat.getCurrencyInstance()
        
        if (transactions.isEmpty()) {
            binding.textNoDataComparison.visibility = View.VISIBLE
            binding.chartIncomeVsExpenses.visibility = View.GONE
            binding.textIncomeAmount.text = "$currency 0"
            binding.textExpensesAmount.text = "$currency 0"
            binding.textNetAmount.text = "$currency 0"
            return
        }
        
        binding.textNoDataComparison.visibility = View.GONE
        binding.chartIncomeVsExpenses.visibility = View.VISIBLE
        
        // Calculate total income and expenses
        val income = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
            
        val expenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
            
        val net = income - expenses
        
        // Format currency values
        val formattedIncome = numberFormat.format(income)
            .replace(numberFormat.currency?.symbol ?: "$", currency)
        val formattedExpenses = numberFormat.format(expenses)
            .replace(numberFormat.currency?.symbol ?: "$", currency)
        val formattedNet = numberFormat.format(net)
            .replace(numberFormat.currency?.symbol ?: "$", currency)
            
        binding.textIncomeAmount.text = formattedIncome
        binding.textExpensesAmount.text = formattedExpenses
        binding.textNetAmount.text = formattedNet
        
        // Create pie chart entries for income vs expenses
        val entries = listOf(
            PieEntry(income.toFloat(), getString(R.string.income)),
            PieEntry(expenses.toFloat(), getString(R.string.expenses))
        )
        
        // Set up dataset
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            requireContext().getColor(R.color.income),
            requireContext().getColor(R.color.expense)
        )
        
        // Configure pie data
        val pieData = PieData(dataSet)
        pieData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val formatted = numberFormat.format(value.toDouble())
                    .replace(numberFormat.currency?.symbol ?: "$", currency)
                return formatted
            }
        })
        pieData.setValueTextSize(11f)
        pieData.setValueTextColor(Color.WHITE)
        
        // Configure chart
        binding.chartIncomeVsExpenses.apply {
            data = pieData
            description.isEnabled = false
            setUsePercentValues(false)
            isDrawHoleEnabled = true
            holeRadius = 35f
            transparentCircleRadius = 40f
            setDrawCenterText(true)
            centerText = getString(R.string.income_vs_expenses)
            setDrawEntryLabels(false)
            legend.isEnabled = true
            animateY(1000, Easing.EaseInOutQuad)
        }
        
        // Refresh chart
        binding.chartIncomeVsExpenses.invalidate()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 