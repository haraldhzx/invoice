package com.invoiceapp.ui.query

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invoiceapp.data.api.McpQueryRequest
import com.invoiceapp.data.api.McpQueryResponse
import com.invoiceapp.data.repository.QueryRepository
import com.invoiceapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QueryViewModel @Inject constructor(
    private val queryRepository: QueryRepository
) : ViewModel() {

    private val _queryState = MutableStateFlow<Resource<McpQueryResponse>?>(null)
    val queryState: StateFlow<Resource<McpQueryResponse>?> = _queryState.asStateFlow()

    private val _examplesState = MutableStateFlow<Resource<List<String>>?>(null)
    val examplesState: StateFlow<Resource<List<String>>?> = _examplesState.asStateFlow()

    private val _queryHistory = MutableStateFlow<List<Pair<String, McpQueryResponse>>>(emptyList())
    val queryHistory: StateFlow<List<Pair<String, McpQueryResponse>>> = _queryHistory.asStateFlow()

    fun submitQuery(query: String) {
        viewModelScope.launch {
            queryRepository.submitQuery(McpQueryRequest(query)).collect { result ->
                _queryState.value = result
                if (result is Resource.Success && result.data != null) {
                    _queryHistory.value = _queryHistory.value + (query to result.data)
                }
            }
        }
    }

    fun loadExamples() {
        viewModelScope.launch {
            queryRepository.getExamples().collect { result ->
                _examplesState.value = result
            }
        }
    }

    fun clearHistory() {
        _queryHistory.value = emptyList()
    }
}
