package com.sever.tools

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

interface CommandRunner {
    fun run(command: List<String>, timeoutSeconds: Long): CommandResult
}

class ProcessCommandRunner : CommandRunner {
    override fun run(command: List<String>, timeoutSeconds: Long): CommandResult {
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        // Read async so a full pipe can't deadlock the process and the timeout can bound a hang.
        val output = CompletableFuture.supplyAsync {
            process.inputStream.bufferedReader().use { it.readText() }
        }

        val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        if (!finished) process.destroyForcibly()

        val captured = runCatching { output.get(5, TimeUnit.SECONDS) }.getOrDefault("")
        return CommandResult(
            command = command,
            exitCode = if (finished) process.exitValue() else null,
            output = captured,
            timedOut = !finished,
        )
    }
}
