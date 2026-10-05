package com.sever.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable

@Serializable
data class CommandArgs(
    @property:LLMDescription("Primary target/operand if the tool needs one (host, IP, URL, path, ...). May be empty.")
    val target: String = "",
    @property:LLMDescription("Command-line flags/arguments; see the tool description for examples. May be empty.")
    val arguments: String = "",
    @property:LLMDescription("Max seconds before the command is killed; omit to use the tool default.")
    val timeoutSeconds: Long? = null,
)
