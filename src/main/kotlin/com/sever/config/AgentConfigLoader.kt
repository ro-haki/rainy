package com.sever.config

import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.llm.LLModel
import org.yaml.snakeyaml.Yaml
import java.io.File

object AgentConfigLoader {
    fun load(resourcePath: String = "/agent.yml"): AgentConfig {
        val external = File("agent.yml")
        val raw = if (external.isFile) {
            external.inputStream().use { Yaml().load(it) }
        } else {
            javaClass.getResourceAsStream(resourcePath)?.use { Yaml().load<Map<String, Any?>>(it) }
                ?: error("Agent config not found (no ./agent.yml and no classpath $resourcePath)")
        }

        return AgentConfig(
            apiKey = Env.require("ANTHROPIC_API_KEY"),
            model = resolveModel(Env.require("ANTHROPIC_MODEL")),
            systemPrompt = raw["systemPrompt"] as? String ?: error("Agent config missing 'systemPrompt'"),
            skillsDir = raw["skillsDir"] as? String ?: "skills",
        )
    }

    private fun resolveModel(modelId: String): LLModel =
        AnthropicModels.models.firstOrNull { it.id == modelId }
            ?: error("Unknown Anthropic model '$modelId'. Available: ${AnthropicModels.models.joinToString { it.id }}")
}
