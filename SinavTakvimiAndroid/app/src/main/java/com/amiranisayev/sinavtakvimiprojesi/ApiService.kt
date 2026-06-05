package com.amiranisayev.sinavtakvimiprojesi

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.http.*

class ApiService {
    init {
        Log.d("DEBUG_ISTEK", "ApiService instance oluşturuldu!")
    }
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true
            prettyPrint=true
            isLenient=true
                coerceInputValues = true  // Uyumsuz tipleri varsayılan değerlere zorlar
                encodeDefaults = true}) // Backend'den gelen ekstra verileri görmezden gelir
        }
    }


    suspend fun getSinavTakvimi(): List<SinavDetay> {
        return client.get("http://10.0.2.2:8080/sinav-takvimi").body()
    }

    suspend fun login(username: String, password: String): Kullanici? {
        return try {
            Log.d("DEBUG_API", "İstek gönderiliyor: $username") // İstek gidiyor mu?
            val response = client.post("http://10.0.2.2:8080/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }

            if (response.status == HttpStatusCode.OK) {
                val kullanici= response.body<Kullanici>()
                Log.d("DEBUG_API", "Giriş Başarılı! Gelen Kullanıcı: $username")
                kullanici
            } else {
                Log.e("DEBUG_API", "Giriş Başarısız! Durum Kodu: ${response.status}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun sinavEkle(sinav: SinavTakvimi): String {
        return try {
            val response = client.post("http://10.0.2.2:8080/sinav-ekle") {
                contentType(ContentType.Application.Json)
                setBody(sinav)
            }
            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                response.body<String>()
            } else {
                "HATA"
            }
        } catch (e: Exception) {
            Log.e("API_HATA", "Ekleme hatası: ${e.message}")
            "HATA"
        }
    }

    suspend fun sinavSil(id: Int): Boolean {
        return try {

            val response = client.delete(
                "http://10.0.2.2:8080/sinav-sil/$id"
            )

            response.status == HttpStatusCode.OK || response.status==HttpStatusCode.NoContent

        } catch (e: Exception) {
            Log.e("API_HATA","Silme hatası: ${e.message}")
            false
        }
    }

    suspend fun getOturumlar(tarih: String = ""): List<Oturum> {
        val url = if (tarih.isEmpty()) {
            "http://10.0.2.2:8080/oturumlar"
        } else {
            "http://10.0.2.2:8080/oturumlar?tarih=$tarih"
        }
        return client.get(url).body()
    }
    suspend fun getBolumler(): List<Bolum> {
        return client.get("http://10.0.2.2:8080/bolumler").body()
    }
    suspend fun getDerslikler(): List<Derslik> {
        return client.get("http://10.0.2.2:8080/derslikler").body()
    }

    suspend fun getSinavlar(): List<SinavDetay> {
        return client.get("http://10.0.2.2:8080/sinavlar").body()
    }

    suspend fun gozetmenAta(sinavId: Int): Boolean {
        return try {
            val response = client.post("http://10.0.2.2:8080/gozetmen-ata") {
                contentType(ContentType.Application.Json)
                setBody(GozetmenAtama(sinavId))
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            Log.e("API_HATA", "Gözetmen atama hatası: ${e.message}")
            false
        }
    }

    suspend fun getGozetmenler(sinavId: Int): List<Personel> {
        return client.get("http://10.0.2.2:8080/gozetmenler/$sinavId").body()
    }

    suspend fun salonAta(sinavId: Int): Boolean {
        return try {
            val response = client.post("http://10.0.2.2:8080/salon-ata") {
                contentType(ContentType.Application.Json)
                setBody(GozetmenAtama(sinavId))
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            Log.e("API_HATA", "Salon atama hatası: ${e.message}")
            false
        }
    }

    suspend fun getDersler(bolumId: Int, yariyil: Int? = null): List<Ders> {
        val url = if (yariyil != null) {
            "http://10.0.2.2:8080/dersler/$bolumId?yariyil=$yariyil"
        } else {
            "http://10.0.2.2:8080/dersler/$bolumId"
        }
        return client.get(url).body()
    }

    suspend fun yedekAl(): Boolean {
        return try {
            val response = client.post("http://10.0.2.2:8080/yedek-al")
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            Log.e("API_HATA", "Yedekleme hatası: ${e.message}")
            false
        }
    }

    suspend fun getSalonDoluluk(): List<SalonDoluluk> {
        return client.get("http://10.0.2.2:8080/istatistik/salon-doluluk").body()
    }
}