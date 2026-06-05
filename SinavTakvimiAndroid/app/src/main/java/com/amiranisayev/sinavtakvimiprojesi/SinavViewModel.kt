package com.amiranisayev.sinavtakvimiprojesi

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlin.collections.emptyList
import android.app.Application
import androidx.lifecycle.AndroidViewModel

class SinavViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = ApiService()
    private val context = application.applicationContext


    // UI'ın izleyeceği liste
    var sinavListesi = mutableStateOf<List<SinavDetay>>(emptyList())
    var yukleniyorMu = mutableStateOf(false)
    var hataMesaji = mutableStateOf<String?>(null)

    var oturumListesi = mutableStateOf<List<Oturum>>(emptyList())
    var bolumListesi = mutableStateOf<List<Bolum>>(emptyList())
    var derslikListesi = mutableStateOf<List<Derslik>>(emptyList())
    var gozetmenListesi = mutableStateOf<List<Personel>>(emptyList())
    var gozetmenAtamaMesaji = mutableStateOf<String?>(null)
    var sinavlarListesi = mutableStateOf<List<SinavDetay>>(emptyList())
    var salonAtamaMesaji = mutableStateOf<String?>(null)
    var dersListesi = mutableStateOf<List<Ders>>(emptyList())
    var yedekMesaji = mutableStateOf<String?>(null)
    var salonDolulukListesi = mutableStateOf<List<SalonDoluluk>>(emptyList())
    var istatistikYukleniyor = mutableStateOf(false)





    init {
        //verileriGetir()
        oturumlariGetir()
        bolumleriGetir()
        derslikleriGetir()
    }

    //private var appContext: android.content.Context? = null

    //fun setContext(context: android.content.Context) {
      //  appContext = context
    //}
    fun verileriGetir() {
        viewModelScope.launch {
            yukleniyorMu.value = true
            try {
                // Ktor Backend'den veriyi çekiyoruz
                sinavListesi.value   = apiService.getSinavTakvimi()
                hataMesaji.value = null
                BildirimYoneticisi.kanalOlustur(context)
                BildirimYoneticisi.yaklasanSinavlariKontrolEt(context, sinavListesi.value)
            } catch (e: Exception) {
                hataMesaji.value = "Bağlantı Hatası: ${e.localizedMessage}"
                e.printStackTrace()
            } finally {
                yukleniyorMu.value = false
            }
        }
    }

    var eklemeMesaji = mutableStateOf<String?>(null)

    fun sinavEkle(yeniSinav: SinavTakvimi) {
        viewModelScope.launch {
            try {
                val sonuc = apiService.sinavEkle(yeniSinav)
                if (sonuc != "HATA") {
                    eklemeMesaji.value = sonuc
                    verileriGetir()
                    // 3 saniye sonra mesajı temizle
                    kotlinx.coroutines.delay(3000)
                    eklemeMesaji.value = null
                } else {
                    hataMesaji.value = "Sınav eklenirken bir hata oluştu."
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sinavSil(id: Int) {
        viewModelScope.launch {
            try {
                val basariliMi = apiService.sinavSil(id)
                if (basariliMi) {
                    // Eğer backend "silindi" dediyse, listeyi hemen yeniliyoruz
                    verileriGetir()
                }
                else{
                    hataMesaji.value="Sınav silinirken bir hata oluştu."
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun oturumlariGetir(tarih: String = "") {
        viewModelScope.launch {
            try {
                oturumListesi.value = if (tarih.isEmpty()) {
                    apiService.getOturumlar("")
                } else {
                    apiService.getOturumlar(tarih)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun bolumleriGetir() {
        viewModelScope.launch {
            try {
                bolumListesi.value = apiService.getBolumler()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun derslikleriGetir() {
        viewModelScope.launch {
            try {
                derslikListesi.value = apiService.getDerslikler()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sinavlariGetir() {
        viewModelScope.launch {
            try {
                sinavlarListesi.value = apiService.getSinavlar()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun gozetmenAta(sinavId: Int) {
        viewModelScope.launch {
            try {
                val basariliMi = apiService.gozetmenAta(sinavId)
                if (basariliMi) {
                    gozetmenAtamaMesaji.value = "Gözetmen ataması başarıyla tamamlandı!"
                    gozetmenleriGetir(sinavId)
                } else {
                    gozetmenAtamaMesaji.value = "Gözetmen ataması başarısız!"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun gozetmenleriGetir(sinavId: Int) {
        viewModelScope.launch {
            try {
                gozetmenListesi.value = apiService.getGozetmenler(sinavId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun salonAta(sinavId: Int) {
        viewModelScope.launch {
            try {
                val basariliMi = apiService.salonAta(sinavId)
                if (basariliMi) {
                    salonAtamaMesaji.value = "Salon ataması başarıyla tamamlandı!"
                    verileriGetir()
                } else {
                    salonAtamaMesaji.value = "Salon ataması başarısız!"
                }
            } catch (e: Exception) {
                salonAtamaMesaji.value = "Hata: ${e.localizedMessage}"
                e.printStackTrace()
            }
        }
    }

    fun dersleriGetir(bolumId: Int, yariyil: Int? = null) {
        viewModelScope.launch {
            try {
                dersListesi.value = apiService.getDersler(bolumId, yariyil)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun yedekAl() {
        viewModelScope.launch {
            try {
                val basariliMi = apiService.yedekAl()
                if (basariliMi) {
                    yedekMesaji.value = "Yedekleme başarıyla tamamlandı!"
                } else {
                    yedekMesaji.value = "Yedekleme başarısız!"
                }
                // 3 saniye sonra mesajı temizle
                kotlinx.coroutines.delay(3000)
                yedekMesaji.value = null
            } catch (e: Exception) {
                yedekMesaji.value = "Hata: ${e.localizedMessage}"
                kotlinx.coroutines.delay(3000)
                yedekMesaji.value = null
                e.printStackTrace()
            }
        }
    }

    fun salonDolulugununGetir() {
        viewModelScope.launch {
            istatistikYukleniyor.value = true
            try {
                salonDolulukListesi.value = apiService.getSalonDoluluk()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                istatistikYukleniyor.value = false
            }
        }
    }
}