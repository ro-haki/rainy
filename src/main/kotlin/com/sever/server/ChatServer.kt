package com.sever.server

import io.ktor.http.ContentType
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.slf4j.LoggerFactory
import java.util.UUID

private val log = LoggerFactory.getLogger("com.sever.server.ChatServer")

fun startChatServer(port: Int): Unit = runBlocking {
    val service = ChatService.create()
    Runtime.getRuntime().addShutdownHook(Thread { service.close() })

    val indexHtml = object {}.javaClass.getResource("/web/index.html")?.readText()
        ?: error("web/index.html not found on classpath")

    log.info("Chat server listening on http://0.0.0.0:{}", port)
    embeddedServer(CIO, port = port, host = "0.0.0.0") {
        routing {
            get("/") { call.respondText(indexHtml, ContentType.Text.Html) }

            post("/api/chat") {
                val body = Json.parseToJsonElement(call.receiveText()).jsonObject
                val sessionId = body["sessionId"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                    ?: UUID.randomUUID().toString()
                val message = body["message"]?.jsonPrimitive?.content.orEmpty()
                val response = runCatching { service.send(sessionId, message) }
                    .getOrElse { "Request failed: ${it.message}" }
                call.respondText(
                    buildJsonObject {
                        put("sessionId", sessionId)
                        put("response", response)
                    }.toString(),
                    ContentType.Application.Json,
                )
            }

            get("/api/history/{id}") {
                val id = call.parameters["id"].orEmpty()
                call.respondText(JsonArray(service.history(id)).toString(), ContentType.Application.Json)
            }
        }
    }.start(wait = true)
}
