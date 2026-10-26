# Flat2Java Spring Integration

Spring Boot integration module for Flat2Java, providing automatic configuration and dependency injection for Flat2Java services.

## 📦 Version Information

- **Current Version**: `0.0.1-rc2`
- **Release Type**: Release Candidate
- **Spring Boot Version**: 3.2.0+
- **Java Version**: 11+

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>io.github.allanhladki</groupId>
    <artifactId>flat2java-spring</artifactId>
    <version>0.0.1-rc2</version>
</dependency>
```

### Gradle Dependency

```gradle
implementation 'io.github.allanhladki:flat2java-spring:0.0.1-rc2'
```

## ✨ Features

- **Auto-Configuration**: Automatically configures Flat2Java services
- **Dependency Injection**: Ready-to-use beans for all Flat2Java interfaces
- **Configuration Properties**: Externalized configuration via application.properties
- **Conditional Beans**: Only creates beans when not already defined
- **Spring Boot Native**: Full Spring Boot integration

## 🔧 Auto-Configuration

The module automatically configures the following beans:

| Bean Name | Interface | Description |
|-----------|-----------|-------------|
| `flat2JavaFactory` | `Flat2JavaFactory` | Main factory for creating services |
| `fieldMetadataInterface` | `FieldMetadataInterface` | Field metadata extraction |
| `fieldSerializationInterface` | `FieldSerializationInterface` | Field serialization |
| `objectSerializationInterface` | `ObjectSerializationInterface` | Object serialization |
| `fieldDeserializationInterface` | `FieldDeserializationInterface` | Field deserialization |
| `objectDeserializationInterface` | `ObjectDeserializationInterface` | Object deserialization |

## 📝 Usage Examples

### Basic Injection

```java
@Service
public class FlatFileService {
    
    @Autowired
    private ObjectSerializationInterface serializer;
    
    @Autowired
    private ObjectDeserializationInterface deserializer;
    
    public String serializeRecord(MyRecord record) {
        return serializer.serialize(record);
    }
    
    public MyRecord deserializeRecord(String flatString) {
        return deserializer.deserialize(flatString, MyRecord.class);
    }
}
```

### Constructor Injection

```java
@Service
public class FlatFileService {
    
    private final ObjectSerializationInterface serializer;
    private final ObjectDeserializationInterface deserializer;
    
    public FlatFileService(
            ObjectSerializationInterface serializer,
            ObjectDeserializationInterface deserializer) {
        this.serializer = serializer;
        this.deserializer = deserializer;
    }
}
```

### Field-Level Operations

```java
@Service
public class FieldService {
    
    @Autowired
    private FieldSerializationInterface fieldSerializer;
    
    @Autowired
    private FieldDeserializationInterface fieldDeserializer;
    
    public String serializeField(String value, FieldMetadata metadata) {
        return fieldSerializer.serialize(value, metadata);
    }
    
    public Object parseField(String slice, FieldMetadata metadata) {
        return fieldDeserializer.parse(slice, metadata);
    }
}
```

## ⚙️ Configuration

### Application Properties

```properties
# Flat2Java Configuration
flat2java.allow-null-strings=true
flat2java.null-value-padding=*
flat2java.bigdecimal.use-decimal-separator=true
flat2java.bigdecimal.decimal-separator=,
flat2java.bigdecimal.rounding-mode=HALF_UP
flat2java.validation.throw-on-string-truncation=true
```

### YAML Configuration

```yaml
flat2java:
  allow-null-strings: true
  null-value-padding: "*"
  bigdecimal:
    use-decimal-separator: true
    decimal-separator: ","
    rounding-mode: HALF_UP
  validation:
    throw-on-string-truncation: true
```

### Environment Variables

```bash
export FLAT2JAVA_ALLOW_NULL_STRINGS=true
export FLAT2JAVA_NULL_VALUE_PADDING="*"
export FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR=true
export FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR=","
export FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE=HALF_UP
export FLAT2JAVA_VALIDATION_THROW_ON_STRING_TRUNCATION=true
```

## 🔍 Configuration Properties Reference

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `flat2java.allow-null-strings` | boolean | false | Allow null string field values |
| `flat2java.null-value-padding` | char | space | Padding character for null values |
| `flat2java.bigdecimal.use-decimal-separator` | boolean | false | Use decimal separator in BigDecimal |
| `flat2java.bigdecimal.decimal-separator` | char | . | Decimal separator character |
| `flat2java.bigdecimal.rounding-mode` | String | UNNECESSARY | Rounding mode for BigDecimal |
| `flat2java.validation.throw-on-string-truncation` | boolean | false | Throw on string truncation |

## 🧪 Testing

### Test Configuration

```java
@SpringBootTest
class FlatFileServiceTest {
    
    @Autowired
    private ObjectSerializationInterface serializer;
    
    @Autowired
    private ObjectDeserializationInterface deserializer;
    
    @Test
    void testSerialization() {
        MyRecord record = new MyRecord("123", "Test");
        String flat = serializer.serialize(record);
        assertNotNull(flat);
    }
}
```

### Test Properties

```properties
# test/resources/application.properties
flat2java.allow-null-strings=true
flat2java.validation.throw-on-string-truncation=false
```

## 🚫 Disabling Auto-Configuration

To disable auto-configuration for specific beans:

```java
@Configuration
public class CustomFlat2JavaConfig {
    
    @Bean
    @Primary
    public ObjectSerializationInterface customSerializer() {
        // Custom implementation
        return new CustomSerializer();
    }
}
```

## 🔧 Troubleshooting

### Common Issues

1. **Bean Not Found**: Ensure the dependency is included and auto-configuration is enabled
2. **Configuration Not Applied**: Check property names and values in application.properties
3. **Version Conflicts**: Ensure Spring Boot version compatibility

### Debug Mode

Enable debug logging to see auto-configuration details:

```properties
logging.level.io.github.allanhladki.flat2java.spring=DEBUG
logging.level.org.springframework.boot.autoconfigure=DEBUG
```

## 📚 Related Documentation

- **[Core Module](../flat2java-core/README.md)**: Core library documentation
- **[Processor Module](../flat2java-processor/README.md)**: Annotation processor documentation
- **[Main Project](../../README.md)**: Project overview and examples

## 🤝 Contributing

Contributions are welcome! Please ensure all tests pass and follow the project's coding standards.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](../../LICENSE) file for details.
