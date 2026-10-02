package com.sever.tools

object ArgumentTokenizer {
    fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var quote: Char? = null
        for (c in input) {
            when {
                quote != null -> if (c == quote) quote = null else current.append(c)
                c == '"' || c == '\'' -> quote = c
                c.isWhitespace() -> if (current.isNotEmpty()) { tokens.add(current.toString()); current.clear() }
                else -> current.append(c)
            }
        }
        if (current.isNotEmpty()) tokens.add(current.toString())
        return tokens
    }
}
