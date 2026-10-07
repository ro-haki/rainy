package com.sever.server

import ai.koog.agents.core.tools.ToolRegistry
import com.sever.agent.AgentFactory
import com.sever.agent.LlmErrors
import com.sever.history.SessionHistory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

/** One chat conversation. Keeps its transcript so each message is answered with prior context. */
class ChatSession(
    id: String,
    private val toolRegistry: ToolRegistry,
    private val systemPrompt: String,
    private val agentFactory: AgentFactory,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val history = SessionHistory(id)
    private val transcript = StringBuilder()
    private val mutex = Mutex()

    suspend fun send(message: String): String = mutex.withLock {
        history.userPrompt(message)

        val input = if (transcript.isEmpty()) message else "$transcript\nUser: $message"
        val agent = agentFactory.create(systemPrompt, toolRegistry, history)

        val response = try {
            agent.run(input)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val errorMessage = LlmErrors.userMessage(e)
            log.error("Chat turn failed: {}", errorMessage)
            history.error(errorMessage)
            return@withLock errorMessage
        }

        history.agentResponse(response)
        transcript.append("User: ").append(message).append("\n\nAssistant: ").append(response).append("\n\n")
        response
    }
}
