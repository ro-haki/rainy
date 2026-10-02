package com.sever.tools

data class ScanRequest(
    val target: String,
    val options: String,
    val timeoutSeconds: Long,
)

class ScanRequestParser(
    private val defaultTimeoutSeconds: Long,
    private val maxTimeoutSeconds: Long,
) {
    fun parse(target: String, options: String, timeoutSeconds: Long?): ScanRequest {
        val cleanTarget = target.trim()
        require(cleanTarget.isNotEmpty()) { "'target' is required." }
        val resolved = (timeoutSeconds ?: defaultTimeoutSeconds).coerceIn(1, maxTimeoutSeconds)
        return ScanRequest(cleanTarget, options, resolved)
    }
}
