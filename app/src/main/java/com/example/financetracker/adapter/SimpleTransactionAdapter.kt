package com.example.financetracker.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.financetracker.R
import com.example.financetracker.model.Transaction
import com.example.financetracker.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale

class SimpleTransactionAdapter(
    context: Context,
    private val transactions: List<Transaction>,
    private val currency: String
) : ArrayAdapter<Transaction>(context, R.layout.item_transaction_simple, transactions) {
    
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    init {
        Log.d("SimpleAdapter", "Initialized with ${transactions.size} transactions")
        transactions.forEachIndexed { index, transaction ->
            Log.d("SimpleAdapter", "Transaction $index: ${transaction.title}, ${transaction.amount}")
        }
    }
    
    override fun getCount(): Int {
        val count = transactions.size
        Log.d("SimpleAdapter", "getCount(): returning $count items")
        return count
    }
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder
        
        if (convertView == null) {
            view = inflater.inflate(R.layout.item_transaction_simple, parent, false)
            
            holder = ViewHolder()
            holder.titleTextView = view.findViewById(R.id.text_transaction_title)
            holder.categoryTextView = view.findViewById(R.id.text_transaction_category)
            holder.dateTextView = view.findViewById(R.id.text_transaction_date)
            holder.amountTextView = view.findViewById(R.id.text_transaction_amount)
            
            view.tag = holder
            Log.d("SimpleAdapter", "Created new view holder for position: $position")
        } else {
            view = convertView
            holder = view.tag as ViewHolder
            Log.d("SimpleAdapter", "Reused view holder for position: $position")
        }
        
        // Get the transaction
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
        
        // Log for debugging
        Log.d("SimpleAdapter", "Bound transaction at position $position: ${transaction.title}, amount: $formattedAmount")
        
        return view
    }
    
    private class ViewHolder {
        lateinit var titleTextView: TextView
        lateinit var categoryTextView: TextView
        lateinit var dateTextView: TextView
        lateinit var amountTextView: TextView
    }
} 