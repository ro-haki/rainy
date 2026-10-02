package com.sever.tools.config

import org.yaml.snakeyaml.Yaml

object ScannerConfigLoader {
    fun load(resourceName: String): ScannerConfig {
        val path = "/scanners/$resourceName.yaml"
        val raw = javaClass.getResourceAsStream(path)?.use { Yaml().load<Map<String, Any?>>(it) }
            ?: error("Scanner config not found on classpath: $path")

        return ScannerConfig(
            name = raw.str("name"),
            toolName = raw.str("toolName"),
            description = raw.str("description"),
            executable = raw.str("executable"),
            targetFlag = raw["targetFlag"] as? String ?: "",
            defaultArguments = (raw["defaultArguments"] as? List<*>).orEmpty().map(Any?::toString),
            optionExamples = (raw["optionExamples"] as? List<*>).orEmpty().map(Any?::toString),
            defaultTimeoutSeconds = raw.long("defaultTimeoutSeconds"),
            maxTimeoutSeconds = raw.long("maxTimeoutSeconds"),
        )
    }

    private fun Map<String, Any?>.str(key: String): String =
        this[key] as? String ?: error("Scanner config missing string '$key'")

    private fun Map<String, Any?>.long(key: String): Long =
        (this[key] as? Number)?.toLong() ?: error("Scanner config missing number '$key'")
}
