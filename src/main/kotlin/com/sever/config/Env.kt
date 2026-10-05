package com.sever.config

import java.io.File

object Env {
    private val dotenv: Map<String, String> by lazy { readDotenv(File(".env")) }

    fun get(name: String): String? = System.getenv(name) ?: dotenv[name]

    fun require(name: String): String =
        get(name) ?: error("Missing required environment variable '$name' (set it in the environment or .env)")

    private fun readDotenv(file: File): Map<String, String> {
        if (!file.isFile) return emptyMap()
        return file.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
            .associate { line ->
                val (key, value) = line.split("=", limit = 2)
                key.trim() to value.trim().trim('"')
            }
    }
}
