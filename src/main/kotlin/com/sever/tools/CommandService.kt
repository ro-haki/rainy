package com.sever.tools

class CommandService(
    private val toolName: String,
    private val command: Command,
    private val parser: CommandRequestParser,
) {
    fun run(target: String, arguments: String, mode: String?, timeoutSeconds: Long?): String {
        val request = parser.parse(target, arguments, mode, timeoutSeconds)
        return try {
            command.run(request.target, request.arguments, request.modeArguments, request.timeoutSeconds).formatted()
        } catch (e: Exception) {
            "Failed to run $toolName: ${e.message}."
        }
    }
}
