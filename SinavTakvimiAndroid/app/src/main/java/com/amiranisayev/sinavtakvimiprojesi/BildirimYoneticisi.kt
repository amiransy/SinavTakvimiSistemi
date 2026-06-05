package com.amiranisayev.sinavtakvimiprojesi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object BildirimYoneticisi {

    private const val KANAL_ID = "sinav_bildirimleri"
    private const val KANAL_ADI = "Sınav Bildirimleri"

    fun kanalOlustur(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val kanal = NotificationChannel(
                KANAL_ID,
                KANAL_ADI,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Yaklaşan sınav bildirimleri"
            }
            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.createNotificationChannel(kanal)
        }
    }

    fun bildirimGonder(
        context: Context,
        baslik: String,
        mesaj: String,
        bildirimId: Int = System.currentTimeMillis().toInt()
    ) {
        val bildirim = NotificationCompat.Builder(context, KANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(baslik)
            .setContentText(mesaj)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setGroup("sinav_grup_$bildirimId") // her bildirim ayrı grup
            .build()

        try {
            NotificationManagerCompat.from(context).notify(bildirimId, bildirim)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun yaklasanSinavlariKontrolEt(context: Context, sinavlar: List<SinavDetay>) {
        val bugun = java.time.LocalDate.now()
        android.util.Log.d("BILDIRIM", "Kontrol başladı. Toplam sınav: ${sinavlar.size}")


        sinavlar.forEach { sinav ->
            val sinavTarihi = try {
                java.time.LocalDate.parse(sinav.Tarih ?: return@forEach)
            } catch (e: Exception) {
                return@forEach
            }

            val kalanGun = bugun.until(sinavTarihi, java.time.temporal.ChronoUnit.DAYS)
            android.util.Log.d("BILDIRIM", "${sinav.DersAdi} - Kalan gün: $kalanGun")

            // Sadece gelecekteki sınavlar için bildirim gönder
            if (kalanGun < 0) return@forEach
            when (kalanGun) {
                1L -> {
                    android.util.Log.d("BILDIRIM", "Yarın sınavı var: ${sinav.DersAdi}")
                    bildirimGonder(
                    context,
                    "Yarın Sınav Var! 📚",
                    "${sinav.DersAdi} sınavı yarın ${sinav.BaslangicSaat} - ${sinav.BitisSaat} saatlerinde.",
                    sinav.SinavID ?: 0
                )}
                3L -> {
                    android.util.Log.d("BILDIRIM", "Yarın sınavı var: ${sinav.DersAdi}")
                    bildirimGonder(
                    context,
                    "3 Gün Sonra Sınav! ⚠️",
                    "${sinav.DersAdi} sınavına 3 gün kaldı. Salon: ${sinav.SalonAdi}",
                    (sinav.SinavID ?: 0) + 1000
                )}
                7L -> {
                    android.util.Log.d("BILDIRIM", "Yarın sınavı var: ${sinav.DersAdi}")
                    bildirimGonder(
                    context,
                    "1 Hafta Sonra Sınav 📅",
                    "${sinav.DersAdi} sınavına 1 hafta kaldı.",
                    (sinav.SinavID ?: 0) + 2000
                )}
            }
        }
    }
}