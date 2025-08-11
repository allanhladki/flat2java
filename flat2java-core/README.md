# Flat2Java Core

The core library for serializing and deserializing Java objects to/from fixed-length flat file formats.

## Description

This module contains the main functionality for serializing Java objects into fixed-length text fields for flat file formats and deserializing them back to Java objects. It provides the core API, annotations, serialization/deserialization logic, and configuration management.

## ✨ Features

- **Complete Serialization Engine**: Convert Java objects to fixed-length flat file strings
- **Full Deserialization Support**: Convert flat file strings back to Java objects
- **Round-Trip Operations**: Reliable serialization and deserialization with data integrity
- **Annotation Support**: `@FlatField` annotation for declarative field mapping
- **Comprehensive Type Support**: String, Integer, Long, BigInteger, BigDecimal, LocalDate, LocalDateTime
- **Flexible Configuration**: Environment-based configuration with validation
- **Robust Validation**: Compile-time and runtime validation with detailed error messages
- **Error Handling**: Custom exception hierarchy with detailed error messages
- **Interface-Based Architecture**: Clean interfaces with dependency injection support
- **Inheritance Support**: Handle object inheritance in serialization/deserialization
- **Padding Consistency**: Reliable padding handling between operations

## 🏗️ Core Components

### Annotations
- `@FlatField`: Main annotation for field definition and configuration

### Metadata
- `FieldMetadata`: Encapsulates field configuration and metadata
- `FieldMetadataExtractor`: Extracts and validates field metadata from classes

### Serialization
- `ObjectSerializer`: Main API for serializing objects to flat file strings
- `FieldSerializer`: Type-specific serialization logic
- `SerializationTypes`: Type management and validation utilities

### Deserialization
- `ObjectDeserializer`: Main API for deserializing flat file strings to objects
- `FieldDeserializer`: Type-specific deserialization logic

### Configuration
- `Flat2JavaConfig`: Environment-based configuration management

### Exceptions
- `Flat2JavaException`: Base exception for all library errors
- `SerializationException`: Serialization-specific errors
- `ValidationException`: Validation-specific errors
- `DeserializationException`: Deserialization-specific errors

## 🚀 Usage

### Basic Serialization

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

### Basic Deserialization

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

### Round-Trip Operations

```java
// Create and populate a record
MyRecord original = new MyRecord();
original.setId("TEST123");
original.setDate(LocalDate.of(2024, 3, 15));
original.setAmount(new BigDecimal("99.99"));

// Serialize to flat string
String flatString = ObjectSerializer.serialize(original);

// Deserialize back to object
MyRecord deserialized = ObjectDeserializer.deserialize(flatString, MyRecord.class);

// Verify round-trip integrity
assertEquals(original.getId(), deserialized.getId());
assertEquals(original.getDate(), deserialized.getDate());
assertEquals(original.getAmount(), deserialized.getAmount());
```

### Inheritance Support

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

## ⚙️ Configuration

The library uses environment variables for configuration:

- `FLAT2JAVA_ALLOW_NULL_STRINGS`: Control null string value handling
- `FLAT2JAVA_NULL_VALUE_PADDING`: Set default padding for null values
- `FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR`: Enable/disable decimal separators
- `FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR`: Set custom decimal separator
- `FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE`: Configure rounding behavior
- `FLAT2JAVA_THROW_ON_STRING_TRUNCATION`: Control string truncation behavior

## 🔧 Error Handling

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

try {
    MyObject result = ObjectDeserializer.deserialize(flatString, MyObject.class);
} catch (DeserializationException e) {
    // Handle deserialization errors
} catch (ValidationException e) {
    // Handle validation errors
} catch (Flat2JavaException e) {
    // Handle other library errors
}
```

## 📋 Requirements

- Java 11 or higher
- SLF4J with Logback for logging (runtime dependency)
- JUnit 5 for testing (test dependency)

## 🔧 Installation

Add the following dependency to your project:

```xml
<dependency>
    <groupId>com.github.allanhladki</groupId>
    <artifactId>flat2java-core</artifactId>
    <version>0.0.1-2</version>
</dependency>
```

## 🧪 Testing

The module includes comprehensive unit tests covering:
- Field serialization for all supported types
- Field deserialization for all supported types
- Round-trip operations and data integrity
- Configuration validation
- Error handling scenarios
- Edge cases and boundary conditions
- Inheritance scenarios
- Padding consistency

Run tests with:
```bash
mvn test
```

## 📚 Related Documentation

- **[Main README](../README.md)**: Complete project overview and examples
- **[Processor Module](../flat2java-processor/README.md)**: Annotation processor documentation
- **[Project Review](../PROJECT_REVIEW.md)**: Comprehensive project analysis

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details. 