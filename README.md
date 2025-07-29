# Flat2Java

A Java library for converting positional plain-text (fixed-length fields) to Java objects, similar to COBOL's data structures.

## Description

This library provides functionality to parse fixed-length text fields and convert them into Java objects. It's particularly useful when working with legacy systems or flat file formats that use fixed-length field definitions.

## Features

- Parse fixed-length text fields into Java objects
- Support for different data types (String, Integer, Date, etc.)
- Annotation-based field mapping
- Customizable field definitions
- Easy to use API

## Usage

```java
public class MyRecord {
    @FlatField(length = 10)
    private String id;

    @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
    private LocalDate date;

    @FlatField(length = 10, precision = 2)
    private BigDecimal amount;
}

// Extract field metadata in declaration order
List<FieldMetadata> metadata = FieldMetadataExtractor.extract(MyRecord.class);
```

**Note:** The order of fields is determined by their declaration order in the class (and superclasses, if using inheritance).

## @FlatField Annotation Attributes

The `@FlatField` annotation is used to mark fields that should be mapped from flat file data. Here are all available attributes:

### Required Attributes

- **`length`** (int): The length of the field in the flat file record. This is the only required attribute.

### Optional Attributes

- **`name`** (String): The name of the field. Defaults to an empty string if not specified.

- **`precision`** (int): The precision for decimal numbers (number of decimal places). Only used for `BigDecimal` fields. Defaults to `0`.

- **`dateTimeFormat`** (String): The date format pattern to use when parsing date fields. Uses the standard Java date format patterns. Required for `LocalDate` and `LocalDateTime` fields. Examples:
  - `"yyyyMMdd"` for dates like "20240315"
  - `"yyyyMMddHHmmss"` for date-times like "20240315143045"
  - `"ddMMyyyy"` for dates like "15032024"

- **`padding`** (char): Optional padding character used in the field. Defaults to space (`' '`). This character is used to pad the field to its specified length.

### Examples

```java
public class ExampleRecord {
    // Basic string field
    @FlatField(length = 10)
    private String id;

    // String field with custom padding
    @FlatField(length = 8, padding = '*')
    private String code;

    // Integer field (uses numeric padding from environment variable)
    @FlatField(length = 5)
    private Integer quantity;

    // BigDecimal field with precision
    @FlatField(length = 10, precision = 2)
    private BigDecimal price;

    // Date field with format
    @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
    private LocalDate date;

    // DateTime field with format
    @FlatField(length = 14, dateTimeFormat = "yyyyMMddHHmmss")
    private LocalDateTime timestamp;

    // Long field with custom padding
    @FlatField(length = 12, padding = '0')
    private Long accountNumber;
}
```

## Requirements

- Java 11 or higher
- Maven 3.6 or higher
- SLF4J with Logback for logging
- JUnit 5 for testing

## Installation

Add the following dependency to your project:

```xml
<dependency>
    <groupId>com.github.allan</groupId>
    <artifactId>flat2java</artifactId>
    <version>0.0.1-2</version>
</dependency>
```

## Environment Variables

The library supports configuration through environment variables to customize its behavior:

### FLAT2JAVA_ALLOW_NULL_VALUES
Controls whether null field values are allowed during serialization.

- **Default:** `true`
- **Values:** `true` or `false`
- **Description:** When set to `true`, null field values are serialized as empty strings with appropriate padding. When set to `false`, a `NullPointerException` is thrown for null field values.

### FLAT2JAVA_DEFAULT_NUMERIC_PADDING
Sets the default padding character for numeric fields when the `@FlatField` annotation doesn't specify a custom padding.

- **Default:** `0`
- **Values:** Any single character (e.g., `0`, ` `, `*`)
- **Description:** This character is used to pad numeric fields to their specified length. If you want numeric fields to be padded with spaces instead of zeros, set this variable to a space character. Note that if a field explicitly specifies a padding character in the `@FlatField` annotation, that value takes precedence over this environment variable.

**Example:**
```bash
# Use spaces for numeric padding
export FLAT2JAVA_DEFAULT_NUMERIC_PADDING=" "

# Use asterisks for numeric padding  
export FLAT2JAVA_DEFAULT_NUMERIC_PADDING="*"
```

### FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR
Controls whether BigDecimal fields should use decimal separators (like ".") during serialization.

- **Default:** `false`
- **Values:** `true` or `false`
- **Description:** When set to `false` (default), BigDecimal values are serialized without decimal separators. For example, a value of `12.34` with precision `2` becomes `"1234"`. When set to `true`, the decimal separator is included, so `12.34` with precision `2` becomes `"12.34"`.

**Example:**
```bash
# Use decimal separators (traditional format)
export FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR="true"

# Don't use decimal separators (compact format)
export FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR="false"
```

### FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR
Sets the decimal separator character for BigDecimal fields when decimal separators are enabled.

- **Default:** `.`
- **Values:** Any single character (e.g., `.`, `,`, `;`)
- **Description:** This character is used as the decimal separator when `FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR` is set to `true`. The library will replace the standard decimal point (`.`) with this custom character during serialization.

**Example:**
```bash
# Use comma as decimal separator
export FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR=","

# Use semicolon as decimal separator
export FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR=";"

# Use space as decimal separator
export FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR=" "
```

**Usage Examples:**
```java
// With FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR=true and FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR=","
@FlatField(length = 8, precision = 2)
private BigDecimal amount; // 12.34 → "12,34"

// With FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR=true and FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR=";"
@FlatField(length = 8, precision = 2)
private BigDecimal amount; // 12.34 → "12;34"
```

## Annotation Processor (Optional)

The project also provides an optional, however recommended, annotation processor (`flat2java-processor`) for compile-time verifications. If you want to enable additional compile-time checks, see the [flat2java-processor README](flat2java-processor/README.md).

## License

This project is licensed under the MIT License - see the LICENSE file for details. 