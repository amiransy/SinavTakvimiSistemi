package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Kullanici(
    val id: Int,
    val kullaniciAdi: String,
    val eposta: String?,
    val rol: String
)

@Serializable
data class LoginRequest(
    val kullaniciAdi: String,
    val sifre: String
)