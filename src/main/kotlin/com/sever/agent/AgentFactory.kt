package com.sever.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import com.sever.config.AgentConfig
import com.sever.history.SessionHistory

class AgentFactory(private val config: AgentConfig) {
    fun create(systemPrompt: String, toolRegistry: ToolRegistry, history: SessionHistory): AIAgent<String, String> =
        AIAgent(
            promptExecutor = MultiLLMPromptExecutor(AnthropicLLMClient(config.apiKey)),
            llmModel = config.model,
            strategy = prefillSafeReActStrategy(),
            systemPrompt = systemPrompt,
            toolRegistry = toolRegistry,
        ) {
            handleEvents {
                onToolCallStarting(history::toolCall)
                onToolCallCompleted(history::toolResult)
                onToolCallFailed(history::toolError)
            }
        }
}
