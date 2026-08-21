package com.xmlapplication.auth.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val image: String,
    val phone: String? = null,
    val birthDate: String? = null,
    val address: Address? = null,
    val company: Company? = null
)

@Serializable
data class Address(
    val address: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String
)

@Serializable
data class Company(
    val name: String,
    val department: String,
    val title: String
)