package com.example.baselibrary.api

data class BaseRepositoryError(
    val error: BaseErrorBody?
) {
    data class BaseErrorBody(
        val code: Int,
        val message: String?
    )
}