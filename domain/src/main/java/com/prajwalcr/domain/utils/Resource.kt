package com.prajwalcr.domain.utils

sealed class Resource<T>(error: Throwable? = null, data: T? = null) {
    class Loading<T>: Resource<T>()

}