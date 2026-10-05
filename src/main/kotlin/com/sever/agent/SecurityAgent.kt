package com.sever.agent

import com.sever.config.AgentConfig
import com.sever.mcp.McpServers
import com.sever.mcp.playwrightMcpServer
import com.sever.tools.CommandTools
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class SecurityAgent(
    private val config: AgentConfig,
    private val mcpServers: McpServers = McpServers(listOf(playwrightMcpServer())),
    private val agentFactory: AgentFactory = AgentFactory(config),
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun run(userPrompt: String): String = runBlocking {
        log.info("Starting agent with model {}", config.model.id)
        try {
            val toolRegistry = mcpServers.connectAll() + CommandTools.registry()
            log.info("Tools available: {}", toolRegistry.tools.joinToString { it.name })

            val agent = agentFactory.create(SystemPromptFactory.build(config), toolRegistry)

            log.info("Running task: {}", userPrompt)
            agent.run(userPrompt).also { log.info("Agent run completed") }
        } catch (e: Throwable) {
            report(e)
            throw e
        } finally {
            mcpServers.closeAll()
        }
    }

    private fun report(error: Throwable) {
        if (isAuthError(error)) {
            log.error("Anthropic API authentication failed — check ANTHROPIC_API_KEY", error)
        } else {
            log.error("Agent run failed", error)
        }
    }

    private fun isAuthError(error: Throwable): Boolean =
        generateSequence(error) { it.cause }.any { cause ->
            val message = cause.message?.lowercase().orEmpty()
            listOf("401", "unauthorized", "authentication", "x-api-key").any { it in message }
        }
}
