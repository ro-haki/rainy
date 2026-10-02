package com.sever.tools.config

data class ScannerConfig(
    val name: String,
    val toolName: String,
    val description: String,
    val executable: String,
    val targetFlag: String,
    val defaultArguments: List<String>,
    val optionExamples: List<String>,
    val defaultTimeoutSeconds: Long,
    val maxTimeoutSeconds: Long,
)
