package com.example.data.auth

/**
 * Google identity for SaaS Organization Manager. Firebase is optional;
 * Drive-backed mills still get a signed-in account after the Google picker.
 */
data class SignedInAccount(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val firebaseLinked: Boolean = false
)
