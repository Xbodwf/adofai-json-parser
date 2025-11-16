package com.fizzd.jsonparser

class StringParser {
    fun parse(text: String?, reviver: ((String, Any?) -> Any?)? = null): Any? {
        if (text == null) return null
        val result = ParserX(text).parseValue()
        return if (reviver != null) {
            _applyReviver("", result, reviver)
        } else {
            result
        }
    }

    fun stringify(
        value: Any?,
        replacer: ((String, Any?) -> Any?)? = null,
        space: Any? = null
    ): String {
        val serializer = Serializer(replacer, space)
        return serializer.serialize(value)
    }

    fun stringify(
        value: Any?,
        replacer: List<String>?,
        space: Any? = null
    ): String {
        val serializer = Serializer(replacer, space)
        return serializer.serialize(value)
    }

    private fun _applyReviver(
        key: String,
        value: Any?,
        reviver: (String, Any?) -> Any?
    ): Any? {
        if (value != null && value is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            val map = value as Map<String, Any?>
            val result = mutableMapOf<String, Any?>()
            for ((prop, propValue) in map) {
                result[prop] = _applyReviver(prop, propValue, reviver)
            }
            return reviver(key, result)
        } else if (value is List<*>) {
            val list = value as List<Any?>
            val result = mutableListOf<Any?>()
            for (i in list.indices) {
                result.add(_applyReviver(i.toString(), list[i], reviver))
            }
            return reviver(key, result)
        }
        return reviver(key, value)
    }
}
