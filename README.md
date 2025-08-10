# Flat2Java

A Java library for serializing and deserializing Java objects to/from fixed-length flat file formats, similar to COBOL's data structures.

## Description

This library provides functionality to serialize Java objects into fixed-length text fields for flat file formats and deserialize them back to Java objects. It's particularly useful when working with legacy systems or flat file formats that use fixed-length field definitions.

## Features

- Serialize Java objects to fixed-length text fields
- Deserialize flat file strings back to Java objects
- Support for multiple data types (String, Integer, Long, BigInteger, BigDecimal, LocalDate, LocalDateTime)
- Annotation-based field mapping with `@FlatField`
- Customizable field definitions (length, precision, padding, date formats)
- Environment-based configuration
- Comprehensive validation and error handling
- Compile-time validation with annotation processor
- Interface-based architecture with dependency injection support
- Automatic exception translation with interceptors
- Easy to use API with both interface and static utility approaches
- Consistent padding handling between serialization and deserialization
- Support for inheritance in object deserialization

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

// Deserialize back to object
MyRecord deserializedRecord = objectSerializer.deserialize(flatFileString, MyRecord.class);
// deserializedRecord now contains the same data as the original record
```

## Deserialization Features

### Basic Deserialization

The library can deserialize flat file strings back to Java objects:

```java
import com.github.allanhladki.flat2java.deserializer.ObjectDeserializer;

// Deserialize a flat string to a Java object
String flatString = "1234567890 2024031500000009999";
MyRecord record = ObjectDeserializer.deserialize(flatString, MyRecord.class);

// The record now contains:
// record.getId() = "1234567890"
// record.getDate() = LocalDate.of(2024, 3, 15)
// record.getAmount() = new BigDecimal("99.99")
```

### Inheritance Support

When deserializing objects with inheritance, you can control whether to include superclass fields:

```java
public class SuperRecord {
    @FlatField(length = 5)
    private String superField1;
    
    @FlatField(length = 5)
    private String superField2;
}

public class SubRecord extends SuperRecord {
    @FlatField(length = 5)
    private String subField1;
    
    @FlatField(length = 5)
    private String subField2;
}

// Deserialize with inheritance (includes superclass fields)
String flatWithInheritance = "supe1supe2sub1 sub2 ";
SubRecord record = ObjectDeserializer.deserialize(flatWithInheritance, SubRecord.class, true);

// Deserialize without inheritance (only subclass fields)
String flatWithoutInheritance = "sub1 sub2 ";
SubRecord record = ObjectDeserializer.deserialize(flatWithoutInheritance, SubRecord.class, false);
```

### Null String Value Handling

The library handles null string values consistently between serialization and deserialization:

- **String fields**: Null values are represented as padding characters and deserialized back to `null`
- **Numeric fields**: Cannot be null - must contain valid values
- **Date/Time fields**: Cannot be null - must contain valid values
- **Primitive types** (int, long, etc.): Attempting to deserialize null values will throw a `NullPointerException`

### Padding Consistency

The library ensures consistent padding behavior:
- Numeric fields always use zero padding (`'0'`) for consistency and reliability
- String fields use the configured null value padding (default: space)
- Custom padding specified in `@FlatField` annotations takes precedence over configuration

## Round-Trip Operations

The library supports complete round-trip operations, allowing you to serialize an object to a flat string and then deserialize it back to an identical object:

```java
import com.github.allanhladki.flat2java.serializer.ObjectSerializer;
import com.github.allanhladki.flat2java.deserializer.ObjectDeserializer;

public class RoundTripExample {
    public static void main(String[] args) {
        // Create a record
        MyRecord original = new MyRecord();
        original.setId("TEST123");
        original.setDate(LocalDate.of(2024, 3, 15));
        original.setAmount(new BigDecimal("99.99"));
        original.setQuantity(42);
        
        // Serialize to flat string
        String flatString = ObjectSerializer.serialize(original);
        System.out.println("Serialized: " + flatString);
        
        // Deserialize back to object
        MyRecord deserialized = ObjectDeserializer.deserialize(flatString, MyRecord.class);
        
        // Verify round-trip integrity
        assertEquals(original.getId(), deserialized.getId());
        assertEquals(original.getDate(), deserialized.getDate());
        assertEquals(original.getAmount(), deserialized.getAmount());
        assertEquals(original.getQuantity(), deserialized.getQuantity());
        
        System.out.println("Round-trip successful!");
    }
}
```

### Round-Trip Best Practices

1. **Consistent Padding**: Ensure that your `@FlatField` annotations use consistent padding characters, or rely on the library's default padding configuration.

2. **Field Length Validation**: The library automatically validates that serialized strings match the expected length for deserialization.

3. **Null Value Handling**: Be aware that primitive types cannot be null, so use wrapper types (Integer, Long, BigDecimal) if you need to represent missing values.

4. **Inheritance Considerations**: When using inheritance, ensure that the `inherited` parameter is consistent between serialization and deserialization operations.

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

- **`padding`** (char): Optional padding character used in the field. Defaults to space (`' '`). This character is used to pad the field to its specified length. **Note:** For numeric fields, this setting is ignored - numeric fields are always padded with zeros (`'0'`) for consistency.

### Examples

```java
public class ExampleRecord {
    // Basic string field
    @FlatField(length = 10)
    private String id;

