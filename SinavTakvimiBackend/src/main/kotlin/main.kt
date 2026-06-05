package com.example

import com.example.config.DatabaseFactory
import com.example.plugins.configureResources
import com.example.plugins.configureSerialization
import com.example.routes.configureRouting
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module(){
    DatabaseFactory.init() // veritabanı bağlantısını başlatmak için kullanılır
    configureSerialization()
    configureResources()
    configureRouting()
}