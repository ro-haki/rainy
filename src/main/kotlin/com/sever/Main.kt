package com.sever

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import com.sever.config.AgentConfigLoader
import com.sever.mcp.McpServers
import com.sever.mcp.playwrightMcpServer
import com.sever.tools.ScanTools
import kotlinx.coroutines.runBlocking

fun main() {
    val config = AgentConfigLoader.load()
    val mcpServers = McpServers(listOf(playwrightMcpServer()))

    try {
        runBlocking {
            val toolRegistry = mcpServers.connectAll() + ScanTools.registry()
            println("Connected. Tools: ${toolRegistry.tools.joinToString { it.name }}")

            val agent = AIAgent(
                promptExecutor = MultiLLMPromptExecutor(AnthropicLLMClient(config.apiKey)),
                llmModel = config.model,
                systemPrompt = config.systemPrompt,
                toolRegistry = toolRegistry,
            )

                val response = agent.run("Run masscan scanme.nmap.org over all ports")
            println(response)
        }
    } finally {
        mcpServers.closeAll()
    }
}
