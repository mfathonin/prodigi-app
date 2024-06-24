package com.merahputihperkasa.prodigi.repository

sealed class LoadDataStatus<T>(
    val data: T? = null,
    val message: String? = null,
) {
    class Success<T>(data: T) : LoadDataStatus<T>(data)
    class Error<T>(message: String, data: T? = null) : LoadDataStatus<T>(data, message)
    class Loading<T> : LoadDataStatus<T>()
}
