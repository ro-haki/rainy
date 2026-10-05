package com.sever.tools

interface Command {
    val name: String
    fun run(target: String, arguments: String, timeoutSeconds: Long): CommandResult
}

class CliCommand(
    override val name: String,
    private val executable: String,
    private val runner: CommandRunner,
    private val defaultArguments: List<String> = emptyList(),
    private val targetFlag: String = "",
) : Command {
    override fun run(target: String, arguments: String, timeoutSeconds: Long): CommandResult {
        val targets = target.trim().split(Regex("\\s+")).filter { it.isNotBlank() }

        // argv list, never a shell string, so arguments can't inject extra commands.
        val command = buildList {
            add(executable)
            addAll(defaultArguments)
            addAll(ArgumentTokenizer.tokenize(arguments))
            targets.forEach { t ->
                if (targetFlag.isEmpty()) add(t) else { add(targetFlag); add(t) }
            }
        }
        return runner.run(command, timeoutSeconds)
    }
}
