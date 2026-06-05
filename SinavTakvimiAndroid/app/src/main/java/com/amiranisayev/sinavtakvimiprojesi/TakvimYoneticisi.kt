package com.amiranisayev.sinavtakvimiprojesi

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object TakvimYoneticisi {

    fun sinaviTakvimaEkle(context: Context, sinav: SinavDetay): Long? {
        return try {
            val tarih = LocalDate.parse(sinav.Tarih ?: return null)
            val baslangicSaat = try {
                LocalTime.parse(sinav.BaslangicSaat ?: "09:00")
            } catch (e: Exception) { LocalTime.of(9, 0) }
            val bitisSaat = try {
                LocalTime.parse(sinav.BitisSaat ?: "10:00")
            } catch (e: Exception) { LocalTime.of(10, 0) }

            val zone = ZoneId.systemDefault()
            val baslangicMs = tarih.atTime(baslangicSaat).atZone(zone).toInstant().toEpochMilli()
            val bitisMs = tarih.atTime(bitisSaat).atZone(zone).toInstant().toEpochMilli()

            val takvimId = takvimIdGetir(context)
            if (takvimId != null) {
                val values = ContentValues().apply {
                    put(CalendarContract.Events.CALENDAR_ID, takvimId)
                    put(CalendarContract.Events.TITLE, "${sinav.DersKodu} - ${sinav.DersAdi} Sınavı")
                    put(CalendarContract.Events.DESCRIPTION,
                        "Bölüm: ${sinav.BolumAdi}\nSalon: ${sinav.SalonAdi}\nGözetmenler: ${sinav.Gozetmenler.joinToString(", ")}")
                    put(CalendarContract.Events.DTSTART, baslangicMs)
                    put(CalendarContract.Events.DTEND, bitisMs)
                    put(CalendarContract.Events.EVENT_LOCATION, "Salon: ${sinav.SalonAdi}")
                    put(CalendarContract.Events.EVENT_TIMEZONE, zone.id)
                }
                val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                if (uri != null) {
                    return uri.lastPathSegment?.toLongOrNull()
                }
            }

            // Takvim bulunamazsa Intent ile aç
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "${sinav.DersKodu} - ${sinav.DersAdi} Sınavı")
                putExtra(CalendarContract.Events.DESCRIPTION,
                    "Bölüm: ${sinav.BolumAdi}\nSalon: ${sinav.SalonAdi}")
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, baslangicMs)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, bitisMs)
                putExtra(CalendarContract.Events.EVENT_LOCATION, "Salon: ${sinav.SalonAdi}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun sinaviTakvimdenSil(context: Context, takvimEtkinlikId: Long) {
        try {
            val uri = android.net.Uri.withAppendedPath(
                CalendarContract.Events.CONTENT_URI,
                takvimEtkinlikId.toString()
            )
            context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun takvimIdGetir(context: Context): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.IS_PRIMARY
        )
        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.ACCOUNT_TYPE} = ? AND ${CalendarContract.Calendars.IS_PRIMARY} = ?",
            arrayOf("com.google", "1"),
            null
        )
        cursor?.use {
            if (it.moveToFirst()) return it.getLong(0)
        }
        val fallback = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection, null, null, null
        )
        fallback?.use {
            if (it.moveToFirst()) return it.getLong(0)
        }
        return null
    }
}