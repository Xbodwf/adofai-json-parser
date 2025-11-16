# ADOFAI JSON Parser

A lightweight, pure Kotlin JSON parser library for parsing and stringifying JSON. This library is designed for ADOFAI levels and provides a simple API similar to JavaScript's `JSON.parse()` and `JSON.stringify()`.

## Features

- ✅ **Pure Kotlin Implementation** - No external dependencies
- ✅ **Multiplatform Support** - Works on JVM and Kotlin/Native (Windows, macOS, Linux)
- ✅ **Simple API** - Similar to JavaScript's JSON API
- ✅ **Reviver/Replacer Support** - Transform values during parsing/stringifying
- ✅ **Pretty Printing** - Format JSON with indentation
- ✅ **Full JSON Support** - Objects, arrays, strings, numbers, booleans, null
- ✅ **Loose parsing** - Support JSON with comma, newline string

## Supported Platforms

- **JVM** - Java Virtual Machine
- **Kotlin/Native**:
  - Windows (mingwX64)
  - macOS (macosX64, macosArm64)
  - Linux (linuxX64, linuxArm64)

## Installation

### Gradle (Kotlin DSL)

Add the dependency to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    // Add your repository here
}

dependencies {
    implementation("com.fizzd:jsonparser:1.0-SNAPSHOT")
}
```

### Building from Source

```bash
# Clone the repository
git clone <repository-url>
cd adofai-json-parser

# Build the project
./gradlew build

# The JAR file will be in build/libs/
# - ADOFAIJSONParser-jvm-1.0-SNAPSHOT.jar (JVM)
# - ADOFAIJSONParser-metadata-1.0-SNAPSHOT.jar (Metadata)
```

## Usage

### Basic Parsing

```kotlin
import com.fizzd.jsonparser.StringParser

val parser = StringParser()

// Parse JSON string
val jsonString = """{"name": "John", "age": 30, "city": "New York"}"""
val result = parser.parse(jsonString) as? Map<String, Any?>

println(result?.get("name")) // Output: John
```

### Parsing Arrays

```kotlin
val jsonArray = """[1, 2, 3, "hello", true, null]"""
val array = parser.parse(jsonArray) as? List<Any?>

array?.forEach { println(it) }
```

### Using Reviver

The `reviver` function allows you to transform values during parsing:

```kotlin
val json = """{"date": "2024-01-01", "value": "123"}"""

val result = parser.parse(json) { key, value ->
    when {
        key == "date" && value is String -> {
            // Transform date string to Date object
            java.time.LocalDate.parse(value)
        }
        key == "value" && value is String -> {
            // Transform string to number
            value.toIntOrNull() ?: value
        }
        else -> value
    }
} as? Map<String, Any?>
```

### Stringifying

```kotlin
val data = mapOf(
    "name" to "John",
    "age" to 30,
    "active" to true,
    "tags" to listOf("developer", "kotlin")
)

// Basic stringify
val json = parser.stringify(data)
println(json)
// Output: {"name":"John","age":30,"active":true,"tags":["developer","kotlin"]}
```

### Pretty Printing

```kotlin
// Pretty print with 2 spaces
val prettyJson = parser.stringify(data, space = 2)
println(prettyJson)
// Output:
// {
//   "name": "John",
//   "age": 30,
//   "active": true,
//   "tags": [
//     "developer",
//     "kotlin"
//   ]
// }

// Pretty print with custom indentation
val customIndent = parser.stringify(data, space = "\t")
```

### Using Replacer Function

Transform values during stringifying:

```kotlin
val data = mapOf(
    "name" to "John",
    "password" to "secret123",
    "age" to 30
)

val json = parser.stringify(data) { key, value ->
    when {
        key == "password" -> "***" // Hide password
        value is Number -> value.toDouble() // Convert to double
        else -> value
    }
}
```

### Using Replacer Array

Only include specified keys:

```kotlin
val data = mapOf(
    "name" to "John",
    "age" to 30,
    "email" to "john@example.com",
    "password" to "secret"
)

