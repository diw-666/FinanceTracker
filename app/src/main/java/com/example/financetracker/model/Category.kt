package com.example.financetracker.model


data class Category(
    val id: String,
    val name: String,
    val type: TransactionType,
    val iconResId: Int? = null
) {
    companion object {
        // Predefined expense categories
        val FOOD = "food"
        val TRANSPORT = "transport"
        val ENTERTAINMENT = "entertainment"
        val BILLS = "bills"
        val SHOPPING = "shopping"
        val HEALTH = "health"
        val OTHER = "other"
        
        // Predefined income categories
        val SALARY = "salary"
        val GIFT = "gift"
        val INVESTMENT = "investment"
        
        fun getDefaultCategories(): List<String> {
            return listOf(
                FOOD, TRANSPORT, ENTERTAINMENT, BILLS, 
                SHOPPING, HEALTH, OTHER, SALARY, GIFT, INVESTMENT
            )
        }
        
        fun getExpenseCategories(): List<String> {
            return listOf(FOOD, TRANSPORT, ENTERTAINMENT, BILLS, SHOPPING, HEALTH, OTHER)
        }
        
        fun getIncomeCategories(): List<String> {
            return listOf(SALARY, GIFT, INVESTMENT, OTHER)
        }
    }
} 