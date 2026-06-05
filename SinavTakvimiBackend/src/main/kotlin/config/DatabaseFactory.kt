package com.example.config

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    lateinit var adminDb: Database
    lateinit var viewerDb: Database

    fun init() {
        try {
            val driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            val jdbcUrl = "jdbc:sqlserver://localhost:1433;databaseName=DersProjesiDB;encrypt=true;trustServerCertificate=true;sendStringParametersAsUnicode=true;unicode=true;"
            adminDb = Database.connect(
                url = jdbcUrl,
                driver = driverClassName,
                user = "App_Admin_Login",
                password = "Admin_Sifre_123"
            )

            viewerDb = Database.connect(
                url = jdbcUrl,
                driver = driverClassName,
                user = "App_Viewer_Login",
                password = "Viewer_Sifre_123"
            )

            println("--- Veritabanı bağlantıları başarıyla kuruldu! ---")
        } catch (e: Exception) {
            println("--- BAĞLANTI HATASI: ${e.message} ---")
            e.printStackTrace()
        }
    }
}