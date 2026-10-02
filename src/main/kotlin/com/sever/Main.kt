package com.sever

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import kotlinx.coroutines.runBlocking

const val API_KEY = "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"
const val SYSTEM_PROMPT = """
    
"""

fun main() {
    println("Starting...")

    val agent = AIAgent(
        promptExecutor = MultiLLMPromptExecutor(AnthropicLLMClient(API_KEY)),
        llmModel = AnthropicModels.Haiku_4_5,
        systemPrompt = SYSTEM_PROMPT
    )

    runBlocking {
        val response = agent.run("")
        println(response)
    }
}
