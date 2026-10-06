package com.sever.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.agent.reActStrategy
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import com.sever.config.AgentConfig
import com.sever.observability.ToolCallLogger

class AgentFactory(private val config: AgentConfig) {
    fun create(systemPrompt: String, toolRegistry: ToolRegistry): AIAgent<String, String> {
        val toolLogger = ToolCallLogger()
        return AIAgent(
            promptExecutor = MultiLLMPromptExecutor(AnthropicLLMClient(config.apiKey)),
            llmModel = config.model,
            strategy = reActStrategy(),
            systemPrompt = systemPrompt,
            toolRegistry = toolRegistry,
        ) {
            handleEvents {
                onToolCallStarting(toolLogger::starting)
                onToolCallCompleted(toolLogger::completed)
                onToolCallFailed(toolLogger::failed)
            }
        }
    }
}
