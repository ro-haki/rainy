package com.sever.mcp

import ai.koog.agents.core.tools.ToolRegistry

interface McpServer {
    val name: String
    suspend fun connect(): ToolRegistry
    fun close()
}
