package com.sever.tools.config

data class ToolConfig(
    val name: String,
    val toolName: String,
    val description: String,
    val executable: String,
    val targetFlag: String,
    val defaultArguments: List<String>,
    val argumentExamples: List<String>,
    val defaultTimeoutSeconds: Long,
    val maxTimeoutSeconds: Long,
)
