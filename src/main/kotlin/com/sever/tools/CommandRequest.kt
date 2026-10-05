package com.sever.tools

data class CommandRequest(
    val target: String,
    val arguments: String,
    val modeArguments: List<String>,
    val timeoutSeconds: Long,
)

class CommandRequestParser(
    private val modes: Map<String, List<String>>,
    private val defaultMode: String,
    private val defaultTimeoutSeconds: Long,
    private val maxTimeoutSeconds: Long,
) {
    fun parse(target: String, arguments: String, mode: String?, timeoutSeconds: Long?): CommandRequest {
        val selectedMode = mode?.lowercase()?.takeIf { it in modes } ?: defaultMode
        val resolvedTimeout = (timeoutSeconds ?: defaultTimeoutSeconds).coerceIn(1, maxTimeoutSeconds)
        return CommandRequest(
            target = target.trim(),
            arguments = arguments,
            modeArguments = modes[selectedMode].orEmpty(),
            timeoutSeconds = resolvedTimeout,
        )
    }
}
