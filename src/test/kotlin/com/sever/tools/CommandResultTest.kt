package com.sever.tools

import kotlin.test.Test
import kotlin.test.assertEquals

class CommandResultTest {

    @Test
    fun `formats command line, exit code and output`() {
        val result = CommandResult(listOf("nmap", "-sV", "host"), exitCode = 0, output = "open 80", timedOut = false)

        assertEquals("$ nmap -sV host\n(exit code 0)\n\nopen 80", result.formatted())
    }

    @Test
    fun `reports a timed-out run instead of an exit code`() {
        val result = CommandResult(listOf("nmap"), exitCode = null, output = "partial", timedOut = true)

        assertEquals("$ nmap\n(timed out)\n\npartial", result.formatted())
    }

    @Test
    fun `blank output is rendered as no output`() {
        val result = CommandResult(listOf("nmap"), exitCode = 0, output = "   ", timedOut = false)

        assertEquals("$ nmap\n(exit code 0)\n\n(no output)", result.formatted())
    }
}
