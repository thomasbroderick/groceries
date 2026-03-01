package dev.samhain.groceries.dto

data class AuthUrlResponse(
    val authorizationUrl: String,
    val state: String
)

data class CallbackRequest(
    val code: String,
    val state: String
)
