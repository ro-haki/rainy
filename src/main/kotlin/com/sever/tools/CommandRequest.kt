package com.sever.tools

data class CommandRequest(
    val target: String,
    val arguments: String,
    val timeoutSeconds: Long,
)

class CommandRequestParser(
    private val defaultTimeoutSeconds: Long,
    private val maxTimeoutSeconds: Long,
) {
    fun parse(target: String, arguments: String, timeoutSeconds: Long?): CommandRequest {
        val resolved = (timeoutSeconds ?: defaultTimeoutSeconds).coerceIn(1, maxTimeoutSeconds)
        return CommandRequest(target.trim(), arguments, resolved)
    }
}
