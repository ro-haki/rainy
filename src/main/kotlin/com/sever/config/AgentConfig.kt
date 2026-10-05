package com.sever.config

import ai.koog.prompt.llm.LLModel

data class AgentConfig(
    val apiKey: String,
    val systemPrompt: String,
    val userPrompt: String,
    val skillsDir: String,
    val model: LLModel,
)
