package com.amiranisayev.sinavtakvimiprojesi

import kotlinx.serialization.Serializable

@Serializable // Bu satır çok önemli! JSON verisini sınıfa çevirmeyi sağlar.
data class SinavTakvimi(
    val SinavID: Int?=0,
    val DersKodu: String?="",
    val DersAdi: String?="",
    val BolumAdi: String?="",
    val Tarih: String?="",
    val Oturum: String?="",
    val BaslangicSaat: String?="",
    val BitisSaat: String?="",
    val SalonAdi: String?="",
    val KatBilgisi: Int?=0
)

@Serializable
data class Oturum(
    val OturumID: Int,
    val Tanim: String,
    val BaslangicSaat: String,
    val BitisSaat: String,
    val bosMu: Boolean = true
)

@Serializable
data class Bolum(
    val BolumID: Int,
    val BolumAdi: String
)

@Serializable
data class Derslik(
    val DerslikID: Int,
    val Ad: String,
    val DerslikTuru: String,
    val Kapasite: Int,
    val KatBilgisi: Int? = null
)

@Serializable
data class Ders(
    val DersID: Int,
    val DersKodu: String,
    val Ad: String,
    val OgrenciSayisi: Int,
    val Yariyil: Int,
    val BolumID: Int
)


@Serializable
data class Personel(
    val PersonelID: Int,
    val Unvan: String,
    val Ad: String,
    val Soyad: String,
    val BolumID: Int
)

@Serializable
data class GozetmenAtama(
    val SinavID: Int
)

@Serializable
data class SinavDetay(
    val SinavID: Int,
    val DersKodu: String,
    val DersAdi: String,
    val BolumAdi: String,
    val Tarih: String,
    val Oturum: String,
    val BaslangicSaat: String,
    val BitisSaat: String,
    val SalonAdi: String?,
    val KatBilgisi: Int?,
    val Gozetmenler: List<String> = emptyList(),
    val gozetmenAtandi: Boolean = false // YENİ
)

@Serializable
data class SalonDoluluk(
    val DerslikID: Int,
    val SalonAdi: String,
    val DerslikTuru: String,
    val Kapasite: Int,
    val ToplamOgrenci: Int,
    val DolulukYuzdesi: Double,
    val DolulukDurumu: String
)