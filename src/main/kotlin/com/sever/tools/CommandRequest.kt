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
    fun parse(args: CommandArgs): CommandRequest {
        val selectedMode = args.mode?.lowercase()?.takeIf { it in modes } ?: defaultMode
        val resolvedTimeout = (args.timeoutSeconds ?: defaultTimeoutSeconds).coerceIn(1, maxTimeoutSeconds)
        return CommandRequest(
            target = args.target.trim(),
            arguments = args.arguments,
            modeArguments = modes[selectedMode].orEmpty(),
            timeoutSeconds = resolvedTimeout,
        )
    }
}
