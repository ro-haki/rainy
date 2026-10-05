package com.sever

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import com.sever.config.AgentConfigLoader
import com.sever.mcp.McpServers
import com.sever.mcp.playwrightMcpServer
import com.sever.skills.AgentSkills
import com.sever.tools.CommandTools
import kotlinx.coroutines.runBlocking

fun main() {
    val config = AgentConfigLoader.load()
    val mcpServers = McpServers(listOf(playwrightMcpServer()))

    try {
        runBlocking {
            val toolRegistry = mcpServers.connectAll() + CommandTools.registry()
            val systemPrompt = buildString {
                append(config.systemPrompt)
                append("\n\n# Available skills\n")
                append(AgentSkills.promptSection(config.skillsDir))
            }

            val agent = AIAgent(
                promptExecutor = MultiLLMPromptExecutor(AnthropicLLMClient(config.apiKey)),
                llmModel = config.model,
                systemPrompt = systemPrompt,
                toolRegistry = toolRegistry,
            )
            val response = agent.run(config.userPrompt)
            println(response)
        }
    } finally {
        mcpServers.closeAll()
    }
}