    // String field with custom padding
    @FlatField(length = 8, padding = '*')
    private String code;

    // Integer field (always uses zero padding, regardless of annotation)
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

- **Fixed Field Types**: Limited to supported data types (see @FlatField documentation)
- **No File I/O**: Library handles string conversion only, not file operations
- **Padding Requirements**: Fields must have consistent padding between serialization and deserialization for proper round-trip operations

## Environment Variables

The library supports configuration through environment variables to customize its behavior:

### FLAT2JAVA_ALLOW_NULL_STRINGS
Controls whether null string field values are allowed during serialization.

- **Default:** `true`
- **Values:** `true` or `false`
- **Description:** When set to `true`, null string field values are serialized as empty strings with appropriate padding. When set to `false`, a `NullPointerException` is thrown for null string field values. Note that numeric and date/time fields cannot be null regardless of this setting.


### FLAT2JAVA_NULL_VALUE_PADDING
Sets the default padding character for null values when the `@FlatField` annotation doesn't specify a custom padding.

- **Default:** ` ` (space)
- **Values:** Any single character (e.g., ` `, `0`, `*`)
- **Description:** This character is used to pad null values for non-numeric fields. It ensures consistency between serialization and deserialization of null values. Note that if a field explicitly specifies a padding character in the `@FlatField` annotation, that value takes precedence over this environment variable.

**Example:**
```bash
# Use spaces for null value padding (default)
export FLAT2JAVA_NULL_VALUE_PADDING=" "

# Use zeros for null value padding
export FLAT2JAVA_NULL_VALUE_PADDING="0"

# Use asterisks for null value padding
export FLAT2JAVA_NULL_VALUE_PADDING="*"
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

### FLAT2JAVA_THROW_ON_STRING_TRUNCATION
Controls whether an exception should be thrown when a string field's value exceeds its defined length.

- **Default:** `false`
- **Values:** `true` or `false`
- **Description:** When set to `false` (default), string values that exceed their field length are automatically truncated to fit. When set to `true`, an `IllegalArgumentException` is thrown when a string value is too long for its field, allowing you to handle data validation at the application level.

**Important Note:** Numeric fields (Integer, Long, BigInteger, BigDecimal) always throw exceptions when their values exceed the field length, regardless of this setting. This prevents data corruption that could occur from truncating numeric values.

**Example:**
```bash
# Throw exceptions when strings are truncated (strict validation)
export FLAT2JAVA_THROW_ON_STRING_TRUNCATION="true"

# Allow string truncation (default behavior)
export FLAT2JAVA_THROW_ON_STRING_TRUNCATION="false"
```

**Usage Examples:**
```java
@FlatField(length = 5)
private String shortField; // "Hello" → "Hello", "VeryLongString" → throws exception if enabled

// With FLAT2JAVA_THROW_ON_STRING_TRUNCATION=true
// "VeryLongString" → IllegalArgumentException: "Field 'shortField' value 'VeryLongString' (length 14) exceeds target length 5"

// With FLAT2JAVA_THROW_ON_STRING_TRUNCATION=false (default)
// "VeryLongString" → "VeryL" (truncated)

@FlatField(length = 3)
private Integer numericField; // 123 → "123", 1234 → always throws exception (numeric field)

// Numeric fields always throw exceptions regardless of FLAT2JAVA_THROW_ON_STRING_TRUNCATION setting
// 1234 → IllegalArgumentException: "Field 'numericField' value '1234' (length 4) exceeds target length 3. Numeric fields cannot be truncated to prevent data corruption."
```

## Troubleshooting

### Common Issues

1. **"Field length must be positive"**: Ensure all `@FlatField` annotations have a positive `length` value
2. **"Precision must be smaller than length"**: For BigDecimal fields, precision should be less than the total field length
3. **"Invalid dateTimeFormat"**: Check that date format patterns are valid Java date format strings
4. **"Unsupported field type"**: Only supported types can be serialized (String, Integer, Long, BigInteger, BigDecimal, LocalDate, LocalDateTime)
5. **"Rounding necessary"**: With UNNECESSARY rounding mode, ensure BigDecimal values match the specified precision exactly
6. **"Input length does not match expected total length"**: The flat string length must match the sum of all field lengths. Check your field definitions and ensure inheritance settings are correct
7. **"Field 'fieldName': invalid [type] 'value'"**: The field value cannot be parsed to the expected type. Check that the input string contains valid data for the field type
8. **"NullPointerException on primitive field"**: Primitive types cannot be null. Use wrapper types (Integer, Long, BigDecimal) if you need to represent missing values
9. **"Padding mismatch between serialization and deserialization"**: Ensure consistent padding configuration. Use `FLAT2JAVA_NULL_VALUE_PADDING` environment variable for null value padding. Note that numeric fields are always padded with zeros for consistency.

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