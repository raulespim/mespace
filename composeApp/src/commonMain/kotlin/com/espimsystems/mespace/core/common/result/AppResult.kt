package com.espimsystems.mespace.core.common.result

import com.espimsystems.mespace.core.common.error.AppError

sealed interface AppResult<out T> {

    data class Success<T>(
        val data: T,
    ) : AppResult<T>

    data class Failure(
        val error: AppError,
    ) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(
    transform: (T) -> R,
): AppResult<R> {
    return when (this) {
        is AppResult.Success -> AppResult.Success(transform(data))
        is AppResult.Failure -> this
    }
}

inline fun <T, R> AppResult<T>.flatMap(
    transform: (T) -> AppResult<R>,
): AppResult<R> {
    return when (this) {
        is AppResult.Success -> transform(data)
        is AppResult.Failure -> this
    }
}

inline fun <T> AppResult<T>.onSuccess(
    action: (T) -> Unit,
): AppResult<T> {
    if (this is AppResult.Success) {
        action(data)
    }

    return this
}

inline fun <T> AppResult<T>.onFailure(
    action: (AppError) -> Unit,
): AppResult<T> {
    if (this is AppResult.Failure) {
        action(error)
    }

    return this
}

fun <T> AppResult<T>.getOrNull(): T? {
    return when (this) {
        is AppResult.Success -> data
        is AppResult.Failure -> null
    }
}

fun <T> AppResult<T>.errorOrNull(): AppError? {
    return when (this) {
        is AppResult.Success -> null
        is AppResult.Failure -> error
    }
}