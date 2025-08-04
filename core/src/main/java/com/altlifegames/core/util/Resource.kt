package com.altlifegames.core.util

/**
 * A sealed class representing the state of a resource provided by repositories.
 * Use this to expose loading, success and error states to the UI in a type‑safe manner.
 */
sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val throwable: Throwable) : Resource<Nothing>()
}