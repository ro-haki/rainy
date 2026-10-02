package com.sever.tools

class ScanService(
    private val scannerName: String,
    private val scanner: Scanner,
    private val parser: ScanRequestParser,
) {
    fun run(target: String, options: String, timeoutSeconds: Long?): String {
        val request = try {
            parser.parse(target, options, timeoutSeconds)
        } catch (e: IllegalArgumentException) {
            return "Error: ${e.message}"
        }
        return try {
            scanner.scan(request.target, request.options, request.timeoutSeconds).formatted()
        } catch (e: Exception) {
            "Failed to run $scannerName: ${e.message}."
        }
    }
}
