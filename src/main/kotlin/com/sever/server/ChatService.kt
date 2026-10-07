package com.sever.server

import ai.koog.agents.core.tools.ToolRegistry
import com.sever.agent.AgentFactory
import com.sever.agent.SystemPromptFactory
import com.sever.config.AgentConfig
import com.sever.config.AgentConfigLoader
import com.sever.history.SessionHistory
import com.sever.mcp.McpServers
import com.sever.mcp.playwrightMcpServer
import com.sever.tools.CommandTools
import com.sever.skills.SkillTools
import kotlinx.serialization.json.JsonElement
import java.util.concurrent.ConcurrentHashMap

/** Owns the shared tool registry and the live chat sessions. */
class ChatService private constructor(
    private val mcpServers: McpServers,
    private val toolRegistry: ToolRegistry,
    private val systemPrompt: String,
    private val agentFactory: AgentFactory,
) {
    private val sessions = ConcurrentHashMap<String, ChatSession>()

    suspend fun send(sessionId: String, message: String): String =
        sessions.computeIfAbsent(sessionId) {
            ChatSession(it, toolRegistry, systemPrompt, agentFactory)
        }.send(message)

    fun history(sessionId: String): List<JsonElement> = SessionHistory(sessionId).read()

    fun close() = mcpServers.closeAll()

    companion object {
        suspend fun create(config: AgentConfig = AgentConfigLoader.load()): ChatService {
            val mcpServers = McpServers(listOf(playwrightMcpServer()))
            val toolRegistry = mcpServers.connectAll() + CommandTools.registry() + SkillTools.registry(config.skillsDir)
            val systemPrompt = SystemPromptFactory.build(config)
            return ChatService(mcpServers, toolRegistry, systemPrompt, AgentFactory(config))
        }
    }
}
