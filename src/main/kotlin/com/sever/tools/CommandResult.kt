package com.sever.tools

data class CommandResult(
    val command: List<String>,
    val exitCode: Int?,
    val output: String,
    val timedOut: Boolean,
) {
    val failed: Boolean get() = timedOut || (exitCode != null && exitCode != 0)

    fun formatted(): String = buildString {
        append("$ ${command.joinToString(" ")}\n")
        append(if (timedOut) "(timed out)\n\n" else "(exit code $exitCode)\n\n")
        append(output.ifBlank { "(no output)" })
    }
}
