package com.sever

import com.sever.agent.Agent
import com.sever.server.startChatServer
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("com.sever.Main")

fun main(args: Array<String>) {
    val prompt = args.joinToString(" ").trim().ifEmpty { readStdinIfPresent() }
    if (prompt.isNotEmpty()) {
        log.info("Agent response:\n{}", Agent().run(prompt))
        return
    }

    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    startChatServer(port)
}

private fun readStdinIfPresent(): String =
    if (System.`in`.available() > 0) System.`in`.bufferedReader().readText().trim() else ""
