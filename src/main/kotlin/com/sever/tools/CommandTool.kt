package com.sever.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.serialization.typeToken
import com.sever.tools.config.ToolConfig
import com.sever.tools.config.ToolConfigLoader

class CommandTool(
    name: String,
    description: String,
    private val service: CommandService,
) : SimpleTool<CommandArgs>(typeToken<CommandArgs>(), name, description) {
    override suspend fun execute(args: CommandArgs): String =
        service.run(args.target, args.arguments, args.mode, args.timeoutSeconds)
}

object CommandToolFactory {
    fun create(resourceName: String, runner: CommandRunner): CommandTool {
        val config = ToolConfigLoader.load(resourceName)
        val command = CliCommand(
            name = config.name,
            executable = config.executable,
            runner = runner,
            defaultArguments = config.defaultArguments,
            targetFlag = config.targetFlag,
        )
        val service = CommandService(
            toolName = config.name,
            command = command,
            parser = CommandRequestParser(
                modes = config.modes,
                defaultMode = config.defaultMode,
                defaultTimeoutSeconds = config.defaultTimeoutSeconds,
                maxTimeoutSeconds = config.maxTimeoutSeconds,
            ),
        )
        return CommandTool(config.toolName, describe(config), service)
    }

    private fun describe(config: ToolConfig): String = buildString {
        append(config.description)
        if (config.modes.isNotEmpty()) {
            append("\n\nRate modes: ${config.modes.keys.joinToString(", ")} (default: ${config.defaultMode}).")
        }
        if (config.argumentExamples.isNotEmpty()) {
            append("\n\nExample arguments:")
            config.argumentExamples.forEach { example ->
                append("\n- '${example.arguments}' — ${example.description}")
            }
        }
    }
}
