package com.espimsystems.mespace.core.common.error

sealed class AppError(
    open val message: String? = null,
    open val cause: Throwable? = null,
) {

    data class Network(
        override val message: String? = null,
        override val cause: Throwable? = null,
    ) : AppError(message = message, cause = cause)

    data class Unauthorized(
        override val message: String? = null,
        override val cause: Throwable? = null,
    ) : AppError(message = message, cause = cause)

    data class Forbidden(
        override val message: String? = null,
        override val cause: Throwable? = null,
    ) : AppError(message = message, cause = cause)

    data class NotFound(
        override val message: String? = null,
        override val cause: Throwable? = null,
    ) : AppError(message = message, cause = cause)

    data class Validation(
        val field: String? = null,
        override val message: String? = null,
        override val cause: Throwable? = null,
    ) : AppError(message = message, cause = cause)

    data class Storage(
        override val message: String? = null,
        override val cause: Throwable? = null,
    ) : AppError(message = message, cause = cause)

    data class Unknown(
        override val message: String? = null,
        override val cause: Throwable? = null,
    ) : AppError(message = message, cause = cause)
}