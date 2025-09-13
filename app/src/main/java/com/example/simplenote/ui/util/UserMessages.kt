package com.example.simplenote.ui.util

object UserMessages {
    fun friendlyError(raw: String): String {
        val s = raw.trim()
        val lower = s.lowercase()
        return when {
            "timeout" in lower || "timed out" in lower -> "Network timeout. Please try again."
            "unable to resolve host" in lower || "no address associated with hostname" in lower || "dns" in lower -> "No internet connection."
            "http 401" in lower || "unauthorized" in lower -> "Invalid username or password."
            "http 400" in lower || "bad request" in lower -> "Invalid input. Please check and try again."
            "http 403" in lower || "forbidden" in lower -> "Access denied. Please login again."
            "http 404" in lower || "not found" in lower -> "Requested resource not found."
            "http 500" in lower || "server error" in lower || "internal server error" in lower -> "Server error. Please try again later."
            else -> {
                var msg = s.lineSequence().firstOrNull { it.isNotBlank() } ?: "Something went wrong. Please try again."
                if (msg.startsWith("{") && msg.endsWith("}")) {
                    msg = msg.substring(1, msg.length - 1)
                }
                if (msg.length > 140) msg = msg.take(137) + "..."
                msg
            }
        }
    }

    fun friendlySuccess(default: String?, fallback: String): String {
        val s = (default ?: "").trim()
        if (s.isEmpty()) return fallback
        if (s.startsWith("HTTP", ignoreCase = true)) return fallback
        return if (s.length > 140) s.take(137) + "..." else s
    }
}
