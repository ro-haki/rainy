package com.sever.tools

interface Scanner {
    val name: String
    fun scan(target: String, options: String, timeoutSeconds: Long): ScanResult
}

class CommandLineScanner(
    override val name: String,
    private val executable: String,
    private val runner: CommandRunner,
    private val defaultArguments: List<String> = emptyList(),
    private val targetFlag: String = "",
) : Scanner {
    override fun scan(target: String, options: String, timeoutSeconds: Long): ScanResult {
        val targets = target.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        require(targets.isNotEmpty()) { "target must not be empty" }

        // argv list, never a shell string, so options can't inject extra commands.
        val command = buildList {
            add(executable)
            addAll(defaultArguments)
            addAll(ArgumentTokenizer.tokenize(options))
            targets.forEach { t ->
                if (targetFlag.isEmpty()) add(t) else { add(targetFlag); add(t) }
            }
        }
        return runner.run(command, timeoutSeconds)
    }
}
