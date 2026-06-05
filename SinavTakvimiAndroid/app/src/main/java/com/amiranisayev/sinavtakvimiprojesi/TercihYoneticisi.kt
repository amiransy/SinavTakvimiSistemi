package com.amiranisayev.sinavtakvimiprojesi

import android.content.Context
import android.content.SharedPreferences

object TercihYoneticisi {
    private const val TERCIH_ADI = "sinav_takvimi_prefs"
    private const val KULLANICI_ADI_KEY = "kullanici_adi"
    private const val KULLANICI_ROL_KEY = "kullanici_rol"
    private const val KULLANICI_ID_KEY = "kullanici_id"
    private const val GIRIS_YAPILDI_KEY = "giris_yapildi"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(TERCIH_ADI, Context.MODE_PRIVATE)
    }

    fun kullaniciyiKaydet(context: Context, kullanici: Kullanici) {
        getPrefs(context).edit().apply {
            putString(KULLANICI_ADI_KEY, kullanici.kullaniciAdi)
            putString(KULLANICI_ROL_KEY, kullanici.rol)
            putInt(KULLANICI_ID_KEY, kullanici.id)
            putBoolean(GIRIS_YAPILDI_KEY, true)
            apply()
        }
    }

    fun kullaniciyiGetir(context: Context): Kullanici? {
        val prefs = getPrefs(context)
        val girisYapildi = prefs.getBoolean(GIRIS_YAPILDI_KEY, false)
        if (!girisYapildi) return null

        return Kullanici(
            id = prefs.getInt(KULLANICI_ID_KEY, 0),
            kullaniciAdi = prefs.getString(KULLANICI_ADI_KEY, "") ?: "",
            eposta = null,
            rol = prefs.getString(KULLANICI_ROL_KEY, "") ?: ""
        )
    }

    fun cikisYap(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
    fun takvimEtkinlikIdKaydet(context: Context, sinavId: Int, etkinlikId: Long) {
        getPrefs(context).edit().putLong("takvim_$sinavId", etkinlikId).apply()
    }

    fun takvimEtkinlikIdGetir(context: Context, sinavId: Int): Long {
        return getPrefs(context).getLong("takvim_$sinavId", -1L)
    }

    fun takvimEtkinlikIdSil(context: Context, sinavId: Int) {
        getPrefs(context).edit().remove("takvim_$sinavId").apply()
    }
}