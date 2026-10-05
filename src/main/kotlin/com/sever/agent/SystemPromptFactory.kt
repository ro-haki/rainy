package com.sever.agent

import com.sever.config.AgentConfig
import com.sever.skills.AgentSkills

object SystemPromptFactory {
    suspend fun build(config: AgentConfig): String = buildString {
        append(config.systemPrompt)
        val skills = AgentSkills.promptSection(config.skillsDir)
        if (skills.isNotBlank()) {
            append("\n\n# Available skills\n")
            append(skills)
        }
    }
}
