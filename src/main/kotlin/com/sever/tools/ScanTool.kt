package com.sever.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.serialization.typeToken
import com.sever.tools.config.ScannerConfig
import com.sever.tools.config.ScannerConfigLoader

class ScanTool(
    name: String,
    description: String,
    private val service: ScanService,
) : SimpleTool<ScanArgs>(typeToken<ScanArgs>(), name, description) {
    override suspend fun execute(args: ScanArgs): String =
        service.run(args.target, args.options, args.timeoutSeconds)
}

object ScanToolFactory {
    fun create(resourceName: String, runner: CommandRunner): ScanTool {
        val config = ScannerConfigLoader.load(resourceName)
        val scanner = CommandLineScanner(
            name = config.name,
            executable = config.executable,
            runner = runner,
            defaultArguments = config.defaultArguments,
            targetFlag = config.targetFlag,
        )
        val service = ScanService(
            scannerName = config.name,
            scanner = scanner,
            parser = ScanRequestParser(config.defaultTimeoutSeconds, config.maxTimeoutSeconds),
        )
        return ScanTool(config.toolName, describe(config), service)
    }

    private fun describe(config: ScannerConfig): String = buildString {
        append(config.description)
        if (config.optionExamples.isNotEmpty()) {
            append(" Example options: ")
            append(config.optionExamples.joinToString("; ") { "'$it'" })
            append(".")
        }
    }
}
