package com.sever.tools

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

interface CommandRunner {
    fun run(command: List<String>, timeoutSeconds: Long, input: String? = null): CommandResult
}

class ProcessCommandRunner : CommandRunner {
    override fun run(command: List<String>, timeoutSeconds: Long, input: String?): CommandResult {
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        // Read async so a full pipe can't deadlock the process and the timeout can bound a hang.
        val output = CompletableFuture.supplyAsync {
            process.inputStream.bufferedReader().use { it.readText() }
        }

        // Feed stdin (if any) and always close it, so tools that read stdin get EOF instead of hanging.
        process.outputStream.use { stdin ->
            if (!input.isNullOrEmpty()) stdin.write(input.toByteArray())
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