// Only include "name" and "age"
val json = parser.stringify(data, replacer = listOf("name", "age"))
// Output: {"name":"John","age":30}
```

## API Reference

### StringParser

Main parser class for JSON operations.

#### Methods

##### `parse(text: String?, reviver: ((String, Any?) -> Any?)? = null): Any?`

Parses a JSON string and returns the corresponding Kotlin object.

- **Parameters:**
  - `text`: JSON string to parse (nullable)
  - `reviver`: Optional function to transform values during parsing
- **Returns:** Parsed object (`Map<String, Any?>`, `List<Any?>`, or primitive type)
- **Returns:** `null` if input is `null` or parsing fails

##### `stringify(value: Any?, replacer: ((String, Any?) -> Any?)? = null, space: Any? = null): String`

Converts a Kotlin object to a JSON string.

- **Parameters:**
  - `value`: Object to stringify
  - `replacer`: Optional function to transform values during stringifying
  - `space`: Indentation (number of spaces 0-10, or string up to 10 characters)
- **Returns:** JSON string representation

##### `stringify(value: Any?, replacer: List<String>?, space: Any? = null): String`

Converts a Kotlin object to a JSON string with key filtering.

- **Parameters:**
  - `value`: Object to stringify
  - `replacer`: List of keys to include in output
  - `space`: Indentation (number of spaces 0-10, or string up to 10 characters)
- **Returns:** JSON string representation

### ParserX

Internal parser implementation. Exported for advanced usage.

#### Constants

- `TOKEN_NONE = 0`
- `TOKEN_CURLY_OPEN = 1`
- `TOKEN_CURLY_CLOSE = 2`
- `TOKEN_SQUARED_OPEN = 3`
- `TOKEN_SQUARED_CLOSE = 4`
- `TOKEN_COLON = 5`
- `TOKEN_COMMA = 6`
- `TOKEN_STRING = 7`
- `TOKEN_NUMBER = 8`
- `TOKEN_TRUE = 9`
- `TOKEN_FALSE = 10`
- `TOKEN_NULL = 11`

### Serializer

Internal serializer implementation. Exported for advanced usage.

## Project Structure

```
src/
├── commonMain/
│   └── kotlin/
│       └── com/
│           └── fizzd/
│               └── jsonparser/
│                   ├── StringParser.kt  # Main API
│                   ├── ParserX.kt        # JSON Parser
│                   └── Serializer.kt     # JSON Serializer
├── jvmMain/          # JVM-specific code (if needed)
└── nativeMain/       # Native-specific code (if needed)
```

## Type Mapping

| JSON Type | Kotlin Type |
|-----------|-------------|
| `object`  | `Map<String, Any?>` |
| `array`   | `List<Any?>` |
| `string`  | `String` |
| `number`  | `Int` or `Double` |
| `boolean` | `Boolean` |
| `null`    | `null` |

## Examples

### Complete Example

```kotlin
import com.fizzd.jsonparser.StringParser

fun main() {
    val parser = StringParser()
    
    // Parse JSON
    val json = """
    {
        "users": [
            {"name": "Alice", "age": 25},
            {"name": "Bob", "age": 30}
        ],
        "total": 2
    }
    """
    
    val data = parser.parse(json) as? Map<String, Any?>
    val users = data?.get("users") as? List<*>
    
    users?.forEach { user ->
        val userMap = user as? Map<String, Any?>
        println("${userMap?.get("name")} is ${userMap?.get("age")} years old")
    }
    
    // Stringify back to JSON
    val backToJson = parser.stringify(data, space = 2)
    println(backToJson)
}
```

## Notes

> [!Note]
> Since this project was split from [ADOFAI-JS](https://github.com/Xbodwf/ADOFAI-JS), this project may not receive code submissions before the original library is updated.

## License

MIT

## Contributing

[Yqloss](https://github.com/Yqloss)
