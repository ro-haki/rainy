package com.sever.tools.config

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.net.JarURLConnection

object ToolConfigLoader {
    private const val DIR = "tools"

    /** Names of every tool config yaml under the resources tools directory, discovered at runtime. */
    fun availableNames(): List<String> {
        val url = javaClass.getResource("/$DIR") ?: return emptyList()
        return when (url.protocol) {
            "file" -> File(url.toURI()).listFiles()
                ?.filter { it.isFile && it.extension == "yaml" }
                ?.map { it.nameWithoutExtension }
                .orEmpty()

            "jar" -> (url.openConnection() as JarURLConnection).jarFile.use { jar ->
                jar.entries().asSequence()
                    .map { it.name }
                    .filter { it.startsWith("$DIR/") && it.endsWith(".yaml") }
                    .map { it.substringAfter("$DIR/").removeSuffix(".yaml") }
                    .filter { it.isNotEmpty() && !it.contains('/') }
                    .toList()
            }

            else -> emptyList()
        }.sorted()
    }

    fun load(resourceName: String): ToolConfig {
        val path = "/tools/$resourceName.yaml"
        val raw = javaClass.getResourceAsStream(path)?.use { Yaml().load<Map<String, Any?>>(it) }
            ?: error("Tool config not found on classpath: $path")

        return ToolConfig(
            name = raw.str("name"),
            description = raw.str("description"),
            executable = raw.str("executable"),
            targetFlag = raw["targetFlag"] as? String ?: "",
            defaultArguments = (raw["defaultArguments"] as? List<*>).orEmpty().map(Any?::toString),
            modes = parseModes(raw["modes"]),
            defaultMode = raw["defaultMode"] as? String ?: "normal",
            argumentExamples = parseExamples(raw["examples"]),
            defaultTimeoutSeconds = raw.long("defaultTimeoutSeconds"),
            maxTimeoutSeconds = raw.long("maxTimeoutSeconds"),
        )
    }

    private fun parseModes(value: Any?): Map<String, List<String>> =
        (value as? Map<*, *>)?.entries?.associate { (key, v) ->
            key.toString() to (v as? List<*>).orEmpty().map(Any?::toString)
        } ?: emptyMap()

    private fun parseExamples(value: Any?): List<ArgumentExample> =
        (value as? List<*>).orEmpty().mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            val args = map["arguments"]?.toString() ?: return@mapNotNull null
            ArgumentExample(args, map["description"]?.toString().orEmpty())
        }

    private fun Map<String, Any?>.str(key: String): String =
        this[key] as? String ?: error("Tool config missing string '$key'")

    private fun Map<String, Any?>.long(key: String): Long =
        (this[key] as? Number)?.toLong() ?: error("Tool config missing number '$key'")
}
