# Flat2Java Core

The core library module for Flat2Java, providing the main serialization and deserialization functionality for fixed-length flat file formats.

## 🏗️ Architecture Overview

### Core Design Principles

- **Separation of Concerns**: Clear separation between serialization, deserialization, and metadata extraction
- **Interface-Based Design**: All functionality exposed through well-defined interfaces
- **Exception Translation**: Automatic exception translation through interceptor pattern
- **Factory Pattern**: Centralized service creation with dependency injection support
- **Utility Classes**: Static utility methods for direct usage without interfaces

### Module Structure

```
flat2java-core/
├── src/main/java/com/github/allanhladki/flat2java/
│   ├── config/           # Configuration management
│   ├── deserializer/     # Deserialization logic
│   ├── exception/        # Custom exceptions and interceptors
│   ├── factory/          # Service factory and DI support
│   ├── fixtures/         # Test fixtures and utilities
│   ├── impl/             # Interface implementations
│   ├── interfaces/       # Public interfaces
│   ├── markers/          # Annotations
│   ├── metadata/         # Field metadata and extraction
│   └── serializer/       # Serialization logic
└── src/test/java/        # Comprehensive test suite
```

## 🔧 Development Setup

### Prerequisites

- **Java**: 11 or higher
- **Maven**: 3.6+ (for building and testing)

### Local Development

```bash
# Clone the repository
git clone https://github.com/allanhladki/flat2java.git
cd flat2java

# Build the project
mvn clean install

# Run tests
mvn test

# Run specific test class
mvn test -Dtest=ObjectDeserializerTest

```

## 🧪 Testing Strategy

### Test Organization

- **Unit Tests**: Individual component testing with mocked dependencies
- **Integration Tests**: End-to-end functionality testing
- **Round-Trip Tests**: Serialization → Deserialization → Validation
- **Exception Tests**: Error handling and edge case validation
- **Performance Tests**: Load testing for large datasets

### Test Patterns

```java
@DisplayName("ObjectDeserializer")
class ObjectDeserializerTest {
    
    @Test
    @DisplayName("Should deserialize valid flat string to object")
    void testValidDeserialization() {
        // Given
        String flatString = "TEST123   20240315";
        Class<TestRecord> clazz = TestRecord.class;
        
        // When
        TestRecord result = ObjectDeserializer.deserialize(flatString, clazz);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("TEST123");
    }
    
    @Test
    @DisplayName("Should throw DeserializationException for invalid input")
    void testInvalidInputThrowsException() {
        // Given
        String invalidString = "INVALID";
        
        // When & Then
        assertThatThrownBy(() -> 
            ObjectDeserializer.deserialize(invalidString, TestRecord.class)
        )
        .isInstanceOf(DeserializationException.class)
        .hasMessageContaining("Input length");
    }
}
```

### Test Fixtures

```java
public class TestFixtures {
    
    @FlatField(length = 10)
    public static class SimpleRecord {
        public String id;
        
        public SimpleRecord() {}
        public SimpleRecord(String id) { this.id = id; }
    }
    
    public static SimpleRecord createValidRecord() {
        return new SimpleRecord("TEST123");
    }
    
    public static String createValidFlatString() {
        return "TEST123   "; // 10 characters with padding
    }
}
```

## 🏛️ Architecture Deep Dive

### Core Components

#### 1. Serialization Pipeline

```
Object → FieldMetadata → FieldSerializer → Formatted String
```

**Key Classes:**
- `ObjectSerializer`: Orchestrates object serialization
- `FieldSerializer`: Handles type-specific serialization
- `FieldMetadata`: Contains field configuration and metadata

#### 2. Deserialization Pipeline

```
Flat String → FieldMetadata → FieldDeserializer → Parsed Object
```

**Key Classes:**
- `ObjectDeserializer`: Orchestrates object deserialization
- `FieldDeserializer`: Handles type-specific deserialization
- `FieldMetadata`: Contains field configuration and metadata

#### 3. Metadata Extraction

```
Class → Reflection → @FlatField Annotations → FieldMetadata List
```

**Key Classes:**
- `FieldMetadataExtractor`: Extracts metadata from annotated classes
- `FieldMetadata`: Encapsulates field configuration
- `FlatField`: Annotation for field configuration

### Interface Design

