package com.example.techsmart.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techsmart.data.model.Producto
import com.example.techsmart.data.repository.TechsmartRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface CatalogUiState {
    object Loading : CatalogUiState
    data class Success(val productos: List<Producto>) : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}

class CatalogViewModel(
    private val repository: TechsmartRepository = TechsmartRepository()
) : ViewModel() {

    val uiState: StateFlow<CatalogUiState> = repository.getProductosStream()
        .map { result ->
            result.fold(
                onSuccess = { CatalogUiState.Success(it) },
                onFailure = { CatalogUiState.Error(it.localizedMessage ?: "Error desconocido") }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CatalogUiState.Loading
        )
}