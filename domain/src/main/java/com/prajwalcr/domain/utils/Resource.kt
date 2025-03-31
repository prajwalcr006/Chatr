package com.prajwalcr.domain.utils

sealed class Resource<T>(error: Throwable? = null, data: T? = null) {
    class Loading<T>: Resource<T>()
    class Success<T>(val data: T?): Resource<T>(data = data)
    class Error<T>(val error: Throwable): Resource<T>(error = error)
}