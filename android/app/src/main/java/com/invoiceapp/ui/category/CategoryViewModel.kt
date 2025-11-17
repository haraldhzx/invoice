package com.invoiceapp.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invoiceapp.data.repository.CategoryRepository
import com.invoiceapp.model.Category
import com.invoiceapp.model.CategoryType
import com.invoiceapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<Resource<List<Category>>?>(null)
    val categoriesState: StateFlow<Resource<List<Category>>?> = _categoriesState.asStateFlow()

    private val _createState = MutableStateFlow<Resource<Category>?>(null)
    val createState: StateFlow<Resource<Category>?> = _createState.asStateFlow()

    private val _deleteState = MutableStateFlow<Resource<Unit>?>(null)
    val deleteState: StateFlow<Resource<Unit>?> = _deleteState.asStateFlow()

    fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories().collect { result ->
                _categoriesState.value = result
            }
        }
    }

    fun createCategory(
        name: String,
        type: CategoryType,
        icon: String?,
        color: String?
    ) {
        viewModelScope.launch {
            val category = Category(
                id = UUID.randomUUID(),
                name = name,
                type = type,
                icon = icon,
                color = color,
                description = null,
                parentId = null,
                isCustom = true,
                userId = null, // Will be set by backend
                createdAt = LocalDateTime.now()
            )
            categoryRepository.createCategory(category).collect { result ->
                _createState.value = result
                if (result is Resource.Success) {
                    loadCategories()
                }
            }
        }
    }

    fun deleteCategory(id: UUID) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(id).collect { result ->
                _deleteState.value = result
                if (result is Resource.Success) {
                    loadCategories()
                }
            }
        }
    }
}
