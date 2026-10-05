package com.sever

import com.sever.agent.SecurityAgent
import com.sever.config.AgentConfigLoader
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("com.sever.Main")

fun main(args: Array<String>) {
    val userPrompt = args.joinToString(" ").trim()
    if (userPrompt.isEmpty()) {
        log.error("No task prompt provided. Pass it as an argument, e.g. \"scan scanme.nmap.org\"")
        return
    }

    val config = AgentConfigLoader.load()
    val response = SecurityAgent(config).run(userPrompt)
    log.info("Agent response:\n{}", response)
}
