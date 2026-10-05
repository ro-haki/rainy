package com.sever.tools

import ai.koog.agents.core.tools.ToolRegistry
import com.sever.tools.config.ToolConfigLoader
import org.slf4j.LoggerFactory

object CommandTools {
    private val log = LoggerFactory.getLogger(javaClass)

    fun registry(): ToolRegistry {
        val runner = ProcessCommandRunner()
        val names = ToolConfigLoader.availableNames()
        log.info("Loaded {} command tools: {}", names.size, names.joinToString())
        return ToolRegistry { tools(names.map { CommandToolFactory.create(it, runner) }) }
    }
}
