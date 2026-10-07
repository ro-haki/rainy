package com.sever.agent

import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.agent.entity.createStorageKey
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.ReceivedToolResults
import ai.koog.agents.core.dsl.extension.nodeExecuteTools
import ai.koog.agents.core.dsl.extension.onTextMessage
import ai.koog.agents.core.dsl.extension.onToolCalls
import ai.koog.prompt.message.Message

private const val REASONING_PROMPT = "Think about the task and plan the next step."
private const val ACT_PROMPT =
    "Now act: call the appropriate tool to perform the next step. " +
        "If the task is fully complete, reply with the final report instead."

/**
 * A ReAct strategy (reason → act → observe) that never ends a request on an assistant message,
 * so it works with models that reject assistant-prefill. The action step appends a user prompt
 * before requesting the LLM, unlike Koog's built-in [ai.koog.agents.ext.agent.reActStrategy].
 */
fun prefillSafeReActStrategy(reasoningInterval: Int = 1): AIAgentGraphStrategy<String, String> =
    strategy("re_act_no_prefill") {
        require(reasoningInterval > 0) { "Reasoning interval must be greater than 0" }
        val reasoningStepKey = createStorageKey<Int>("reasoning_step")

        val nodeSetup by node<String, String> {
            storage.set(reasoningStepKey, 0)
            it
        }

        val nodeReasonFromInput by node<String, Unit> { input ->
            llm.writeSession {
                appendPrompt {
                    user(input)
                    user(REASONING_PROMPT)
                }
                requestLLMWithoutTools()
            }
        }

        val nodeAct by node<Unit, Message.Assistant> {
            llm.writeSession {
                appendPrompt { user(ACT_PROMPT) }
                requestLLM()
            }
        }

        val nodeExecuteTools by nodeExecuteTools()

        val nodeReasonFromResults by node<ReceivedToolResults, Unit> { results ->
            val step = storage.getValue(reasoningStepKey)
            llm.writeSession {
                appendPrompt {
                    user {
                        results.toolResults.forEach { toolResult(it.toMessagePart()) }
                    }
                }
                if (step % reasoningInterval == 0) {
                    appendPrompt { user(REASONING_PROMPT) }
                    requestLLMWithoutTools()
                }
            }
            storage.set(reasoningStepKey, step + 1)
        }

        edge(nodeStart forwardTo nodeSetup)
        edge(nodeSetup forwardTo nodeReasonFromInput)
        edge(nodeReasonFromInput forwardTo nodeAct)
        edge(nodeAct forwardTo nodeExecuteTools onToolCalls { true })
        edge(nodeAct forwardTo nodeFinish onTextMessage { true })
        edge(nodeExecuteTools forwardTo nodeReasonFromResults)
        edge(nodeReasonFromResults forwardTo nodeAct)
    }
