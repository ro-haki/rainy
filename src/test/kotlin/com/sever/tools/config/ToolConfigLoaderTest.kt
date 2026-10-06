package com.sever.tools.config

import kotlin.test.Test
import kotlin.test.assertEquals

class ToolConfigLoaderTest {

    @Test
    fun `discovers every bundled tool config by name`() {
        val expected = listOf(
            "alterx", "dnsx", "gau", "masscan", "nmap", "nuclei", "rustscan", "subfinder", "waybackurls",
        )
        assertEquals(expected, ToolConfigLoader.availableNames())
    }

    @Test
    fun `loads the identifying fields of the nmap config`() {
        val config = ToolConfigLoader.load("nmap")

        assertEquals("nmap", config.name)
        assertEquals("nmap", config.executable)
        assertEquals("", config.targetFlag)
    }

    @Test
    fun `loads rate modes and the default mode`() {
        val config = ToolConfigLoader.load("nmap")

        assertEquals(mapOf("slow" to listOf("-T2"), "normal" to listOf("-T3"), "fast" to listOf("-T4")), config.modes)
        assertEquals("normal", config.defaultMode)
    }

    @Test
    fun `loads the timeout policy`() {
        val config = ToolConfigLoader.load("nmap")

        assertEquals(300, config.defaultTimeoutSeconds)
        assertEquals(3600, config.maxTimeoutSeconds)
    }

    @Test
    fun `loads argument examples as arguments with descriptions`() {
        val config = ToolConfigLoader.load("nmap")

        val first = config.argumentExamples.first()
        assertEquals("-sV", first.arguments)
        assertEquals("Detect the service and version behind each open port", first.description)
        assertEquals(10, config.argumentExamples.size)
    }

    @Test
    fun `targets go positionally so nmap has no target flag`() {
        assertEquals("", ToolConfigLoader.load("nmap").targetFlag)
    }
}
