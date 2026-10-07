package com.sever.history

import ai.koog.agents.core.feature.handler.tool.ToolCallCompletedContext
import ai.koog.agents.core.feature.handler.tool.ToolCallFailedContext
import ai.koog.agents.core.feature.handler.tool.ToolCallStartingContext
import ai.koog.serialization.kotlinx.toKotlinxJsonElement
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.time.Instant

/** Append-only JSONL of everything that happens in one chat session: prompts, responses, tool calls. */
class SessionHistory(private val sessionId: String, logDir: String = "logs") {
    private val file = File(logDir).apply { mkdirs() }.resolve("session-$sessionId.jsonl")

    fun userPrompt(text: String) = record("user_prompt") { put("text", text) }

    fun agentResponse(text: String) = record("agent_response") { put("text", text) }

    fun error(text: String) = record("error") { put("text", text) }

    fun toolCall(e: ToolCallStartingContext) = record("tool_call") {
        put("tool", e.toolName)
        put("toolCallId", e.toolCallId)
        put("args", e.toolArgs.toKotlinxJsonElement())
    }

    fun toolResult(e: ToolCallCompletedContext) = record("tool_result") {
        put("tool", e.toolName)
        put("toolCallId", e.toolCallId)
        put("result", e.toolResult?.toKotlinxJsonElement() ?: JsonNull)
    }

    fun toolError(e: ToolCallFailedContext) = record("tool_error") {
        put("tool", e.toolName)
        put("toolCallId", e.toolCallId)
        put("error", e.message)
    }

    fun read(): List<JsonElement> =
        if (file.isFile) file.readLines().filter { it.isNotBlank() }.map(Json::parseToJsonElement) else emptyList()

    @Synchronized
    private fun record(type: String, body: JsonObjectBuilder.() -> Unit) {
        val line = buildJsonObject {
            put("ts", Instant.now().toString())
            put("sessionId", sessionId)
            put("type", type)
            body()
        }.toString()
        runCatching { file.appendText(line + "\n") }
    }
}
