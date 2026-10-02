package com.sever.mcp

import ai.koog.agents.core.tools.ToolRegistry

class McpServers(private val servers: List<McpServer>) {
    suspend fun connectAll(): ToolRegistry =
        servers.map { it.connect() }.fold(ToolRegistry { }) { acc, registry -> acc + registry }

    fun closeAll() = servers.forEach { runCatching { it.close() } }
}
