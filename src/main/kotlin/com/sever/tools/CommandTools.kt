package com.sever.tools

import ai.koog.agents.core.tools.ToolRegistry
import com.sever.tools.config.ToolConfigLoader

object CommandTools {
    fun registry(): ToolRegistry {
        val runner = ProcessCommandRunner()
        val tools = ToolConfigLoader.availableNames().map { CommandToolFactory.create(it, runner) }
        return ToolRegistry { tools(tools) }
    }
}
