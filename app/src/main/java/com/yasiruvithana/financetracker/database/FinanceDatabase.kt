package com.yasiruvithana.financetracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yasiruvithana.financetracker.database.converters.DateConverter
import com.yasiruvithana.financetracker.database.converters.StringListConverter
import com.yasiruvithana.financetracker.database.converters.TransactionTypeConverter
import com.yasiruvithana.financetracker.database.dao.BudgetDao
import com.yasiruvithana.financetracker.database.dao.CategoryDao
import com.yasiruvithana.financetracker.database.dao.TransactionDao
import com.yasiruvithana.financetracker.database.entities.BudgetEntity
import com.yasiruvithana.financetracker.database.entities.CategoryEntity
import com.yasiruvithana.financetracker.database.entities.TransactionEntity
import com.yasiruvithana.financetracker.model.Category
import com.yasiruvithana.financetracker.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, BudgetEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, TransactionTypeConverter::class, StringListConverter::class)
abstract class FinanceDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                // Prepopulate with default categories
                                prepopulateCategories(database.categoryDao())
                            }
                        }
                    }
                })
                .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        private suspend fun prepopulateCategories(categoryDao: CategoryDao) {
            // Create default expense categories
            val expenseCategories = listOf(
                CategoryEntity(
                    id = Category.FOOD,
                    name = "Food",
                    type = TransactionType.EXPENSE,
                    iconResId = null
                ),
                CategoryEntity(
                    id = Category.TRANSPORT,
                    name = "Transport",
                    type = TransactionType.EXPENSE,
                    iconResId = null
                ),
                CategoryEntity(
                    id = Category.ENTERTAINMENT,
                    name = "Entertainment",
                    type = TransactionType.EXPENSE,
                    iconResId = null
                ),
                CategoryEntity(
                    id = Category.BILLS,
                    name = "Bills",
                    type = TransactionType.EXPENSE,
                    iconResId = null
                ),
                CategoryEntity(
                    id = Category.SHOPPING,
                    name = "Shopping",
                    type = TransactionType.EXPENSE,
                    iconResId = null
                ),
                CategoryEntity(
                    id = Category.HEALTH,
                    name = "Health",
                    type = TransactionType.EXPENSE,
                    iconResId = null
                ),
                CategoryEntity(
                    id = Category.OTHER,
                    name = "Other",
                    type = TransactionType.EXPENSE,
                    iconResId = null
                )
            )
            
            // Create default income categories
            val incomeCategories = listOf(
                CategoryEntity(
                    id = Category.SALARY,
                    name = "Salary",
                    type = TransactionType.INCOME,
                    iconResId = null
                ),
                CategoryEntity(
                    id = Category.GIFT,
                    name = "Gift",
                    type = TransactionType.INCOME,
                    iconResId = null
                ),
                CategoryEntity(
                    id = Category.INVESTMENT,
                    name = "Investment",
                    type = TransactionType.INCOME,
                    iconResId = null
                ),
                CategoryEntity(
                    id = "income_other",
                    name = "Other",
                    type = TransactionType.INCOME,
                    iconResId = null
                )
            )
            
            categoryDao.insertAll(expenseCategories + incomeCategories)
        }
    }
} 