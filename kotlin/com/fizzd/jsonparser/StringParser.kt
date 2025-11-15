package com.fizzd.jsonparser

class StringParser {
    fun parse(text: String?, reviver: ((String, Any?) -> Any?)? = null): Any? {
        if (text == null) return null
        val result = ParserX(text).parseValue()
        return if (reviver != null) _applyReviver("", result, reviver) else result
    }

    fun stringify(value: Any?, replacer: ((String, Any?) -> Any?)?, space: Any?): String {
        val serializer = Serializer(replacerFunc = replacer, whitelist = null, space = space)
        return serializer.serialize(value)
    }

    fun stringify(value: Any?, whitelist: Collection<String>?, space: Any?): String {
        val serializer = Serializer(replacerFunc = null, whitelist = whitelist, space = space)
        return serializer.serialize(value)
    }

    private fun _applyReviver(key: String, value: Any?, reviver: (String, Any?) -> Any?): Any? {
        var v = value
        if (v is MutableList<*>) {
            val list = v as MutableList<Any?>
            for (i in list.indices) {
                list[i] = _applyReviver(i.toString(), list[i], reviver)
            }
            v = list
        } else if (v is MutableMap<*, *>) {
            val map = v as MutableMap<String, Any?>
            for (prop in map.keys.toList()) {
                map[prop] = _applyReviver(prop, map[prop], reviver)
            }
            v = map
        }
        return reviver(key, v)
    }
}