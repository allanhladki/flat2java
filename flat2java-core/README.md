# Flat2Java Core

The core library for serializing Java objects to fixed-length flat file formats.

## Description

This module contains the main functionality for serializing Java objects into fixed-length text fields for flat file formats. It provides the core API, annotations, serialization logic, and configuration management.

## Features

- **Serialization Engine**: Convert Java objects to fixed-length flat file strings
- **Annotation Support**: `@FlatField` annotation for declarative field mapping
- **Type Support**: String, Integer, Long, BigInteger, BigDecimal, LocalDate, LocalDateTime
- **Configuration**: Environment-based configuration with validation
- **Validation**: Comprehensive field and metadata validation
- **Error Handling**: Custom exception hierarchy with detailed error messages
- **Logging**: Comprehensive logging with SLF4J

## Core Components

### Annotations
- `@FlatField`: Main annotation for field definition and configuration

### Metadata
- `FieldMetadata`: Encapsulates field configuration and metadata
- `FieldMetadataExtractor`: Extracts and validates field metadata from classes

### Serialization
- `ObjectSerializer`: Main API for serializing objects to flat file strings
- `FieldSerializer`: Type-specific serialization logic
- `SerializationTypes`: Type management and validation utilities

### Configuration
- `Flat2JavaConfig`: Environment-based configuration management

### Exceptions
- `Flat2JavaException`: Base exception for all library errors
- `SerializationException`: Serialization-specific errors
- `ValidationException`: Validation-specific errors

## Usage

```java
import com.github.allanhladki.flat2java.markers.FlatField;
import com.github.allanhladki.flat2java.serializer.ObjectSerializer;

public class MyRecord {
    @FlatField(length = 10)
    private String id;

    @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
    private LocalDate date;

    @FlatField(length = 10, precision = 2)
    private BigDecimal amount;
    
    // getters and setters...
}

// Serialize an object
MyRecord record = new MyRecord();
record.setId("1234567890");
record.setDate(LocalDate.of(2024, 3, 15));
record.setAmount(new BigDecimal("99.99"));

String flatFileString = ObjectSerializer.serialize(record);
// Result: "1234567890 2024031500000009999"
```

## Configuration

The library uses environment variables for configuration:

- `FLAT2JAVA_ALLOW_NULL_STRINGS`: Control null string value handling
- `FLAT2JAVA_DEFAULT_NUMERIC_PADDING`: Set default padding for numeric fields
- `FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR`: Enable/disable decimal separators
- `FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR`: Set custom decimal separator
- `FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE`: Configure rounding behavior

## Error Handling

The library provides comprehensive error handling:

```java
try {
    String result = ObjectSerializer.serialize(myObject);
} catch (SerializationException e) {
    // Handle serialization errors
} catch (ValidationException e) {
    // Handle validation errors
} catch (Flat2JavaException e) {
    // Handle other library errors
}
```

## Requirements

- Java 11 or higher
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

## Testing

The module includes comprehensive unit tests covering:
- Field serialization for all supported types
- Configuration validation
- Error handling scenarios
- Edge cases and boundary conditions

Run tests with:
```bash
mvn test
```

## License

This project is licensed under the MIT License - see the LICENSE file for details. 