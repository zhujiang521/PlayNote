package com.zj.data.model

import android.util.Log

private const val TAG = "UiState"

/**
 * A sealed hierarchy describing the state of the text generation.
 */
sealed interface UiState<out R> {

    /**
     * Empty state when the screen is first shown
     */
    data object Initial : UiState<Nothing>

    /**
     * Still loading
     */
    data object Loading : UiState<Nothing>

    /**
     * Text has been generated
     */
    data class Success<out T>(val data: T) : UiState<T>

    /**
     * There was an error generating text
     */
    data class Error(val errorMessage: String) : UiState<Nothing> {
        init {
            Log.e(TAG, "errorMessage: $errorMessage")
        }
    }
}