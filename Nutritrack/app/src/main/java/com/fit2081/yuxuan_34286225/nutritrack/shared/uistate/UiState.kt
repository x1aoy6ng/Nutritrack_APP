package com.fit2081.yuxuan_34286225.nutritrack.shared.uistate

// for generative ai
sealed interface UiState {
    /**
     * Empty state when the screen is first shown
     */
    object Initial: UiState

    /**
     * Still loading
     */
    object Loading: UiState

    /**
     * Text has been generated
     */
    data class Success(val outputText: String): UiState

    /**
     * There was an error generating text
     */
    data class Error(val errorMessage: String): UiState
}