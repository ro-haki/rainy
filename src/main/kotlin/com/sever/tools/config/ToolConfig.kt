package com.sever.tools.config

data class ArgumentExample(
    val arguments: String,
    val description: String,
)

data class ToolConfig(
    val name: String,
    val description: String,
    val executable: String,
    val targetFlag: String,
    val defaultArguments: List<String>,
    val modes: Map<String, List<String>>,
    val defaultMode: String,
    val argumentExamples: List<ArgumentExample>,
    val defaultTimeoutSeconds: Long,
    val maxTimeoutSeconds: Long,
)
