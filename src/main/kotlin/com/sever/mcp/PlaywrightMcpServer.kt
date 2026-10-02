package com.sever.mcp

fun playwrightMcpServer(): McpServer = ProcessMcpServer(
    name = "playwright",
    command = listOf("npx", "@playwright/mcp", "--headless", "--browser=chromium", "--no-sandbox"),
)
