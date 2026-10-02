package com.sever.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable

@Serializable
data class ScanArgs(
    @property:LLMDescription("Host, IP, hostname or CIDR; several separated by spaces.")
    val target: String,
    @property:LLMDescription("Raw scanner flags; see the tool description for examples. May be empty.")
    val options: String = "",
    @property:LLMDescription("Max seconds before the scan is killed; omit to use the scanner default.")
    val timeoutSeconds: Long? = null,
)