```java
// Core interfaces for dependency injection
public interface ObjectSerializationInterface {
    String serialize(Object object);
    String serialize(Object object, boolean includeInherited);
}

public interface ObjectDeserializationInterface {
    <T> T deserialize(String flat, Class<T> clazz);
    <T> T deserialize(String flat, Class<T> clazz, boolean includeInherited);
}

public interface FieldSerializationInterface {
    String serializeString(String value);
    String serializeInteger(Integer value);
    // ... other type-specific methods
}

public interface FieldDeserializationInterface {
    Object parse(String slice, FieldMetadata metadata);
}
```

### Exception Handling Architecture

```java
@TranslateExceptions(operationType = OperationType.DESERIALIZATION)
public Object parse(String slice, FieldMetadata metadata) {
    // Method implementation with automatic exception translation
}

// Exception hierarchy
Flat2JavaException (base)
├── SerializationException
├── DeserializationException
└── ValidationException
```

## 🔄 Data Flow

### Serialization Flow

1. **Object Input**: Java object with @FlatField annotations
2. **Metadata Extraction**: Extract field metadata using reflection
3. **Field Processing**: Process each field according to its type and configuration
4. **String Formatting**: Apply padding and length constraints
5. **Concatenation**: Combine all field strings into final flat string

### Deserialization Flow

1. **String Input**: Flat file string with fixed-length fields
2. **Metadata Extraction**: Extract field metadata from target class
3. **String Slicing**: Slice input string according to field lengths
4. **Field Parsing**: Parse each slice according to field type and configuration
5. **Object Construction**: Create and populate target object instance

## ⚙️ Configuration Management

### Environment Variables

| Variable | Purpose | Default | Validation |
|----------|---------|---------|------------|
| `FLAT2JAVA_ALLOW_NULL_STRINGS` | Enable null string handling | `false` | Boolean parse |
| `FLAT2JAVA_NULL_VALUE_PADDING` | Null value padding character | ` ` (space) | Single character |
| `FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR` | Use decimal separators | `false` | Boolean parse |
| `FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR` | Decimal separator character | `.` | Single character |
| `FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE` | BigDecimal rounding mode | `UNNECESSARY` | Enum validation |
| `FLAT2JAVA_THROW_ON_STRING_TRUNCATION` | Strict string validation | `false` | Boolean parse |

### Configuration Validation

```java
public class Flat2JavaConfig {
    
    public void validateConfiguration() {
        if (bigDecimalDecimalSeparator == null || 
            String.valueOf(bigDecimalDecimalSeparator).length() != 1) {
            throw new IllegalArgumentException("Decimal separator must be a single character");
        }
        
        if (bigDecimalRoundingMode == null) {
            throw new IllegalArgumentException("Rounding mode cannot be null");
        }
    }
}
```

## 🧹 Code Quality

### Code Style

- **Naming**: Use descriptive names for methods and variables
- **Documentation**: Javadoc for all public methods and classes
- **Constants**: Extract magic numbers and strings to constants
- **Error Handling**: Use appropriate exception types with meaningful messages

### Static Analysis

```bash
# Run Spotless
mvn spotless:apply

```

## 🔍 Debugging and Troubleshooting

### Common Issues

#### 1. Field Length Mismatch
```java
// Problem: Input string length doesn't match expected total field length
String input = "SHORT";
// Expected: 10 characters (based on @FlatField(length = 10))
// Actual: 5 characters

// Solution: Ensure input string matches total expected length
String paddedInput = String.format("%-10s", input); // Pad to 10 characters
```

#### 2. Date Format Issues
```java
// Problem: Date format doesn't match @FlatField annotation
@FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
private LocalDate date;

// Input: "15/03/24" (wrong format)
// Expected: "20240315" (yyyyMMdd format)

// Solution: Ensure input matches specified date format
```

#### 3. Null Value Handling
```java
// Problem: Null values when null handling is disabled
// Solution: Enable null handling or provide default values
export FLAT2JAVA_ALLOW_NULL_STRINGS="true"
```

### Debug Logging

```java
// Enable debug logging for troubleshooting
logging.level.io.github.allanhladki.flat2java=DEBUG

// Key debug points:
// - Field metadata extraction
// - Serialization/deserialization steps
// - Exception translation
// - Configuration loading
```

## 📞 Support and Resources

### Getting Help

- **Issues**: [GitHub Issues](https://github.com/allanhladki/flat2java/issues)
- **Discussions**: [GitHub Discussions](https://github.com/allanhladki/flat2java/discussions)

### Related Projects

- **flat2java-processor**: Annotation processor for compile-time validation

---

**Maintainer Notes**: This README should be updated whenever significant architectural changes are made or new features are added. Keep the examples current and ensure all links are working.
