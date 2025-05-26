package com.yasiruvithana.financetracker.database.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.yasiruvithana.financetracker.database.dao.CategoryDao
import com.yasiruvithana.financetracker.database.entities.CategoryEntity
import com.yasiruvithana.financetracker.model.Category
import com.yasiruvithana.financetracker.model.TransactionType


class CategoryRepository(private val categoryDao: CategoryDao) {

    // Get all categories as LiveData
    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories().map { entities ->
        entities.map { it.toCategory() }
    }

    // Get categories by type
    fun getCategoriesByType(type: TransactionType): LiveData<List<Category>> {
        return categoryDao.getCategoriesByType(type).map { entities ->
            entities.map { it.toCategory() }
        }
    }

    // Insert a category
    suspend fun insert(category: Category) {
        categoryDao.insert(CategoryEntity.fromCategory(category))
    }

    // Insert multiple categories
    suspend fun insertAll(categories: List<Category>) {
        val entities = categories.map { CategoryEntity.fromCategory(it) }
        categoryDao.insertAll(entities)
    }

    // Update a category
    suspend fun update(category: Category) {
        categoryDao.update(CategoryEntity.fromCategory(category))
    }

    // Delete a category
    suspend fun delete(category: Category) {
        categoryDao.delete(CategoryEntity.fromCategory(category))
    }

    // Delete a category by ID
    suspend fun deleteById(categoryId: String) {
        categoryDao.deleteById(categoryId)
    }

    // Get a category by ID
    suspend fun getCategoryById(categoryId: String): Category? {
        return categoryDao.getCategoryById(categoryId)?.toCategory()
    }

    // Get a category by name
    suspend fun getCategoryByName(name: String): Category? {
        return categoryDao.getCategoryByName(name)?.toCategory()
    }
} 