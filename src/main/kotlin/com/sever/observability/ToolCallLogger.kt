package com.sever.observability

import ai.koog.agents.core.feature.handler.tool.ToolCallCompletedContext
import ai.koog.agents.core.feature.handler.tool.ToolCallEventContext
import ai.koog.agents.core.feature.handler.tool.ToolCallFailedContext
import ai.koog.agents.core.feature.handler.tool.ToolCallStartingContext
import ai.koog.serialization.kotlinx.toKotlinxJsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Logs every tool the LLM invokes as one JSON object per line, to stdout and a per-run file. */
class ToolCallLogger {
    private val file: File = run {
        val dir = File("logs").also { it.mkdirs() }
        val stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").withZone(KYIV).format(Instant.now())
        File(dir, "tool-calls-$stamp.jsonl")
    }

    fun starting(e: ToolCallStartingContext) =
        write("tool_call", e) { put("args", e.toolArgs.toKotlinxJsonElement()) }

    fun completed(e: ToolCallCompletedContext) =
        write("tool_result", e) {
            put("args", e.toolArgs.toKotlinxJsonElement())
            put("result", e.toolResult?.toKotlinxJsonElement() ?: JsonNull)
        }

    fun failed(e: ToolCallFailedContext) =
        write("tool_error", e) {
            put("args", e.toolArgs.toKotlinxJsonElement())
            put("error", e.message)
        }

    private fun write(event: String, context: ToolCallEventContext, extra: JsonObjectBuilder.() -> Unit) {
        val line = buildJsonObject {
            put("ts", Instant.now().toString())
            put("event", event)
            put("tool", context.toolName)
            put("toolCallId", context.toolCallId)
            extra()
        }.toString()
        println(line)
        runCatching { file.appendText(line + "\n") }
    }

    private companion object {
        val KYIV: ZoneId = ZoneId.of("Europe/Kyiv")
    }
}
