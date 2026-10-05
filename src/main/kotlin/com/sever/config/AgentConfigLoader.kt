package com.sever.config

import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.llm.LLModel
import org.yaml.snakeyaml.Yaml

object AgentConfigLoader {
    fun load(resourcePath: String = "/agent.yml"): AgentConfig {
        val raw = javaClass.getResourceAsStream(resourcePath)?.use { Yaml().load<Map<String, Any?>>(it) }
            ?: error("Agent config not found on classpath: $resourcePath")

        val apiKey = System.getenv("ANTHROPIC_API_KEY")
            ?: raw["apiKey"] as? String
            ?: error("Agent config missing 'apiKey' and ANTHROPIC_API_KEY is unset")

        return AgentConfig(
            apiKey = apiKey,
            systemPrompt = raw["systemPrompt"] as? String ?: error("Agent config missing 'systemPrompt'"),
            userPrompt = raw["userPrompt"] as? String ?: error("Agent config missing 'userPrompt'"),
            skillsDir = raw["skillsDir"] as? String ?: "skills",
            model = resolveModel(raw["model"] as? String ?: error("Agent config missing 'model'")),
        )
    }

    private fun resolveModel(modelId: String): LLModel =
        AnthropicModels.models.firstOrNull { it.id == modelId }
            ?: error("Unknown Anthropic model '$modelId'. Available: ${AnthropicModels.models.joinToString { it.id }}")
}
