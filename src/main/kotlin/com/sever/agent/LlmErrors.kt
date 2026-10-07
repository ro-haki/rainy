package com.sever.agent

/** Turns an LLM/client exception into a concise, user-facing message by matching its message chain. */
object LlmErrors {
    fun userMessage(error: Throwable): String {
        val text = generateSequence(error) { it.cause }
            .joinToString(" | ") { it.message.orEmpty() }
            .lowercase()
        return when {
            text.containsAny("credit balance is too low", "insufficient", "billing") ->
                "The Anthropic account is out of credits — add credits in Plans & Billing."
            text.containsAny("401", "unauthorized", "authentication", "x-api-key") ->
                "Anthropic API authentication failed — check ANTHROPIC_API_KEY."
            text.containsAny("429", "rate limit", "overloaded") ->
                "Anthropic API is rate-limited or overloaded — retry shortly."
            else -> "Agent run failed: ${error.message}"
        }
    }

    private fun String.containsAny(vararg needles: String) = needles.any { it in this }
}
