# Flat2Java Quarkus Integration

Quarkus integration module for Flat2Java, providing CDI producers and native Quarkus support for Flat2Java services.

## 📦 Version Information

- **Current Version**: `0.0.1-rc2`
- **Release Type**: Release Candidate
- **Quarkus Version**: 3.6.0+
- **Java Version**: 11+

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>com.github.allanhladki</groupId>
    <artifactId>flat2java-quarkus</artifactId>
    <version>0.0.1-rc2</version>
</dependency>
```


## ✨ Features

- **CDI Integration**: Full CDI support with `@ApplicationScoped` beans
- **Quarkus Native**: Optimized for Quarkus native compilation
- **Dependency Injection**: Ready-to-use beans for all Flat2Java interfaces
- **Extension Support**: Native Quarkus extension with build-time optimization
- **Zero Configuration**: Works out of the box with Quarkus applications


## 📝 Usage Examples

### Basic Injection

```java
@ApplicationScoped
public class FlatFileService {
    
    @Inject
    private ObjectSerializationInterface serializer;
    
    @Inject
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
@ApplicationScoped
public class FlatFileService {
    
    private final ObjectSerializationInterface serializer;
    private final ObjectDeserializationInterface deserializer;
    
    @Inject
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
@ApplicationScoped
public class FieldService {
    
    @Inject
    private FieldSerializationInterface fieldSerializer;
    
    @Inject
    private FieldDeserializationInterface fieldDeserializer;
    
    public String serializeField(String value, FieldMetadata metadata) {
        return fieldSerializer.serialize(value, metadata);
    }
    
    public Object parseField(String slice, FieldMetadata metadata) {
        return fieldDeserializer.parse(slice, metadata);
    }
}
```

### REST Endpoint Example

```java
@Path("/flatfile")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class FlatFileResource {
    
    @Inject
    private ObjectSerializationInterface serializer;
    
    @Inject
    private ObjectDeserializationInterface deserializer;
    
    @POST
    @Path("/serialize")
    public Response serializeRecord(MyRecord record) {
        try {
            String flat = serializer.serialize(record);
            return Response.ok(flat).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("Serialization failed: " + e.getMessage())
                    .build();
        }
    }
    
    @POST
    @Path("/deserialize")
    public Response deserializeRecord(String flatString) {
        try {
            MyRecord record = deserializer.deserialize(flatString, MyRecord.class);
            return Response.ok(record).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity("Deserialization failed: " + e.getMessage())
                    .build();
        }
    }
}
```

## 🏗️ Quarkus Extension

The module includes a native Quarkus extension that:

- Registers the `flat2java-quarkus` feature
- Ensures CDI beans are available during build time
- Provides build-time validation
- Optimizes for native compilation

### Extension Features

- **Feature Registration**: `flat2java-quarkus`
- **Build-Time Beans**: Automatic bean discovery and registration
- **Native Support**: Full compatibility with Quarkus native compilation

## 🧪 Testing

### Test Configuration

```java
@QuarkusTest
class FlatFileServiceTest {
    
    @Inject
    private ObjectSerializationInterface serializer;
    
    @Inject
    private ObjectDeserializationInterface deserializer;
    
    @Test
    void testSerialization() {
        MyRecord record = new MyRecord("123", "Test");
        String flat = serializer.serialize(record);
        assertNotNull(flat);
    }
    
    @Test
    void testDeserialization() {
        String flat = "123     Test    ";
        MyRecord record = deserializer.deserialize(flat, MyRecord.class);
        assertEquals("123", record.getId());
        assertEquals("Test", record.getName());
    }
}
```

### Integration Test

```java
@QuarkusTest
class FlatFileResourceTest {
    
    @Test
    void testSerializeEndpoint() {
        MyRecord record = new MyRecord("123", "Test");
        
        given()
            .contentType(ContentType.JSON)
            .body(record)
        .when()
            .post("/flatfile/serialize")
        .then()
            .statusCode(200)
            .body(not(emptyString()));
    }
}
```

## ⚙️ Configuration

### Application Properties

```properties
# Quarkus-specific configuration
quarkus.arc.detect-unused-false-positives=true
quarkus.arc.remove-unused-beans=false

# Flat2Java configuration (via environment variables)
FLAT2JAVA_ALLOW_NULL_STRINGS=true
FLAT2JAVA_NULL_VALUE_PADDING=*
FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR=true
FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR=,
FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE=HALF_UP
FLAT2JAVA_THROW_ON_STRING_TRUNCATION=true
```

### Environment Variables

```bash
export FLAT2JAVA_ALLOW_NULL_STRINGS=true
export FLAT2JAVA_NULL_VALUE_PADDING="*"
export FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR=true
export FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR=","
export FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE=HALF_UP
export FLAT2JAVA_THROW_ON_STRING_TRUNCATION=true
```

## 🚀 Native Compilation

### GraalVM Native Image

The module is fully compatible with Quarkus native compilation:

```bash
# Build native image
mvn clean package -Pnative

# Run native image
./target/quarkus-app/quarkus-run
```

### Native Image Configuration

```properties
# Native image configuration
quarkus.native.additional-build-args=\
    --initialize-at-build-time=com.github.allanhladki.flat2java,\
    --report-unsupported-elements-at-runtime
```

## 🔧 Troubleshooting

### Common Issues

1. **Bean Not Found**: Ensure the dependency is included and CDI is enabled
2. **Injection Failure**: Check that the class is properly annotated with CDI scopes
3. **Native Compilation Issues**: Verify GraalVM compatibility

### Debug Mode

Enable debug logging to see CDI bean creation:

```properties
quarkus.log.category."com.github.allanhladki.flat2java.quarkus".level=DEBUG
quarkus.log.category."io.quarkus.arc".level=DEBUG
```

### Bean Validation

Use Quarkus Dev Mode to inspect beans:

```bash
mvn quarkus:dev
```

Then visit: http://localhost:8080/q/dev/io.quarkus.quarkus-arc/beans

## 📚 Related Documentation

- **[Core Module](../flat2java-core/README.md)**: Core library documentation
- **[Processor Module](../flat2java-processor/README.md)**: Annotation processor documentation
- **[Main Project](../../README.md)**: Project overview and examples

## 🤝 Contributing

Contributions are welcome! Please ensure all tests pass and follow the project's coding standards.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](../../LICENSE) file for details.
