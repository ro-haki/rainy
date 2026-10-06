package com.sever.tools

class CommandService(
    private val toolName: String,
    private val command: Command,
    private val parser: CommandRequestParser,
) {
    fun run(args: CommandArgs): String =
        try {
            command.run(parser.parse(args)).formatted()
        } catch (e: Exception) {
            "Failed to run $toolName: ${e.message}."
        }
}
