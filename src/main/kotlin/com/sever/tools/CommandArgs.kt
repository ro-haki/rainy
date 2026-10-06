package com.sever.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable

@Serializable
data class CommandArgs(
    @property:LLMDescription("Primary target/operand if the tool needs one (host, IP, URL, path, ...). May be empty.")
    val target: String = "",
    @property:LLMDescription("Command-line flags/arguments; see the tool description for examples. May be empty.")
    val arguments: String = "",
    @property:LLMDescription("Rate/intensity mode: 'slow', 'normal' or 'fast'. Omit to use the tool default.")
    val mode: String? = null,
    @property:LLMDescription("Text piped to the tool's standard input, e.g. a newline-separated host list. Omit if not needed.")
    val input: String? = null,
    @property:LLMDescription("Max seconds before the command is killed; omit to use the tool default.")
    val timeoutSeconds: Long? = null,
)
