# Flat2Java

[![Version](https://img.shields.io/badge/version-0.0.1--rc2-blue.svg)](https://github.com/allanhladki/flat2java/releases)
[![Java](https://img.shields.io/badge/java-11+-green.svg)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A comprehensive Java library for serializing and deserializing Java objects to/from fixed-length flat file formats, similar to COBOL's data structures.

## Description

Flat2Java provides robust functionality to serialize Java objects into fixed-length text fields for flat file formats and deserialize them back to Java objects. It's particularly useful when working with legacy systems, flat file formats that use fixed-length field definitions, or when you need reliable round-trip serialization.

## ✨ Key Features

- **Complete Round-Trip Support**: Serialize objects to flat strings and deserialize them back to identical objects
- **Comprehensive Type Support**: String, Integer, Long, BigInteger, BigDecimal, LocalDate, LocalDateTime
- **Annotation-Based Mapping**: `@FlatField` annotation for declarative field configuration
- **Flexible Configuration**: Environment variables and programmatic configuration options
- **Robust Validation**: Compile-time and runtime validation with detailed error messages
- **Interface-Based Architecture**: Clean interfaces with dependency injection support
- **Exception Translation**: Automatic exception translation with interceptors
- **Inheritance Support**: Handle object inheritance in serialization/deserialization
- **Padding Consistency**: Reliable padding handling between serialization and deserialization


## 🚀 Quick Start

### Basic Usage

```java
import com.github.allanhladki.flat2java.markers.FlatField;
import com.github.allanhladki.flat2java.serializer.ObjectSerializer;
import com.github.allanhladki.flat2java.deserializer.ObjectDeserializer;

public class MyRecord {
    @FlatField(length = 10)
    private String id;

    @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
    private LocalDate date;

    @FlatField(length = 10, precision = 2)
    private BigDecimal amount;
    
    // getters and setters...
}

// Serialize and deserialize
MyRecord record = new MyRecord("1234567890", LocalDate.of(2024, 3, 15), new BigDecimal("99.99"));
String flatFileString = ObjectSerializer.serialize(record);
MyRecord deserializedRecord = ObjectDeserializer.deserialize(flatFileString, MyRecord.class);
```

### Interface-Based Approach (Recommended)

```java
import com.github.allanhladki.flat2java.factory.Flat2JavaFactory;
import com.github.allanhladki.flat2java.interfaces.ObjectSerializationInterface;
import com.github.allanhladki.flat2java.interfaces.ObjectDeserializationInterface;

Flat2JavaFactory factory = new Flat2JavaFactory();
ObjectSerializationInterface objectSerializer = factory.getObjectSerializationInterface();
ObjectDeserializationInterface objectDeserializer = factory.getObjectDeserializationInterface();

String flatFileString = objectSerializer.serialize(record);
MyRecord deserializedRecord = objectDeserializer.deserialize(flatFileString, MyRecord.class);
```

### Dependency Injection with Spring

```java
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.allanhladki.flat2java.interfaces.*;

@Service
public class FlatFileService {
    
    @Autowired
    private ObjectSerializationInterface objectSerializer;
    
    @Autowired
    private ObjectDeserializationInterface objectDeserializer;
    
    
    public String serializeRecord(MyRecord record) {
        return objectSerializer.serialize(record);
    }
    
    public MyRecord deserializeRecord(String flatString) {
        return objectDeserializer.deserialize(flatString, MyRecord.class);
    }
}
```

**Spring Configuration:**
```java
@Configuration
public class Flat2JavaConfig {
    
    @Bean
    public Flat2JavaFactory flat2JavaFactory() {
        return new Flat2JavaFactory();
    }
    
    @Bean
    public ObjectSerializationInterface objectSerializer(Flat2JavaFactory factory) {
        return factory.getObjectSerializationInterface();
    }
    
    @Bean
    public ObjectDeserializationInterface objectDeserializer(Flat2JavaFactory factory) {
        return factory.getObjectDeserializationInterface();
    }
}
```

### Dependency Injection with Quarkus

```java
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import com.github.allanhladki.flat2java.interfaces.*;

@ApplicationScoped
public class FlatFileService {
    
    @Inject
    ObjectSerializationInterface objectSerializer;
    
    @Inject
    ObjectDeserializationInterface objectDeserializer;
    
    public String serializeRecord(MyRecord record) {
        return objectSerializer.serialize(record);
    }
    
    public MyRecord deserializeRecord(String flatString) {
        return objectDeserializer.deserialize(flatString, MyRecord.class);
    }
}
```

**Quarkus Configuration:**
```java
@ApplicationScoped
public class Flat2JavaProducer {
    
    @Produces
    @ApplicationScoped
    public Flat2JavaFactory flat2JavaFactory() {
        return new Flat2JavaFactory();
    }
    
    @Produces
    @ApplicationScoped
    public ObjectSerializationInterface objectSerializer(Flat2JavaFactory factory) {
        return factory.getObjectSerializationInterface();
    }
    
    @Produces
    @ApplicationScoped
    public ObjectDeserializationInterface objectDeserializer(Flat2JavaFactory factory) {
        return factory.getObjectDeserializationInterface();
    }
}
```

## 🔄 Round-Trip Operations

The library excels at complete round-trip operations, ensuring data integrity:

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

## 🏗️ Architecture

### Core Components

- **`ObjectSerializer`**: Main API for serializing objects to flat file strings
- **`ObjectDeserializer`**: Main API for deserializing flat file strings to objects
- **`FieldSerializer`**: Type-specific serialization logic for individual fields
- **`FieldDeserializer`**: Type-specific deserialization logic for individual fields
- **`FieldMetadata`**: Encapsulates field configuration and metadata
- **`FieldMetadataExtractor`**: Extracts and validates field metadata from classes


### Exception Handling

Comprehensive error handling with custom exceptions:

- `Flat2JavaException`: Base exception for all library errors
- `SerializationException`: Thrown during serialization failures
- `ValidationException`: Thrown during validation failures
- `DeserializationException`: Thrown during deserialization failures

## 📝 @FlatField Annotation

The `@FlatField` annotation is the core of field configuration:

### Required Attributes

- **`length`** (int): The length of the field in the flat file record

### Optional Attributes

- **`name`** (String): Custom field name (defaults to field name)
- **`precision`** (int): Decimal places for BigDecimal fields
- **`dateTimeFormat`** (String): Date format pattern for date/time fields
- **`padding`** (char): Custom padding character (defaults to space)

### Examples

```java
public class ExampleRecord {
    @FlatField(length = 10)
    private String id;

    @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
    private LocalDate date;

    @FlatField(length = 10, precision = 2)
    private BigDecimal amount;

    @FlatField(length = 5, padding = '*')
    private String code;
}
```

## ⚙️ Configuration

### Environment Variables

The library supports extensive configuration through environment variables:

| Variable | Description | Values | Default | Example |
|----------|-------------|---------|---------|---------|
| `FLAT2JAVA_ALLOW_NULL_STRINGS` | Allow null string field values | `true`, `false` | `false` | `export FLAT2JAVA_ALLOW_NULL_STRINGS="true"` |
| `FLAT2JAVA_NULL_VALUE_PADDING` | Padding character for null string values | Any single character | ` ` (space) | `export FLAT2JAVA_NULL_VALUE_PADDING="*"` |
| `FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR` | Use decimal separator in BigDecimal serialization | `true`, `false` | `false` | `export FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR="true"` |
| `FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR` | Decimal separator character for BigDecimal | Any single character | `.` | `export FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR=","` |
| `FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE` | Rounding mode for BigDecimal precision | `UNNECESSARY`, `UP`, `DOWN`, `CEILING`, `FLOOR`, `HALF_UP`, `HALF_DOWN`, `HALF_EVEN` | `UNNECESSARY` | `export FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE="HALF_UP"` |
| `FLAT2JAVA_THROW_ON_STRING_TRUNCATION` | Throw exception when string values exceed field length | `true`, `false` | `false` | `export FLAT2JAVA_THROW_ON_STRING_TRUNCATION="true"` |

**Quick Setup:**
```bash
# Enable null handling and set custom padding
export FLAT2JAVA_ALLOW_NULL_STRINGS="true"
export FLAT2JAVA_NULL_VALUE_PADDING="*"

# Use decimal separators with comma
export FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR="true"
export FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR=","

# Strict string validation
export FLAT2JAVA_THROW_ON_STRING_TRUNCATION="true"
```

### Programmatic Configuration

```java
import com.github.allanhladki.flat2java.config.Flat2JavaConfig;

// Configure programmatically
Flat2JavaConfig config = new Flat2JavaConfig();
config.setAllowNullStrings(true);
config.setNullValuePadding(' ');
config.setBigDecimalUseDecimalSeparator(false);
```

## 🔧 Installation

> **Note**: This project is currently in Release Candidate phase (version `0.0.1-rc2`). The API is stable but may have minor changes before the final release.

### Core Library

```xml
<dependency>
    <groupId>com.github.allanhladki</groupId>
    <artifactId>flat2java-core</artifactId>
    <version>0.0.1-rc2</version>
</dependency>
```

### Framework Integrations

#### Spring Boot Integration

```xml
<dependency>
    <groupId>com.github.allanhladki</groupId>
    <artifactId>flat2java-spring</artifactId>
    <version>0.0.1-rc2</version>
</dependency>
```

#### Quarkus Integration

```xml
<dependency>
    <groupId>com.github.allanhladki</groupId>
    <artifactId>flat2java-quarkus</artifactId>
    <version>0.0.1-rc2</version>
</dependency>
```

### Annotation Processor (Optional)

For compile-time validation:

```xml
<dependency>
    <groupId>com.github.allanhladki</groupId>
    <artifactId>flat2java-processor</artifactId>
    <version>0.0.1-rc2</version>
    <scope>provided</scope>
</dependency>
```



## 🧪 Testing

The library includes comprehensive test coverage:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ObjectRoundTripTest

# Run tests in specific module
mvn test -pl flat2java-core
```

## 📚 Documentation

- **[Core Module](flat2java-core/README.md)**: Detailed information about the core library
- **[Processor Module](flat2java-processor/README.md)**: Annotation processor documentation
- **[Spring Integration](flat2java-spring/README.md)**: Spring Boot integration guide
- **[Quarkus Integration](flat2java-quarkus/README.md)**: Quarkus integration guide


## 🤝 Contributing

Contributions are welcome! Please ensure all tests pass before submitting a pull request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
