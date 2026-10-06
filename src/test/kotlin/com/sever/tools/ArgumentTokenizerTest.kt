package com.sever.tools

import kotlin.test.Test
import kotlin.test.assertEquals

class ArgumentTokenizerTest {

    @Test
    fun `splits on whitespace into separate tokens`() {
        assertEquals(listOf("-sV", "-p", "1-1000"), ArgumentTokenizer.tokenize("-sV -p 1-1000"))
    }

    @Test
    fun `collapses runs of whitespace`() {
        assertEquals(listOf("a", "b"), ArgumentTokenizer.tokenize("  a   \t b  "))
    }

    @Test
    fun `keeps double-quoted value as a single token`() {
        assertEquals(listOf("--script", "http-title and safe"), ArgumentTokenizer.tokenize("--script \"http-title and safe\""))
    }

    @Test
    fun `keeps single-quoted value as a single token`() {
        assertEquals(listOf("a b"), ArgumentTokenizer.tokenize("'a b'"))
    }

    @Test
    fun `empty input yields no tokens`() {
        assertEquals(emptyList(), ArgumentTokenizer.tokenize(""))
    }

    @Test
    fun `blank input yields no tokens`() {
        assertEquals(emptyList(), ArgumentTokenizer.tokenize("   "))
    }
}
