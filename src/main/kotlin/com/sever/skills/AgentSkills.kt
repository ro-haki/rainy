package com.sever.skills

import ai.koog.rag.base.files.JVMFileSystemProvider
import ai.koog.skills.discovery.discoverSkills
import ai.koog.skills.prompt.SkillsPromptFormat
import ai.koog.skills.prompt.generateSkillsPrompt
import org.slf4j.LoggerFactory
import java.io.File

object AgentSkills {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun promptSection(skillsDir: String): String {
        val root = File(skillsDir).absoluteFile
        if (!root.isDirectory) {
            log.info("No skills directory at {}", root.path)
            return ""
        }

        val skills = discoverSkills(JVMFileSystemProvider.ReadOnly, listOf(root.path))
        log.info("Loaded {} skills: {}", skills.size, skills.joinToString { it.name })
        if (skills.isEmpty()) return ""

        return generateSkillsPrompt(skills, SkillsPromptFormat.YML)
    }
}
