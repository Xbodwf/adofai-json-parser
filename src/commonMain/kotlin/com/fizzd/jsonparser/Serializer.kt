package com.fizzd.jsonparser

class Serializer(
    private val replacer: Any? = null,
    private val space: Any? = null
) {
    private var result: StringBuilder = StringBuilder()
    private var indent: Int = 0
    private val indentStr: String

    init {
        indentStr = when {
            space is Number -> {
                val num = (space as Number).toInt().coerceIn(0, 10)
                " ".repeat(num)
            }
            space is String -> space.take(10)
            else -> ""
        }
    }

    fun serialize(obj: Any?): String {
        result = StringBuilder()
        serializeValue(obj, "")
        return result.toString()
    }

    private fun serializeValue(value: Any?, key: String = "") {
        var processedValue = value
        @Suppress("UNCHECKED_CAST")
        val replacerFunc = replacer as? ((String, Any?) -> Any?)
        if (replacerFunc != null) {
            processedValue = replacerFunc(key, value)
        }
        when {
            processedValue == null -> result.append("null")
            processedValue is String -> serializeString(processedValue)
            processedValue is Boolean -> result.append(processedValue.toString())
            processedValue is List<*> -> serializeArray(processedValue)
            processedValue is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                serializeObject(processedValue as Map<String, Any?>)
            }
            else -> serializeOther(processedValue)
        }
    }

    private fun serializeObject(obj: Map<String, Any?>) {
        var first = true
        result.append("{")
        if (indentStr.isNotEmpty()) {
            result.append("\n")
            indent++
        }
        for ((key, value) in obj) {
            val replacerList = replacer as? List<*>
            if (replacerList != null && !replacerList.contains(key)) {
                continue
            }
            if (!first) {
                result.append(",")
                if (indentStr.isNotEmpty()) result.append("\n")
            }
            if (indentStr.isNotEmpty()) {
                result.append(indentStr.repeat(indent))
            }
            serializeString(key.toString())
            result.append(":")
            if (indentStr.isNotEmpty()) result.append(" ")
            serializeValue(value, key)
            first = false
        }
        if (indentStr.isNotEmpty()) {
            result.append("\n")
            indent--
            result.append(indentStr.repeat(indent))
        }
        result.append("}")
    }

    private fun serializeArray(array: List<*>) {
        result.append("[")
        if (indentStr.isNotEmpty() && array.isNotEmpty()) {
            result.append("\n")
            indent++
        }
        var first = true
        for (i in array.indices) {
            if (!first) {
                result.append(",")
                if (indentStr.isNotEmpty()) result.append("\n")
            }
            if (indentStr.isNotEmpty()) {
                result.append(indentStr.repeat(indent))
            }
            serializeValue(array[i], i.toString())
            first = false
        }
        if (indentStr.isNotEmpty() && array.isNotEmpty()) {
            result.append("\n")
            indent--
            result.append(indentStr.repeat(indent))
        }
        result.append("]")
    }

    private fun serializeString(str: String) {
        result.append("\"")
        for (char in str) {
            when (char) {
                '\b' -> result.append("\\b")
                '\t' -> result.append("\\t")
                '\n' -> result.append("\\n")
                '\u000C' -> result.append("\\f")
                '\r' -> result.append("\\r")
                '"' -> result.append("\\\"")
                '\\' -> result.append("\\\\")
                else -> {
                    val code = char.code
                    if (code in 32..126) {
                        result.append(char)
                    } else {
                        result.append("\\u")
                        result.append(code.toString(16).padStart(4, '0'))
                    }
                }
            }
        }
        result.append("\"")
    }

    private fun serializeOther(value: Any?) {
        when (value) {
            is Number -> {
                if (value.toDouble().isFinite()) {
                    result.append(value.toString())
                } else {
                    result.append("null")
                }
            }
            else -> serializeString(value.toString())
        }
    }
}

