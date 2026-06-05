package com.example.models

import org.jetbrains.exposed.sql.Table

object KullaniciTable : Table("Kullanicilar") {
    val id = integer("KullaniciID").autoIncrement()
    val kullaniciAdi = varchar("KullaniciAdi", 50)
    val sifre = varchar("Sifre", 100)
    val eposta = varchar("Eposta", 100).nullable()
    val rol = varchar("Rol", 20)

    override val primaryKey = PrimaryKey(id)
}