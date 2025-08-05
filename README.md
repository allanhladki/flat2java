# Flat2Java

A Java library for serializing Java objects to fixed-length flat file formats, similar to COBOL's data structures.

## Description

This library provides functionality to serialize Java objects into fixed-length text fields for flat file formats. It's particularly useful when working with legacy systems or flat file formats that use fixed-length field definitions.

**Note**: Currently, this library supports **serialization only** (Java objects → flat file strings). Deserialization support (flat file strings → Java objects) is planned for future releases.

## Features

- Serialize Java objects to fixed-length text fields
- Support for multiple data types (String, Integer, Long, BigInteger, BigDecimal, LocalDate, LocalDateTime)
- Annotation-based field mapping with `@FlatField`
- Customizable field definitions (length, precision, padding, date formats)
- Environment-based configuration
- Comprehensive validation and error handling
- Compile-time validation with annotation processor
- Interface-based architecture with dependency injection support
- Automatic exception translation with interceptors
- Easy to use API with both interface and static utility approaches

## Quick Start

### Interface-Based Approach (Recommended)

```java
import com.github.allanhladki.flat2java.factory.Flat2JavaFactory;
import com.github.allanhladki.flat2java.interfaces.ObjectSerializationInterface;

public class MyRecord {
    @FlatField(length = 10)
    private String id;

    @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
    private LocalDate date;

    @FlatField(length = 10, precision = 2)
    private BigDecimal amount;
}


**Note:** The order of fields is determined by their declaration order in the class (and superclasses, if using inheritance).

// Create factory and get intercepted services
Flat2JavaFactory factory = new Flat2JavaFactory();
ObjectSerializationInterface objectSerializer = factory.getObjectSerializationInterface();

// Serialize an object with automatic exception translation
MyRecord record = new MyRecord();
record.setId("1234567890");
record.setDate(LocalDate.of(2024, 3, 15));
record.setAmount(new BigDecimal("99.99"));

String flatFileString = objectSerializer.serialize(record);
// Result: "1234567890 2024031500000009999"
```

### Static Utility Approach (Legacy)

```java
import com.github.allanhladki.flat2java.serializer.ObjectSerializer;

// Same MyRecord class as above...

// Serialize an object using static methods
MyRecord record = new MyRecord();
record.setId("1234567890");
record.setDate(LocalDate.of(2024, 3, 15));
record.setAmount(new BigDecimal("99.99"));

String flatFileString = ObjectSerializer.serialize(record);
// Result: "1234567890 2024031500000009999"
```


## Architecture

### Interface-Based Architecture

The library provides an interface-based architecture that supports dependency injection and automatic exception translation:

#### Core Interfaces

- **`FieldSerializationInterface`** - Serializes individual field values
- **`ObjectSerializationInterface`** - Serializes entire objects
- **`FieldMetadataInterface`** - Extracts field metadata from classes

#### Factory Pattern

The `Flat2JavaFactory` creates properly configured service instances with automatic exception translation:

```java
Flat2JavaFactory factory = new Flat2JavaFactory();

// Get intercepted services with automatic exception translation
FieldSerializationInterface fieldSerializer = factory.getFieldSerializationInterface();
ObjectSerializationInterface objectSerializer = factory.getObjectSerializationInterface();
FieldMetadataInterface metadataExtractor = factory.getFieldMetadataInterface();
```

#### Exception Translation

All services automatically translate standard Java exceptions to project-specific exceptions:

- `NullPointerException` → `SerializationException`
- `IllegalArgumentException` → `ValidationException`
- Other exceptions → `SerializationException` or `ValidationException` based on context

#### Benefits

- **Clean separation of concerns** - interfaces vs implementations
- **Dependency injection ready** - easy to mock for testing
- **Automatic exception translation** - consistent error handling
- **Extensible design** - new implementations can be added
- **Backward compatibility** - static utility methods still available
- **Dependency injection ready** - works with Jakarta CDI, Spring, Quarkus, etc.

## Dependency Injection Examples

### Jakarta CDI (Quarkus, WildFly, etc.)

```java
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.github.allanhladki.flat2java.interfaces.ObjectSerializationInterface;

@ApplicationScoped
public class FlatFileService {
    
    @Inject
    private ObjectSerializationInterface objectSerializer;
    
    public String serializeRecord(MyRecord record) {
        return objectSerializer.serialize(record);
    }
}
```

### Spring Framework

```java
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.allanhladki.flat2java.interfaces.ObjectSerializationInterface;

@Service
public class FlatFileService {
    
    @Autowired
    private ObjectSerializationInterface objectSerializer;
    
    public String serializeRecord(MyRecord record) {
        return objectSerializer.serialize(record);
    }
}
```

### Manual Configuration (No DI Framework)

```java
import com.github.allanhladki.flat2java.factory.Flat2JavaFactory;
import com.github.allanhladki.flat2java.interfaces.ObjectSerializationInterface;

public class FlatFileService {
    
    private final ObjectSerializationInterface objectSerializer;
    
    public FlatFileService() {
        Flat2JavaFactory factory = new Flat2JavaFactory();
        this.objectSerializer = factory.getObjectSerializationInterface();
    }
    
    public String serializeRecord(MyRecord record) {
        return objectSerializer.serialize(record);
    }
}
```

### Custom Bean Configuration (Spring Boot)

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.allanhladki.flat2java.factory.Flat2JavaFactory;
import com.github.allanhladki.flat2java.interfaces.ObjectSerializationInterface;

