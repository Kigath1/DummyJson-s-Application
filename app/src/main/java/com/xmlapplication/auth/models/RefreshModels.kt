package com.xmlapplication.auth.models

import kotlinx.serialization.Serializable


@Serializable
data class RefreshTokenRequest(
    val refreshToken: String ?= null,
    val expiresInMins: Int = 30
)


@Serializable
data class RefreshTokenResponse(
    val refreshToken: String,
    val accessToken: String
)