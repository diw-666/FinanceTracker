package com.example.financetracker.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.R
import com.example.financetracker.model.Transaction
import com.example.financetracker.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionRecyclerAdapter(
    private val context: Context,
    private val transactions: List<Transaction>,
    private val currency: String,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionRecyclerAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    init {
        Log.d("TransactionAdapter", "Initialized with ${transactions.size} transactions")
        transactions.forEachIndexed { index, transaction ->
            Log.d("TransactionAdapter", "Transaction $index: ${transaction.title}, ${transaction.amount}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_transaction_simple, parent, false)
        Log.d("TransactionAdapter", "Created new ViewHolder")
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        
        // Set title and category
        holder.titleTextView.text = transaction.title
        holder.categoryTextView.text = transaction.category
        
        // Format and set date
        holder.dateTextView.text = dateFormat.format(transaction.date)
        
        // Format amount
        val amount = transaction.amount
        val formattedAmount: String
        
        if (transaction.type == TransactionType.EXPENSE) {
            formattedAmount = "-$currency$amount"
            holder.amountTextView.setTextColor(Color.parseColor("#E53935")) // Red
        } else {
            formattedAmount = "+$currency$amount"
            holder.amountTextView.setTextColor(Color.parseColor("#43A047")) // Green
        }
        
        holder.amountTextView.text = formattedAmount
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(transaction)
        }
        
        // Log for debugging
        Log.d("TransactionAdapter", "Bound transaction at position $position: ${transaction.title}, amount: $formattedAmount")
    }

    override fun getItemCount(): Int {
        val count = transactions.size
        Log.d("TransactionAdapter", "getItemCount(): returning $count items")
        return count
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.text_transaction_title)
        val categoryTextView: TextView = itemView.findViewById(R.id.text_transaction_category)
        val dateTextView: TextView = itemView.findViewById(R.id.text_transaction_date)
        val amountTextView: TextView = itemView.findViewById(R.id.text_transaction_amount)
    }
} 