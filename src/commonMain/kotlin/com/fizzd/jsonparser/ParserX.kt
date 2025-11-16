package com.fizzd.jsonparser

class ParserX(private val json: String, private val endSection: String? = null) {
    companion object {
        private const val WHITE_SPACE = " \t\n\r\uFEFF"
        private const val WORD_BREAK = " \t\n\r{}[],:\""
        
        const val TOKEN_NONE = 0
        const val TOKEN_CURLY_OPEN = 1
        const val TOKEN_CURLY_CLOSE = 2
        const val TOKEN_SQUARED_OPEN = 3
        const val TOKEN_SQUARED_CLOSE = 4
        const val TOKEN_COLON = 5
        const val TOKEN_COMMA = 6
        const val TOKEN_STRING = 7
        const val TOKEN_NUMBER = 8
        const val TOKEN_TRUE = 9
        const val TOKEN_FALSE = 10
        const val TOKEN_NULL = 11
    }

    private var position: Int = 0

    init {
        if (peek() == 0xFEFF.toInt()) {
            read()
        }
    }

    fun parseValue(): Any? {
        return parseByToken(nextToken)
    }

    fun parseObject(): Map<String, Any?>? {
        val obj = mutableMapOf<String, Any?>()
        read()
        while (true) {
            var nextToken: Int
            do {
                nextToken = this.nextToken
                if (nextToken == TOKEN_NONE) {
                    return null
                }
                if (nextToken == TOKEN_CURLY_CLOSE) {
                    return obj
                }
            } while (nextToken == TOKEN_COMMA)
            val key = parseString()
            if (key == null) {
                return null
            }
            if (this.nextToken != TOKEN_COLON) {
                return null
            }
            if (endSection == null || key != endSection) {
                read()
                obj[key] = parseValue()
            } else {
                return obj
            }
        }
    }

    fun parseArray(): List<Any?>? {
        val array = mutableListOf<Any?>()
        read()
        var parsing = true
        while (parsing) {
            val nextToken = this.nextToken
            when (nextToken) {
                TOKEN_NONE -> return null
                TOKEN_SQUARED_CLOSE -> parsing = false
                TOKEN_COMMA -> { /* continue */ }
                else -> {
                    val value = parseByToken(nextToken)
                    array.add(value)
                }
            }
        }
        return array
    }

    private fun parseByToken(token: Int): Any? {
        return when (token) {
            TOKEN_CURLY_OPEN -> parseObject()
            TOKEN_SQUARED_OPEN -> parseArray()
            TOKEN_STRING -> parseString()
            TOKEN_NUMBER -> parseNumber()
            TOKEN_TRUE -> true
            TOKEN_FALSE -> false
            TOKEN_NULL -> null
            else -> null
        }
    }

    fun parseString(): String? {
        var result = ""
        read()
        var parsing = true
        while (parsing) {
            if (peek() == -1) {
                break
            }
            val char = nextChar
            when (char) {
                "\"" -> parsing = false
                "\\" -> {
                    if (peek() == -1) {
                        parsing = false
                        break
                    }
                    val escaped = nextChar
                    when (escaped) {
                        "\"", "/", "\\" -> result += escaped
                        "b" -> result += "\b"
                        "f" -> result += "\u000C"
                        "n" -> result += "\n"
                        "r" -> result += "\r"
                        "t" -> result += "\t"
                        "u" -> {
                            var unicode = ""
                            for (i in 0 until 4) {
                                unicode += nextChar
                            }
                            result += unicode.toInt(16).toChar()
                        }
                    }
                }
                else -> result += char
            }
        }
        return result
    }

    fun parseNumber(): Number {
        val word = nextWord
        return if (word.indexOf(".") == -1) {
            word.toIntOrNull() ?: 0
        } else {
            word.toDoubleOrNull() ?: 0.0
        }
    }

    private fun eatWhitespace() {
        while (WHITE_SPACE.indexOf(peekChar) != -1) {
            read()
            if (peek() == -1) {
                break
            }
        }
    }

    private fun peek(): Int {
        if (position >= json.length) {
            return -1
        }
        return json[position].code
    }

    private fun read(): Int {
        if (position >= json.length) {
            return -1
        }
        return json[position++].code
    }

    private val peekChar: String
        get() {
            val code = peek()
            return if (code == -1) "\u0000" else code.toChar().toString()
        }

    private val nextChar: String
        get() {
            val code = read()
            return if (code == -1) "\u0000" else code.toChar().toString()
        }

    private val nextWord: String
        get() {
            var result = ""
            while (WORD_BREAK.indexOf(peekChar) == -1) {
                result += nextChar
                if (peek() == -1) {
                    break
                }
            }
            return result
        }

    private val nextToken: Int
        get() {
            eatWhitespace()
            if (peek() == -1) {
                return TOKEN_NONE
            }
            val char = peekChar
            return when (char) {
                "\"" -> TOKEN_STRING
                "," -> {
                    read()
                    TOKEN_COMMA
                }
                "-", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> TOKEN_NUMBER
                ":" -> TOKEN_COLON
                "[" -> TOKEN_SQUARED_OPEN
                "]" -> {
                    read()
                    TOKEN_SQUARED_CLOSE
                }
                "{" -> TOKEN_CURLY_OPEN
                "}" -> {
                    read()
                    TOKEN_CURLY_CLOSE
                }
                else -> {
                    val word = nextWord
                    when (word) {
                        "false" -> TOKEN_FALSE
                        "true" -> TOKEN_TRUE
                        "null" -> TOKEN_NULL
                        else -> TOKEN_NONE
                    }
                }
            }
        }
}

