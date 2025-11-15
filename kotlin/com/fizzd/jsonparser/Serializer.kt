package com.fizzd.jsonparser

class Serializer(private val replacerFunc: ((String, Any?) -> Any?)? = null, private val whitelist: Collection<String>? = null, space: Any? = null) {
    private val result = StringBuilder()
    private var indent = 0
    private var indentStr = ""

    init {
        when (space) {
            is Number -> {
                val n = space.toInt()
                indentStr = " ".repeat(kotlin.math.min(10, kotlin.math.max(0, n)))
            }
            is String -> {
                indentStr = if (space.length > 10) space.substring(0, 10) else space
            }
        }
    }

    fun serialize(obj: Any?): String {
        result.setLength(0)
        serializeValue(obj, "")
        return result.toString()
    }

    private fun serializeValue(valueIn: Any?, key: String = "") {
        var value = valueIn
        if (replacerFunc != null) value = replacerFunc.invoke(key, value)
        when (value) {
            null -> result.append("null")
            is String -> serializeString(value)
            is Boolean -> result.append(value.toString())
            is List<*> -> serializeArray(value as List<Any?>)
            is Map<*, *> -> serializeObject(value as Map<String, Any?>)
            else -> serializeOther(value)
        }
    }

    private fun serializeObject(obj: Map<String, Any?>) {
        var first = true
        result.append('{')
        if (indentStr.isNotEmpty()) {
            result.append('\n')
            indent++
        }
        for (entry in obj.entries) {
            val key = entry.key
            if (whitelist != null && !whitelist.contains(key)) continue
            if (!first) {
                result.append(',')
                if (indentStr.isNotEmpty()) result.append('\n')
            }
            if (indentStr.isNotEmpty()) result.append(indentStr.repeat(indent))
            serializeString(key)
            result.append(':')
            if (indentStr.isNotEmpty()) result.append(' ')
            serializeValue(entry.value, key)
            first = false
        }
        if (indentStr.isNotEmpty()) {
            result.append('\n')
            indent--
            result.append(indentStr.repeat(indent))
        }
        result.append('}')
    }

    private fun serializeArray(array: List<Any?>) {
        result.append('[')
        if (indentStr.isNotEmpty() && array.isNotEmpty()) {
            result.append('\n')
            indent++
        }
        var first = true
        for (i in array.indices) {
            if (!first) {
                result.append(',')
                if (indentStr.isNotEmpty()) result.append('\n')
            }
            if (indentStr.isNotEmpty()) result.append(indentStr.repeat(indent))
            serializeValue(array[i], i.toString())
            first = false
        }
        if (indentStr.isNotEmpty() && array.isNotEmpty()) {
            result.append('\n')
            indent--
            result.append(indentStr.repeat(indent))
        }
        result.append(']')
    }

    private fun serializeString(str: String) {
        result.append('"')
        for (ch in str) {
            when (ch) {
                '\b' -> result.append("\\b")
                '\t' -> result.append("\\t")
                '\n' -> result.append("\\n")
                '\u000C' -> result.append("\\f")
                '\r' -> result.append("\\r")
                '"' -> result.append("\\\"")
                '\\' -> result.append("\\\\")
                else -> {
                    val code = ch.code
                    if (code in 32..126) {
                        result.append(ch)
                    } else {
                        result.append("\\u")
                        result.append(code.toString(16).padStart(4, '0'))
                    }
                }
            }
        }
        result.append('"')
    }

    private fun serializeOther(value: Any?) {
        if (value is Number) {
            val d = value.toDouble()
            if (d.isInfinite() || d.isNaN()) {
                result.append("null")
            } else {
                result.append(value.toString())
            }
        } else {
            serializeString(value.toString())
        }
    }
}