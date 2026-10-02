package com.sever.mcp

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.mcp.McpToolRegistryProvider
import ai.koog.agents.mcp.fromProcess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class ProcessMcpServer(
    override val name: String,
    private val command: List<String>,
) : McpServer {
    private var process: Process? = null

    override suspend fun connect(): ToolRegistry {
        val started = withContext(Dispatchers.IO) {
            ProcessBuilder(command)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
        }
        process = started
        return McpToolRegistryProvider.fromProcess(started)
    }

    override fun close() {
        process?.destroy()
    }
}
