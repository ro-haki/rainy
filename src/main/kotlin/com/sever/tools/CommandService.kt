package com.sever.tools

class CommandService(
    private val toolName: String,
    private val command: Command,
    private val parser: CommandRequestParser,
) {
    fun run(target: String, arguments: String, timeoutSeconds: Long?): String {
        val request = parser.parse(target, arguments, timeoutSeconds)
        return try {
            command.run(request.target, request.arguments, request.timeoutSeconds).formatted()
        } catch (e: Exception) {
            "Failed to run $toolName: ${e.message}."
        }
    }
}
