package com.sever.skills

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.serialization.typeToken
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import java.io.File

@Serializable
data class SkillArgs(
    @property:LLMDescription("The skill name to load, exactly as listed in the available skills.")
    val name: String,
)

class SkillTool(private val skillsDir: String) :
    SimpleTool<SkillArgs>(typeToken<SkillArgs>(), "load_skill", DESCRIPTION) {
    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun execute(args: SkillArgs): String {
        val name = args.name.trim()
        if (!name.matches(SKILL_NAME)) return "Invalid skill name '${args.name}'."

        val file = File(skillsDir, "$name/SKILL.md")
        if (!file.isFile) return "Unknown skill '$name'."

        log.info("Skill loaded: {}", name)
        return file.readText()
    }

    private companion object {
        const val DESCRIPTION =
            "Load the full playbook for a named skill. Call this before following a skill, then do what it says."
        val SKILL_NAME = Regex("[a-z0-9-]+")
    }
}

object SkillTools {
    fun registry(skillsDir: String): ToolRegistry = ToolRegistry { tool(SkillTool(skillsDir)) }
}
