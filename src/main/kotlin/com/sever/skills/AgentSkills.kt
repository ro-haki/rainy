package com.sever.skills

import ai.koog.rag.base.files.JVMFileSystemProvider
import ai.koog.skills.discovery.discoverSkills
import ai.koog.skills.prompt.SkillsPromptFormat
import ai.koog.skills.prompt.generateSkillsPrompt
import java.io.File

object AgentSkills {
    suspend fun promptSection(skillsDir: String): String {
        val root = File(skillsDir).absoluteFile
        if (!root.isDirectory) return ""

        val skills = discoverSkills(JVMFileSystemProvider.ReadOnly, listOf(root.path))
        if (skills.isEmpty()) return ""

        return generateSkillsPrompt(skills, SkillsPromptFormat.YML)
    }
}
