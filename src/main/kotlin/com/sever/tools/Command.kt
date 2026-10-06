package com.sever.tools

interface Command {
    val name: String
    fun run(request: CommandRequest): CommandResult
}

class CliCommand(
    override val name: String,
    private val executable: String,
    private val runner: CommandRunner,
    private val defaultArguments: List<String> = emptyList(),
    private val targetFlag: String = "",
) : Command {
    override fun run(request: CommandRequest): CommandResult {
        val targets = request.target.split(Regex("\\s+")).filter { it.isNotBlank() }

        // argv list, never a shell string, so arguments can't inject extra commands.
        val command = buildList {
            add(executable)
            addAll(defaultArguments)
            addAll(request.modeArguments)
            addAll(ArgumentTokenizer.tokenize(request.arguments))
            targets.forEach { target ->
                if (targetFlag.isEmpty()) add(target) else { add(targetFlag); add(target) }
            }
        }
        return runner.run(command, request.timeoutSeconds, request.input)
    }
}
