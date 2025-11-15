package com.fizzd.jsonparser

class ParserX(private val json: String, private val endSection: String? = null) {
    object TOKEN {
        const val NONE = 0
        const val CURLY_OPEN = 1
        const val CURLY_CLOSE = 2
        const val SQUARED_OPEN = 3
        const val SQUARED_CLOSE = 4
        const val COLON = 5
        const val COMMA = 6
        const val STRING = 7
        const val NUMBER = 8
        const val TRUE = 9
        const val FALSE = 10
        const val NULL = 11
    }

    companion object {
        const val WHITE_SPACE = " \t\n\r\uFEFF"
        const val WORD_BREAK = " \t\n\r{}[],:\""
    }

    private var position: Int = 0

    init {
        if (peek() == 0xfeff) {
            read()
        }
    }

    fun parseValue(): Any? {
        return parseByToken(nextToken())
    }

    fun parseObject(): MutableMap<String, Any?>? {
        val obj: MutableMap<String, Any?> = LinkedHashMap()
        read()
        while (true) {
            var nextToken: Int
            do {
                nextToken = nextToken()
                if (nextToken == TOKEN.NONE) return null
                if (nextToken == TOKEN.CURLY_CLOSE) return obj
            } while (nextToken == TOKEN.COMMA)
            val key = parseString() ?: return null
            if (nextToken() != TOKEN.COLON) return null
            if (endSection == null || key != endSection) {
                read()
                obj[key] = parseValue()
            } else {
                return obj
            }
        }
    }

    fun parseArray(): MutableList<Any?>? {
        val array = mutableListOf<Any?>()
        read()
        var parsing = true
        while (parsing) {
            when (val nextToken = nextToken()) {
                TOKEN.NONE -> return null
                TOKEN.SQUARED_CLOSE -> parsing = false
                TOKEN.COMMA -> {}
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
            TOKEN.CURLY_OPEN -> parseObject()
            TOKEN.SQUARED_OPEN -> parseArray()
            TOKEN.STRING -> parseString()
            TOKEN.NUMBER -> parseNumber()
            TOKEN.TRUE -> true
            TOKEN.FALSE -> false
            TOKEN.NULL -> null
            else -> null
        }
    }

    fun parseString(): String? {
        val sb = StringBuilder()
        read()
        var parsing = true
        while (parsing) {
            if (peek() == -1) break
            val ch = nextChar()
            when (ch) {
                '"' -> parsing = false
                '\\' -> {
                    if (peek() == -1) { parsing = false; break }
                    val escaped = nextChar()
                    when (escaped) {
                        '"', '/', '\\' -> sb.append(escaped)
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            var unicode = ""
                            repeat(4) { unicode += nextChar() }
                            sb.append(unicode.toInt(16).toChar())
                        }
                    }
                }
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun parseNumber(): Number {
        val word = nextWord()
        return if (!word.contains('.')) {
            word.toIntOrNull() ?: 0
        } else {
            word.toDoubleOrNull() ?: 0.0
        }
    }

    private fun eatWhitespace() {
        while (WHITE_SPACE.indexOf(peekChar()) != -1) {
            read()
            if (peek() == -1) break
        }
    }

    private fun peek(): Int {
        return if (position >= json.length) -1 else json[position].code
    }

    private fun read(): Int {
        return if (position >= json.length) -1 else json[position++].code
    }

    private fun peekChar(): Char {
        val code = peek()
        return if (code == -1) '\u0000' else code.toChar()
    }

    private fun nextChar(): Char {
        val code = read()
        return if (code == -1) '\u0000' else code.toChar()
    }

    private fun nextWord(): String {
        val sb = StringBuilder()
        while (WORD_BREAK.indexOf(peekChar()) == -1) {
            sb.append(nextChar())
            if (peek() == -1) break
        }
        return sb.toString()
    }

    fun nextToken(): Int {
        eatWhitespace()
        if (peek() == -1) return TOKEN.NONE
        return when (val ch = peekChar()) {
            '"' -> TOKEN.STRING
            ',' -> { read(); TOKEN.COMMA }
            '-' , '0','1','2','3','4','5','6','7','8','9' -> TOKEN.NUMBER
            ':' -> TOKEN.COLON
            '[' -> TOKEN.SQUARED_OPEN
            ']' -> { read(); TOKEN.SQUARED_CLOSE }
            '{' -> TOKEN.CURLY_OPEN
            '}' -> { read(); TOKEN.CURLY_CLOSE }
            else -> {
                when (val word = nextWord()) {
                    "false" -> TOKEN.FALSE
                    "true" -> TOKEN.TRUE
                    "null" -> TOKEN.NULL
                    else -> TOKEN.NONE
                }
            }
        }
    }
}