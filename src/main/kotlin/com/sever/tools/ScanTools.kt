package com.sever.tools

import ai.koog.agents.core.tools.ToolRegistry

object ScanTools {
    private val scanners = listOf("nmap", "masscan", "nuclei")

    fun registry(): ToolRegistry {
        val runner = ProcessCommandRunner()
        return ToolRegistry {
            tools(scanners.map { ScanToolFactory.create(it, runner) })
        }
    }
}
