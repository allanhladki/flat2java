# Flat2Java Annotation Processor

This module provides compile-time validation for the flat2java library using Java's annotation processing API.

## Description

The annotation processor automatically validates `@FlatField` annotations during compilation, catching configuration errors early in the development cycle. This helps prevent runtime errors and improves development experience.

## Features

- **Compile-time Validation**: Validates field configurations during compilation
- **Early Error Detection**: Catches configuration errors before runtime
- **IDE Integration**: Works seamlessly with IDEs that support annotation processing
- **Build Integration**: Integrates with Maven and other build tools

## Validations Performed

The processor validates the following aspects of `@FlatField` annotations:

### Field Length Validation
- Ensures `length` is a positive integer
- Validates that `length > 0`

### Precision Validation
- Ensures `precision` is a non-negative integer
- Validates that `precision < length` (precision must be smaller than field length)

### DateTime Format Validation
- Validates `dateTimeFormat` patterns are syntactically correct
- Tests format patterns with sample dates to ensure they work

### Field Type Validation
- Framework for validating supported field types (extensible)

## Installation

Add the following dependency to your project:

```xml
<dependency>
    <groupId>com.github.allanhladki</groupId>
    <artifactId>flat2java-processor</artifactId>
    <version>0.0.1-2</version>
    <scope>provided</scope>
</dependency>
```

## Usage

The annotation processor is automatically invoked during compilation when you have `@FlatField` annotations in your code.

### Example Usage

```java
public class MyRecord {
    @FlatField(length = 10)  // ✅ Valid
    private String id;

    @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")  // ✅ Valid
    private LocalDate date;

    @FlatField(length = 10, precision = 2)  // ✅ Valid
    private BigDecimal amount;

    @FlatField(length = 0)  // ❌ Compile error: length must be positive
    private String invalidField;

    @FlatField(length = 5, precision = 6)  // ❌ Compile error: precision >= length
    private BigDecimal invalidPrecision;

    @FlatField(length = 8, dateTimeFormat = "invalid")  // ❌ Compile error: invalid format
    private LocalDate invalidDate;
}
```

## Build Integration

### Maven

The processor works automatically with Maven when added as a dependency. For explicit configuration:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>com.github.allanhladki</groupId>
                <artifactId>flat2java-processor</artifactId>
                <version>0.0.1-2</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### Gradle

```gradle
dependencies {
    annotationProcessor 'com.github.allanhladki:flat2java-processor:0.0.1-2'
}
```

## Error Messages

The processor provides clear, actionable error messages:

```
[ERROR] @FlatField 'length' must be a positive integer, current: 0
[ERROR] @FlatField 'precision' (6) must be smaller than 'length' (5)
[ERROR] @FlatField 'dateTimeFormat' has invalid pattern: invalid - Unknown pattern letter: i
```

## Benefits

1. **Early Error Detection**: Catch configuration errors during compilation
2. **Improved Developer Experience**: Clear error messages with context
3. **Reduced Runtime Errors**: Prevent invalid configurations from reaching production
4. **IDE Support**: Works with IntelliJ IDEA, Eclipse, and other IDEs
5. **Build Integration**: Fails builds with invalid configurations

## Limitations

- Only validates `@FlatField` annotations
- Limited to compile-time validation (runtime validation still occurs)
- Requires annotation processing to be enabled in your build

## Troubleshooting

### Processor Not Running
- Ensure the dependency is added with `scope=provided`
- Check that annotation processing is enabled in your IDE/build tool
- Verify the processor is on the classpath during compilation

### False Positives
- Some valid configurations might be flagged if they depend on runtime values
- Use runtime validation for complex scenarios

## Requirements

- Java 11 or higher
- Annotation processing support in your build tool/IDE
- flat2java-core dependency

## License

This project is licensed under the MIT License - see the LICENSE file for details. 