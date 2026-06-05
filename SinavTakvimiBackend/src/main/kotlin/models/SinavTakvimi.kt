package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.time


@Serializable
data class SalonDoluluk(
    val DerslikID: Int,
    val SalonAdi: String,
    val DerslikTuru: String,
    val Kapasite: Int,
    val ToplamOgrenci: Int,
    val DolulukYuzdesi: Double,
    val DolulukDurumu: String  // "Boş", "Orta", "Dolu"
)
// 1. Mobil uygulamanın (JSON olarak) alacağı veri yapısı
@Serializable
data class SinavTakvimi(
    val SinavID: Int,
    val DersKodu: String,
    val DersAdi: String,
    val BolumAdi: String,
    val Tarih: String,
    val Oturum: String,
    val BaslangicSaat: String,
    val BitisSaat: String,
    val SalonAdi: String,
    val KatBilgisi: Int
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
    val KatBilgisi: Int?
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

// 2. SQL'deki View ile Kotlin arasındaki bağlantı nesnesi
object SinavTakvimiTable : Table("v_SinavTakvimi") {
    val SinavID = integer("SinavID")
    val DersKodu = varchar("DersKodu", 50)
    val DersAdi = varchar("DersAdi", 100)
    val BolumAdi = varchar("BolumAdi", 100)
    val Tarih = date("Tarih")
    val Oturum = varchar("Oturum", 50)
    val BaslangicSaat = time("BaslangicSaat")
    val BitisSaat = time("BitisSaat")
    val SalonAdi = varchar("SalonAdi", 50).nullable()
    val KatBilgisi = integer("KatBilgisi").nullable()

    // View'larda primary key olmaz ama Exposed için birini işaretlemeliyiz
    override val primaryKey = PrimaryKey(SinavID)
}

object DerslerTable : Table("Dersler") {
    val DersID = integer("DersID").autoIncrement()
    val DersKodu = varchar("DersKodu", 50)
    val DersTuru = varchar("DersTuru", 50)
    val Ad = varchar("Ad", 100)
    val OgrenciSayisi = integer("OgrenciSayisi")
    val Yariyil = integer("Yariyil")
    val BolumID = integer("BolumID")
    override val primaryKey = PrimaryKey(DersID)
}

object OturumlarTable : Table("Oturumlar") {
    val OturumID = integer("OturumID").autoIncrement()
    val Tanim = varchar("Tanim", 50)
    val BaslangicSaat = time("BaslangicSaat")
    val BitisSaat = time("BitisSaat")
    override val primaryKey = PrimaryKey(OturumID)
}

object BolumlerTable : Table("Bolumler") {
    val BolumID = integer("BolumID").autoIncrement()
    val BolumAdi = varchar("BolumAdi", 100)
    override val primaryKey = PrimaryKey(BolumID)
}


object SinavlarTable : Table("Sınavlar") {
    val SinavID = integer("SinavID").autoIncrement()
    val DersID = integer("DersID")
    val Tarih = date("Tarih")
    val OturumID = integer("OturumID")
    override val primaryKey = PrimaryKey(SinavID)
}

object SinavSalonlariTable : Table("Sınav_Salonları") {
    val AtamaID = integer("AtamaID").autoIncrement()
    val SinavID = integer("SinavID")
    val DerslikID = integer("DerslikID")
    override val primaryKey = PrimaryKey(AtamaID)
}

object DersliklerTable : Table("Derslikler") {
    val DerslikID = integer("DerslikID").autoIncrement()
    val Ad = varchar("Ad", 50)
    val DerslikTuru = varchar("DerslikTuru", 50)
    val Kapasite = integer("Kapasite")
    val Aktif = bool("Aktif")
    val KatBilgisi = integer("KatBilgisi")
    override val primaryKey = PrimaryKey(DerslikID)
}

object PersonelTable : Table("Personel") {
    val PersonelID = integer("PersonelID").autoIncrement()
    val Unvan = varchar("Unvan", 50)
    val Ad = varchar("Ad", 50)
    val Soyad = varchar("Soyad", 50)
    val BolumID = integer("BolumID")
    override val primaryKey = PrimaryKey(PersonelID)
}

object GozetmenAtamalarTable : Table("Gozetmen_Atamaları") {
    val GozetmenAtamaID = integer("GozetmenAtamaID").autoIncrement()
    val SınavSalonID = integer("SınavSalonID") references SinavSalonlariTable.AtamaID
    val PersonelID = integer("PersonelID") references PersonelTable.PersonelID
    override val primaryKey = PrimaryKey(GozetmenAtamaID)
}