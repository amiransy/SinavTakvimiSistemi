package com.example.routes

import com.example.config.DatabaseFactory
import com.example.models.Bolum
import com.example.models.BolumlerTable
import com.example.models.Ders
import com.example.models.DerslerTable
import com.example.models.Derslik
import com.example.models.DersliklerTable
import com.example.models.GozetmenAtama
import com.example.models.GozetmenAtamalarTable
import com.example.models.Kullanici
import com.example.models.KullaniciTable
import com.example.models.LoginRequest
import com.example.models.Oturum
import com.example.models.OturumlarTable
import com.example.models.Personel
import com.example.models.PersonelTable
import com.example.models.SinavDetay
import com.example.models.SinavSalonlariTable
import com.example.models.SinavTakvimi
import com.example.models.SinavTakvimiTable
import com.example.models.SinavlarTable
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import io.ktor.server.request.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.time.LocalDate
import java.time.LocalTime
import com.example.models.SalonDoluluk

fun String.fixEncoding(): String {
    return try {
        String(this.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
    } catch (e: Exception) {
        this
    }
}
fun Application.configureRouting() {
    environment.monitor.subscribe(ApplicationStarted) {
        println(">>> Routing yapılandırıldı!")
    }
    routing {
        get("/") {
            call.respondText("Sınav Sistemi API Çalışıyor!")
        }
        get("/sinav-takvimi") {
            val veriler = try {
                transaction(DatabaseFactory.viewerDb) {
                    SinavTakvimiTable.selectAll().map { row ->
                        val sinavID = row[SinavTakvimiTable.SinavID]

                        // Her sınav için gözetmenleri getir
                        val gozetmenler = (GozetmenAtamalarTable innerJoin PersonelTable)
                            .select {
                                GozetmenAtamalarTable.SınavSalonID inSubQuery
                                        SinavSalonlariTable
                                            .slice(SinavSalonlariTable.AtamaID)
                                            .select { SinavSalonlariTable.SinavID eq sinavID }
                            }
                            .distinctBy { it[PersonelTable.PersonelID] }
                            .map { personelRow ->
                                "${personelRow[PersonelTable.Unvan].fixEncoding()} ${personelRow[PersonelTable.Ad].fixEncoding()} ${personelRow[PersonelTable.Soyad].fixEncoding()}"
                            }

                        SinavDetay(
                            SinavID = sinavID,
                            DersKodu = row[SinavTakvimiTable.DersKodu].fixEncoding(),
                            DersAdi = row[SinavTakvimiTable.DersAdi].fixEncoding(),
                            BolumAdi = row[SinavTakvimiTable.BolumAdi].fixEncoding(),
                            Tarih = row[SinavTakvimiTable.Tarih].toString(),
                            Oturum = row[SinavTakvimiTable.Oturum].fixEncoding(),
                            BaslangicSaat = row[SinavTakvimiTable.BaslangicSaat].toString(),
                            BitisSaat = row[SinavTakvimiTable.BitisSaat].toString(),
                            SalonAdi = row[SinavTakvimiTable.SalonAdi]?.fixEncoding() ?: "",
                            KatBilgisi = row[SinavTakvimiTable.KatBilgisi] ?: 0,
                            Gozetmenler = gozetmenler,
                            gozetmenAtandi = gozetmenler.isNotEmpty()
                        )
                    }
                }
            } catch (e: Exception) {
                println("Sorgu hatası: ${e.message}")
                null
            }
            if (veriler != null) {
                call.respond(veriler)
            } else {
                call.respondText("Veri çekilemedi, konsola bak!")
            }
        }
        get("/oturumlar") {
            val tarihParam = call.request.queryParameters["tarih"]
            val tarih = if (tarihParam != null) LocalDate.parse(tarihParam) else null

            val oturumlar = try {
                transaction {
                    OturumlarTable.selectAll().map { row ->
                        val oturumID = row[OturumlarTable.OturumID]
                        val doluMu = if (tarih != null) {
                            SinavlarTable.select {
                                (SinavlarTable.OturumID eq oturumID) and
                                        (SinavlarTable.Tarih eq tarih)
                            }.count() > 0
                        } else {
                            false
                        }
                        Oturum(
                            OturumID = oturumID,
                            Tanim = row[OturumlarTable.Tanim].fixEncoding(),
                            BaslangicSaat = row[OturumlarTable.BaslangicSaat].toString(),
                            BitisSaat = row[OturumlarTable.BitisSaat].toString(),
                            bosMu = !doluMu
                        )
                    }
                }
            } catch (e: Exception) {
                println("Oturum sorgu hatası: ${e.message}")
                null
            }
            if (oturumlar != null) {
                call.respond(oturumlar)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Oturumlar çekilemedi!")
            }
        }

        get("/bolumler") {
            val bolumler = try {
                transaction {
                    BolumlerTable.selectAll().map { row ->
                        Bolum(
                            BolumID = row[BolumlerTable.BolumID],
                            BolumAdi = row[BolumlerTable.BolumAdi].fixEncoding()
                        )
                    }
                }
            } catch (e: Exception) {
                println("Bölüm sorgu hatası: ${e.message}")
                null
            }
            if (bolumler != null) {
                call.respond(bolumler)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Bölümler çekilemedi!")
            }
        }

        get("/derslikler") {
            val derslikler = try {
                transaction {
                    DersliklerTable.selectAll().map { row ->
                        Derslik(
                            DerslikID = row[DersliklerTable.DerslikID],
                            Ad = row[DersliklerTable.Ad].fixEncoding(),
                            DerslikTuru = row[DersliklerTable.DerslikTuru].fixEncoding(),
                            Kapasite = row[DersliklerTable.Kapasite],
                            KatBilgisi = row[DersliklerTable.KatBilgisi]
                        )
                    }
                }
            } catch (e: Exception) {
                println("Derslik sorgu hatası: ${e.message}")
                null
            }
            if (derslikler != null) {
                call.respond(derslikler)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Derslikler çekilemedi!")
            }
        }

        get("/dersler/{bolumId}") {
            val bolumId = call.parameters["bolumId"]?.toIntOrNull()
            val yariyil = call.request.queryParameters["yariyil"]?.toIntOrNull()

            if (bolumId == null) {
                call.respond(HttpStatusCode.BadRequest, "Geçersiz bölüm ID")
                return@get
            }
            val dersler = try {
                transaction(DatabaseFactory.viewerDb) {
                    DerslerTable.select {
                        if (yariyil != null) {
                            (DerslerTable.BolumID eq bolumId) and (DerslerTable.Yariyil eq yariyil)
                        } else {
                            DerslerTable.BolumID eq bolumId
                        }
                    }.map { row ->
                        Ders(
                            DersID = row[DerslerTable.DersID],
                            DersKodu = row[DerslerTable.DersKodu].fixEncoding(),
                            Ad = row[DerslerTable.Ad].fixEncoding(),
                            OgrenciSayisi = row[DerslerTable.OgrenciSayisi],
                            Yariyil = row[DerslerTable.Yariyil],
                            BolumID = row[DerslerTable.BolumID]
                        )
                    }
                }
            } catch (e: Exception) {
                println("Ders listesi hatası: ${e.message}")
                null
            }
            if (dersler != null) {
                call.respond(dersler)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Dersler çekilemedi!")
            }
        }
        loginRouting()
        adminRouting()
    }
}
fun Route.loginRouting() {
    get("/istatistik/salon-doluluk") {
        val veriler = try {
            transaction(DatabaseFactory.viewerDb) {
                DersliklerTable.selectAll().map { derslikRow ->
                    val derslikID = derslikRow[DersliklerTable.DerslikID]
                    val kapasite = derslikRow[DersliklerTable.Kapasite]

                    val toplamOgrenci = try {
                        val conn = TransactionManager.current().connection
                        val rs = conn.prepareStatement(
                            """
                        SELECT ISNULL(SUM(d.OgrenciSayisi), 0)
                        FROM Sınav_Salonları ss
                        INNER JOIN Sınavlar s ON ss.SinavID = s.SinavID
                        INNER JOIN Dersler d ON s.DersID = d.DersID
                        WHERE ss.DerslikID = $derslikID
                        """.trimIndent(),
                            false
                        ).executeQuery()
                        if (rs.next()) rs.getInt(1) else 0
                    } catch (e: Exception) {
                        0
                    }

                    val dolulukYuzdesi = if (kapasite > 0) {
                        (toplamOgrenci.toDouble() / kapasite.toDouble() * 100.0)
                            .coerceAtMost(100.0)
                    } else 0.0

                    val dolulukDurumu = when {
                        dolulukYuzdesi >= 80.0 -> "Dolu"
                        dolulukYuzdesi >= 40.0 -> "Orta"
                        else -> "Boş"
                    }

                    SalonDoluluk(
                        DerslikID = derslikID,
                        SalonAdi = derslikRow[DersliklerTable.Ad].fixEncoding(),
                        DerslikTuru = derslikRow[DersliklerTable.DerslikTuru].fixEncoding(),
                        Kapasite = kapasite,
                        ToplamOgrenci = toplamOgrenci,
                        DolulukYuzdesi = Math.round(dolulukYuzdesi * 10.0) / 10.0,
                        DolulukDurumu = dolulukDurumu
                    )
                }
            }
        } catch (e: Exception) {
            println("Salon doluluk hatası: ${e.message}")
            e.printStackTrace()
            null
        }
        if (veriler != null) {
            call.respond(veriler)
        } else {
            call.respond(HttpStatusCode.InternalServerError, "Salon doluluk verisi çekilemedi!")
        }
    }

    post("/login") {
        try {
            val request = call.receive<LoginRequest>()

            // Önce viewerDb ile kullanıcıyı buluyor
            val user = transaction(DatabaseFactory.viewerDb) {
                KullaniciTable.select {
                    (KullaniciTable.kullaniciAdi eq request.kullaniciAdi) and
                            (KullaniciTable.sifre eq request.sifre)
                }.map {
                    Kullanici(
                        id = it[KullaniciTable.id],
                        kullaniciAdi = it[KullaniciTable.kullaniciAdi],
                        eposta = it[KullaniciTable.eposta],
                        rol = it[KullaniciTable.rol]
                    )
                }.singleOrNull()
            }

            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Kullanıcı adı veya şifre hatalı!")
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Geçersiz istek formatı!")
        }
    }
}
fun Route.adminRouting() {
    post("/sinav-ekle") {
        println(">>> /sinav-ekle endpoint'ine istek geldi!")
        try {
            val yeniSinav = call.receive<SinavTakvimi>()
            var yeniSinavID: Int = 0
            val uyariMesaji = mutableListOf<String>()

            // Transaction içinde sadece sınav ekle
            transaction(DatabaseFactory.adminDb) {
                val bolumID = BolumlerTable
                    .select { BolumlerTable.BolumAdi eq yeniSinav.BolumAdi }
                    .map { it[BolumlerTable.BolumID] }
                    .singleOrNull()
                    ?: throw Exception("Bölüm bulunamadı: ${yeniSinav.BolumAdi}")

                val mevcutDersID = DerslerTable
                    .select {
                        (DerslerTable.DersKodu eq yeniSinav.DersKodu) and
                                (DerslerTable.BolumID eq bolumID)
                    }
                    .map { it[DerslerTable.DersID] }
                    .firstOrNull()

                val dersID = mevcutDersID
                    ?: throw Exception("Ders bulunamadi: ${yeniSinav.DersKodu}")
                val oturumID = yeniSinav.Oturum.trim().toIntOrNull()
                    ?: throw Exception("Geçersiz OturumID: ${yeniSinav.Oturum}")

                val tarih = LocalDate.parse(yeniSinav.Tarih.trim())

                val yariyil = DerslerTable
                    .select { DerslerTable.DersID eq dersID }
                    .map { it[DerslerTable.Yariyil] }
                    .singleOrNull() ?: 1

                val conn = TransactionManager.current().connection

                // Yarıyıl çakışma kontrolü
                val yariyilCakisma = conn.prepareStatement(
                    "SELECT dbo.fn_YariyilCakismaVarMi($yariyil, '$tarih', $oturumID, $bolumID)",
                    false
                ).executeQuery()
                yariyilCakisma.next()
                if (yariyilCakisma.getInt(1) == 1) {
                    throw Exception("Bu yarıyılda aynı oturumda zaten bir sınav var! Lütfen farklı oturum seçin.")
                }

                // Günlük limit kontrolü
                val gunlukLimit = conn.prepareStatement(
                    "SELECT dbo.fn_GunlukSinavLimitiAsildiMi($yariyil, '$tarih', $bolumID)",
                    false
                ).executeQuery()
                gunlukLimit.next()
                if (gunlukLimit.getInt(1) == 1) {
                    uyariMesaji.add("UYARI: Bu dönem için aynı güne 2'den fazla sınav eklenmiştir!")
                }

                // Sınavlar tablosuna ekle
                yeniSinavID = SinavlarTable.insert {
                    it[DersID] = dersID
                    it[Tarih] = tarih
                    it[OturumID] = oturumID
                } get SinavlarTable.SinavID
            }

            // Transaction dışında salon atama
            transaction(DatabaseFactory.adminDb) {
                val conn = TransactionManager.current().connection
                println(">>> sp_AkilliSalonAtama çağrılıyor, SinavID: $yeniSinavID")
                conn.prepareStatement(
                    "EXEC sp_AkilliSalonAtama @SinavID = $yeniSinavID",
                    false
                ).executeUpdate()
                println(">>> sp_AkilliSalonAtama tamamlandı!")
            }

            if (uyariMesaji.isNotEmpty()) {
                call.respond(HttpStatusCode.Created, "Başarıyla eklendi. ${uyariMesaji.joinToString(" ")}")
            } else {
                call.respond(HttpStatusCode.Created, "Başarıyla eklendi")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Hata: ${e.localizedMessage}")
        }
    }
    delete("/sinav-sil/{id}") {
        try {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                transaction(DatabaseFactory.adminDb) {
                    val conn = TransactionManager.current().connection

                    // 1. Önce gözetmen atamalarını sil
                    conn.prepareStatement(
                        "DELETE FROM Gozetmen_Atamaları WHERE SınavSalonID IN (SELECT AtamaID FROM Sınav_Salonları WHERE SinavID = $id)",
                        false
                    ).executeUpdate()

                    // 2. Sonra salon atamalarını sil
                    conn.prepareStatement(
                        "DELETE FROM Sınav_Salonları WHERE SinavID = $id",
                        false
                    ).executeUpdate()

                    // 3. En son sınavı sil
                    conn.prepareStatement(
                        "DELETE FROM Sınavlar WHERE SinavID = $id",
                        false
                    ).executeUpdate()
                }
                call.respond(HttpStatusCode.OK, "Sınav başarıyla silindi")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Geçersiz ID")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Silme Hatası: ${e.localizedMessage}")
        }
    }


    // Sınavları listele (gözetmen atama için)
    get("/sinavlar") {
        val sinavlar = try {
            transaction(DatabaseFactory.viewerDb) {
                SinavTakvimiTable.selectAll().map { row ->
                    val sinavID = row[SinavTakvimiTable.SinavID]

                    val gozetmenVar = GozetmenAtamalarTable
                        .innerJoin(SinavSalonlariTable)
                        .select { SinavSalonlariTable.SinavID eq sinavID }
                        .count() > 0

                    SinavDetay(
                        SinavID = sinavID,
                        DersKodu = row[SinavTakvimiTable.DersKodu].fixEncoding(),
                        DersAdi = row[SinavTakvimiTable.DersAdi].fixEncoding(),
                        BolumAdi = row[SinavTakvimiTable.BolumAdi].fixEncoding(),
                        Tarih = row[SinavTakvimiTable.Tarih].toString(),
                        Oturum = row[SinavTakvimiTable.Oturum].fixEncoding(),
                        BaslangicSaat = row[SinavTakvimiTable.BaslangicSaat].toString(),
                        BitisSaat = row[SinavTakvimiTable.BitisSaat].toString(),
                        SalonAdi = row[SinavTakvimiTable.SalonAdi]?.fixEncoding() ?: "",
                        KatBilgisi = row[SinavTakvimiTable.KatBilgisi] ?: 0,
                        Gozetmenler = emptyList(),
                        gozetmenAtandi = gozetmenVar
                    )
                }
            }
        } catch (e: Exception) {
            println("Sınav listesi hatası: ${e.message}")
            null
        }
        if (sinavlar != null) {
            call.respond(sinavlar)
        } else {
            call.respond(HttpStatusCode.InternalServerError, "Sınavlar çekilemedi!")
        }
    }


    // Gözetmen atama stored procedure'ünü çağır
    post("/gozetmen-ata") {
        try {
            val atama = call.receive<GozetmenAtama>()
            transaction(DatabaseFactory.adminDb) {
                val conn = TransactionManager.current().connection
                conn.prepareStatement(
                    "EXEC sp_GozetmenAtamaSistemi @SinavID = ${atama.SinavID}",
                    false
                ).executeUpdate()
            }
            call.respond(HttpStatusCode.OK, "Gözetmen ataması başarıyla tamamlandı!")
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Hata: ${e.localizedMessage}")
        }
    }


    // Sınava atanan gözetmenleri listele
    get("/gozetmenler/{sinavId}") {
        val sinavId = call.parameters["sinavId"]?.toIntOrNull()
        if (sinavId == null) {
            call.respond(HttpStatusCode.BadRequest, "Geçersiz sınav ID")
            return@get
        }
        val gozetmenler = try {
            transaction(DatabaseFactory.viewerDb) {
                (GozetmenAtamalarTable innerJoin PersonelTable)
                    .select {
                        GozetmenAtamalarTable.SınavSalonID inSubQuery
                                SinavSalonlariTable
                                    .slice(SinavSalonlariTable.AtamaID)
                                    .select { SinavSalonlariTable.SinavID eq sinavId }
                    }
                    .distinctBy { it[PersonelTable.PersonelID] }
                    .map { row ->
                        Personel(
                            PersonelID = row[PersonelTable.PersonelID],
                            Unvan = row[PersonelTable.Unvan].fixEncoding(),
                            Ad = row[PersonelTable.Ad].fixEncoding(),
                            Soyad = row[PersonelTable.Soyad].fixEncoding(),
                            BolumID = row[PersonelTable.BolumID]
                        )
                    }
            }
        } catch (e: Exception) {
            println("Gözetmen listesi hatası: ${e.message}")
            null
        }
        if (gozetmenler != null) {
            call.respond(gozetmenler)
        } else {
            call.respond(HttpStatusCode.InternalServerError, "Gözetmenler çekilemedi!")
        }
    }

    post("/salon-ata") {
        try {
            val atama = call.receive<GozetmenAtama>()
            transaction(DatabaseFactory.adminDb) {
                exec("EXEC sp_AkilliSalonAtama @SinavID = ${atama.SinavID}")
            }
            call.respond(HttpStatusCode.OK, "Salon ataması başarıyla tamamlandı!")
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Hata: ${e.localizedMessage}")
        }
    }

    post("/yedek-al") {
        try {
            transaction(DatabaseFactory.adminDb) {
                val conn = TransactionManager.current().connection
                conn.prepareStatement(
                    "EXEC sp_VeritabaniYedekle",
                    false
                ).executeUpdate()
            }
            call.respond(HttpStatusCode.OK, "Yedekleme başarıyla tamamlandı!")
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Hata: ${e.localizedMessage}")
        }
    }
}