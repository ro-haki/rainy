package com.sever.mcp

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.mcp.McpToolRegistryProvider
import ai.koog.agents.mcp.fromProcess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

open class ProcessMcpServer(
    override val name: String,
    private val command: List<String>,
) : McpServer {
    private val log = LoggerFactory.getLogger(javaClass)
    private var process: Process? = null

    override suspend fun connect(): ToolRegistry {
        log.info("Connecting MCP server '{}'", name)
        val started = withContext(Dispatchers.IO) {
            ProcessBuilder(command)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
        }
        process = started
        return McpToolRegistryProvider.fromProcess(started).also {
            log.info("MCP server '{}' exposed {} tools", name, it.tools.size)
        }
    }

    override fun close() {
        process?.destroy()
    }
}
