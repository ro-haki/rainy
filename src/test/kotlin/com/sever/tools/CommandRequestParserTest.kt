package com.sever.tools

import kotlin.test.Test
import kotlin.test.assertEquals

class CommandRequestParserTest {

    private val modes = mapOf(
        "slow" to listOf("-T2"),
        "normal" to listOf("-T3"),
        "fast" to listOf("-T4"),
    )

    private fun parser(defaultMode: String = "normal", default: Long = 300, max: Long = 3600) =
        CommandRequestParser(modes, defaultMode, default, max)

    @Test
    fun `known mode resolves to its arguments`() {
        val request = parser().parse(CommandArgs(mode = "fast"))
        assertEquals(listOf("-T4"), request.modeArguments)
    }

    @Test
    fun `mode is matched case-insensitively`() {
        val request = parser().parse(CommandArgs(mode = "FAST"))
        assertEquals(listOf("-T4"), request.modeArguments)
    }

    @Test
    fun `unknown mode falls back to the default mode`() {
        val request = parser(defaultMode = "slow").parse(CommandArgs(mode = "ludicrous"))
        assertEquals(listOf("-T2"), request.modeArguments)
    }

    @Test
    fun `null mode uses the default mode`() {
        val request = parser(defaultMode = "normal").parse(CommandArgs(mode = null))
        assertEquals(listOf("-T3"), request.modeArguments)
    }

    @Test
    fun `null timeout uses the default timeout`() {
        val request = parser(default = 300).parse(CommandArgs(timeoutSeconds = null))
        assertEquals(300, request.timeoutSeconds)
    }

    @Test
    fun `timeout above the max is clamped to the max`() {
        val request = parser(max = 3600).parse(CommandArgs(timeoutSeconds = 999_999))
        assertEquals(3600, request.timeoutSeconds)
    }

    @Test
    fun `non-positive timeout is clamped up to one`() {
        val request = parser().parse(CommandArgs(timeoutSeconds = 0))
        assertEquals(1, request.timeoutSeconds)
    }

    @Test
    fun `target is trimmed`() {
        val request = parser().parse(CommandArgs(target = "  example.com  "))
        assertEquals("example.com", request.target)
    }

    @Test
    fun `arguments are passed through unchanged`() {
        val request = parser().parse(CommandArgs(arguments = "-sV -p 80"))
        assertEquals("-sV -p 80", request.arguments)
    }
}
