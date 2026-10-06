package com.sever.tools

import kotlin.test.Test
import kotlin.test.assertEquals

class CliCommandTest {

    private class CapturingCommandRunner : CommandRunner {
        var capturedCommand: List<String> = emptyList()
        var capturedTimeout: Long = -1

        override fun run(command: List<String>, timeoutSeconds: Long, input: String?): CommandResult {
            capturedCommand = command
            capturedTimeout = timeoutSeconds
            return CommandResult(command, exitCode = 0, output = "", timedOut = false)
        }
    }

    private fun request(
        target: String = "example.com",
        arguments: String = "",
        modeArguments: List<String> = emptyList(),
        timeoutSeconds: Long = 300,
    ) = CommandRequest(target, arguments, modeArguments, input = null, timeoutSeconds = timeoutSeconds)

    @Test
    fun `assembles argv as executable then defaults then mode then arguments then targets`() {
        val runner = CapturingCommandRunner()
        val command = CliCommand("nmap", "nmap", runner, defaultArguments = listOf("--stats"))

        command.run(request(target = "host", arguments = "-sV -p 80", modeArguments = listOf("-T4")))

        assertEquals(listOf("nmap", "--stats", "-T4", "-sV", "-p", "80", "host"), runner.capturedCommand)
    }

    @Test
    fun `empty target flag places targets positionally`() {
        val runner = CapturingCommandRunner()
        val command = CliCommand("nmap", "nmap", runner, targetFlag = "")

        command.run(request(target = "a.com b.com"))

        assertEquals(listOf("nmap", "a.com", "b.com"), runner.capturedCommand)
    }

    @Test
    fun `non-empty target flag precedes each target`() {
        val runner = CapturingCommandRunner()
        val command = CliCommand("nuclei", "nuclei", runner, targetFlag = "-u")

        command.run(request(target = "a.com b.com"))

        assertEquals(listOf("nuclei", "-u", "a.com", "-u", "b.com"), runner.capturedCommand)
    }

    @Test
    fun `blank target produces no target tokens`() {
        val runner = CapturingCommandRunner()
        val command = CliCommand("whoami", "whoami", runner)

        command.run(request(target = "   "))

        assertEquals(listOf("whoami"), runner.capturedCommand)
    }

    @Test
    fun `timeout is forwarded to the runner`() {
        val runner = CapturingCommandRunner()
        val command = CliCommand("nmap", "nmap", runner)

        command.run(request(timeoutSeconds = 120))

        assertEquals(120, runner.capturedTimeout)
    }
}
