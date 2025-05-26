package com.yasiruvithana.financetracker.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.yasiruvithana.financetracker.FinanceApplication
import com.yasiruvithana.financetracker.R
import com.yasiruvithana.financetracker.adapter.TransactionRecyclerAdapter
import com.yasiruvithana.financetracker.data.PreferenceManager
import com.yasiruvithana.financetracker.model.Transaction
import com.yasiruvithana.financetracker.ui.viewmodels.TransactionViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SimpleTransactionsFragment : Fragment() {
    
    private lateinit var recyclerTransactions: RecyclerView
    private lateinit var textTransactionCount: TextView
    private lateinit var searchEditText: TextInputEditText
    private lateinit var sortButton: MaterialButton
    private lateinit var prefManager: PreferenceManager
    
    private var allTransactions: List<Transaction> = emptyList()
    private var filteredTransactions: List<Transaction> = emptyList()
    private var currentSortOption = SortOption.DATE_DESC
    
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
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transactions_simple, container, false)
        
        recyclerTransactions = view.findViewById(R.id.recycler_transactions)
        textTransactionCount = view.findViewById(R.id.text_transaction_count)
        searchEditText = view.findViewById(R.id.search_transactions)
        sortButton = view.findViewById(R.id.button_sort)
        
        prefManager = PreferenceManager(requireContext())
        
        setupSearch()
        setupSortButton()
        
        // Observe transactions from database through ViewModel
        transactionViewModel.allTransactions.observe(viewLifecycleOwner) { transactionList ->
            allTransactions = transactionList
            Log.d(TAG, "Observed ${allTransactions.size} transactions from database")
            filterAndSortTransactions()
        }
        
        return view
    }
    
    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                filterAndSortTransactions()
            }
        })
    }
    
    private fun setupSortButton() {
        sortButton.setOnClickListener { view ->
            showSortMenu(view)
        }
    }
    
    private fun showSortMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_transaction_sort, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sort_date_newest -> {
                    currentSortOption = SortOption.DATE_DESC
                    filterAndSortTransactions()
                    true
                }
                R.id.sort_date_oldest -> {
                    currentSortOption = SortOption.DATE_ASC
                    filterAndSortTransactions()
                    true
                }
                R.id.sort_amount_highest -> {
                    currentSortOption = SortOption.AMOUNT_DESC
                    filterAndSortTransactions()
                    true
                }
                R.id.sort_amount_lowest -> {
                    currentSortOption = SortOption.AMOUNT_ASC
                    filterAndSortTransactions()
                    true
                }
                R.id.sort_title_az -> {
                    currentSortOption = SortOption.TITLE_ASC
                    filterAndSortTransactions()
                    true
                }
                R.id.sort_title_za -> {
                    currentSortOption = SortOption.TITLE_DESC
                    filterAndSortTransactions()
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    private fun filterAndSortTransactions() {
        // First filter by search query
        val query = searchEditText.text.toString().trim().lowercase()
        filteredTransactions = if (query.isEmpty()) {
            allTransactions
        } else {
            allTransactions.filter { transaction ->
                transaction.title.lowercase().contains(query) ||
                transaction.category.lowercase().contains(query) ||
                transaction.notes.lowercase().contains(query) ||
                transaction.amount.toString().contains(query)
            }
        }
        
        // Then sort according to current sort option
        filteredTransactions = when (currentSortOption) {
            SortOption.DATE_DESC -> filteredTransactions.sortedByDescending { it.date }
            SortOption.DATE_ASC -> filteredTransactions.sortedBy { it.date }
            SortOption.AMOUNT_DESC -> filteredTransactions.sortedByDescending { it.amount }
            SortOption.AMOUNT_ASC -> filteredTransactions.sortedBy { it.amount }
            SortOption.TITLE_ASC -> filteredTransactions.sortedBy { it.title.lowercase() }
            SortOption.TITLE_DESC -> filteredTransactions.sortedByDescending { it.title.lowercase() }
        }
        
        updateUI()
    }
    
    private fun updateUI() {
        // Set transaction count text
        val countText = if (filteredTransactions.size == 1) {
            "1 transaction"
        } else {
            "${filteredTransactions.size} transactions"
        }
        
        if (allTransactions.size != filteredTransactions.size) {
            textTransactionCount.text = "$countText (filtered from ${allTransactions.size})"
        } else {
            textTransactionCount.text = countText
        }
        
        // Create and set adapter
        val adapter = TransactionRecyclerAdapter(
            requireContext(),
            filteredTransactions,
            prefManager.getCurrency()
        ) { transaction ->
            openTransactionDetails(transaction)
        }
        
        recyclerTransactions.adapter = adapter
        
        // Log
        Log.d(TAG, "Updated UI with adapter for ${filteredTransactions.size} transactions")
    }
    
    private fun openTransactionDetails(transaction: Transaction) {
        val intent = Intent(requireContext(), TransactionDetailActivity::class.java).apply {
            putExtra(TransactionDetailActivity.EXTRA_TRANSACTION_ID, transaction.id)
        }
        startActivity(intent)
    }
    
    enum class SortOption {
        DATE_DESC,  // Newest first (default)
        DATE_ASC,   // Oldest first
        AMOUNT_DESC, // Highest first
        AMOUNT_ASC,  // Lowest first
        TITLE_ASC,   // A to Z
        TITLE_DESC   // Z to A
    }
    
    companion object {
        private const val TAG = "TransactionsFragment"
    }
} 