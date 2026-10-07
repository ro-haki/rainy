package com.sever.agent

import com.sever.config.AgentConfig
import com.sever.config.AgentConfigLoader
import com.sever.history.SessionHistory
import com.sever.mcp.McpServers
import com.sever.mcp.playwrightMcpServer
import com.sever.skills.SkillTools
import com.sever.tools.CommandTools
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class Agent(
    private val config: AgentConfig = AgentConfigLoader.load(),
    private val mcpServers: McpServers = McpServers(listOf(playwrightMcpServer())),
    private val agentFactory: AgentFactory = AgentFactory(config),
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun run(userPrompt: String): String = runBlocking {
        log.info("Starting agent with model {}", config.model.id)
        try {
            val toolRegistry = mcpServers.connectAll() + CommandTools.registry() + SkillTools.registry(config.skillsDir)
            log.info("Tools available: {}", toolRegistry.tools.joinToString { it.name })

            val history = SessionHistory("cli-${System.currentTimeMillis()}")
            history.userPrompt(userPrompt)
            val agent = agentFactory.create(SystemPromptFactory.build(config), toolRegistry, history)

            log.info("Running task: {}", userPrompt)
            agent.run(userPrompt).also {
                history.agentResponse(it)
                log.info("Agent run completed")
            }
        } catch (e: Throwable) {
            log.error(LlmErrors.userMessage(e), e)
            throw e
        } finally {
            mcpServers.closeAll()
        }
    }
}