@Configuration
public class Flat2JavaConfig {
    
    @Bean
    public ObjectSerializationInterface objectSerializationInterface() {
        Flat2JavaFactory factory = new Flat2JavaFactory();
        return factory.getObjectSerializationInterface();
    }
    
    @Bean
    public FieldSerializationInterface fieldSerializationInterface() {
        Flat2JavaFactory factory = new Flat2JavaFactory();
        return factory.getFieldSerializationInterface();
    }
    
    @Bean
    public FieldMetadataInterface fieldMetadataInterface() {
        Flat2JavaFactory factory = new Flat2JavaFactory();
        return factory.getFieldMetadataInterface();
    }
}
```

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
- SLF4J with Logback for logging (runtime dependency)
- JUnit 5 for testing (test dependency)

## Installation

Add the following dependency to your project:

```xml
<dependency>
    <groupId>com.github.allanhladki</groupId>
    <artifactId>flat2java-core</artifactId>
    <version>0.0.1-2</version>
</dependency>
```

### Optional: Annotation Processor

For compile-time validation, add the annotation processor:

```xml
<dependency>
    <groupId>com.github.allanhladki</groupId>
    <artifactId>flat2java-processor</artifactId>
    <version>0.0.1-2</version>
    <scope>provided</scope>
</dependency>
```

## Error Handling

The library provides comprehensive error handling with custom exceptions:

- `Flat2JavaException`: Base exception for all library errors
- `SerializationException`: Thrown during serialization failures
- `ValidationException`: Thrown during validation failures

All exceptions include detailed error messages to help diagnose issues.

## Limitations

- **Serialization Only**: Currently supports only serialization (Java objects → flat file strings)
- **Fixed Field Types**: Limited to supported data types (see @FlatField documentation)
- **No File I/O**: Library handles string conversion only, not file operations
- **No Deserialization**: Cannot read flat files back into Java objects (planned for future release)

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

### FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE
Sets the rounding mode for BigDecimal operations when precision scaling is required.

- **Default:** `UNNECESSARY`
- **Values:** Any valid Java `RoundingMode` enum value (e.g., `HALF_UP`, `HALF_DOWN`, `HALF_EVEN`, `UP`, `DOWN`, `CEILING`, `FLOOR`, `UNNECESSARY`)
- **Description:** This setting controls how BigDecimal values are rounded when they need to be scaled to a specific precision. The rounding mode determines the behavior when a value cannot be represented exactly with the specified precision.

**Important Note:** Rounding is not encouraged in fixed-length string formats since they work with a fixed number of decimals. It is recommended to perform rounding in the application that uses this library before passing values to the serializer. The default `UNNECESSARY` mode will throw an exception if rounding would be required, ensuring data integrity and preventing unexpected precision loss.

**Available Rounding Modes:**
- `HALF_UP`: Round towards the nearest neighbor, ties round up (default)
- `HALF_DOWN`: Round towards the nearest neighbor, ties round down
- `HALF_EVEN`: Round towards the nearest neighbor, ties round to even neighbor
- `UP`: Round towards positive infinity
- `DOWN`: Round towards zero
- `CEILING`: Round towards positive infinity
- `FLOOR`: Round towards negative infinity
- `UNNECESSARY`: No rounding needed (throws exception if rounding would be necessary)

**Example:**
```bash
# Use HALF_DOWN rounding (ties round down)
export FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE="HALF_DOWN"

# Use HALF_EVEN rounding (Banker's rounding)
export FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE="HALF_EVEN"

# Use UP rounding (always round up)
export FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE="UP"
```

**Usage Examples:**
```java
// With FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE="UNNECESSARY" (default)
@FlatField(length = 8, precision = 2)
private BigDecimal amount; // 12.34 → "12.34" (exact precision, no rounding)

// With FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE="HALF_UP"
@FlatField(length = 8, precision = 2)
private BigDecimal amount; // 12.345 → "12.35"

// With FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE="HALF_DOWN"
@FlatField(length = 8, precision = 2)
private BigDecimal amount; // 12.345 → "12.34"

// With FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE="HALF_EVEN"
@FlatField(length = 8, precision = 2)
private BigDecimal amount; // 12.345 → "12.34", 12.335 → "12.34"
```

## Troubleshooting

### Common Issues

1. **"Field length must be positive"**: Ensure all `@FlatField` annotations have a positive `length` value
2. **"Precision must be smaller than length"**: For BigDecimal fields, precision should be less than the total field length
3. **"Invalid dateTimeFormat"**: Check that date format patterns are valid Java date format strings
4. **"Unsupported field type"**: Only supported types can be serialized (String, Integer, Long, BigInteger, BigDecimal, LocalDate, LocalDateTime)
5. **"Rounding necessary"**: With UNNECESSARY rounding mode, ensure BigDecimal values match the specified precision exactly

### Performance Tips

- Use the annotation processor for compile-time validation
- Consider caching serialized results for frequently used objects
- For large datasets, process records in batches

## Annotation Processor (Optional)

The project also provides an optional, however recommended, annotation processor (`flat2java-processor`) for compile-time verifications. If you want to enable additional compile-time checks, see the [flat2java-processor README](flat2java-processor/README.md).

## Contributing

Contributions are welcome! Please ensure all tests pass before submitting a pull request.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 