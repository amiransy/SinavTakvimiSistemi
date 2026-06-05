package com.amiranisayev.sinavtakvimiprojesi

import kotlinx.serialization.Serializable

// Backend'den gelen kullanıcı bilgileri için
@Serializable
data class Kullanici(
    val id: Int,
    val kullaniciAdi: String,
    val eposta: String?,
    val rol: String
)

// Giriş yaparken gönderdiğimiz bilgiler için
@Serializable
data class LoginRequest(
    val kullaniciAdi: String,
    val sifre: String
)