# flat2java-processor

This module provides the annotation processor for the flat2java toolkit. It performs compile-time code generation and validation for Java projects.

## Usage as a Plugin in Other Projects

To use `flat2java-processor` in your Maven project for compile-time verifications:

1. **Add the dependency to your `pom.xml`:**

```xml
<dependency>
    <groupId>com.github.allanhladki.flat2java</groupId>
    <artifactId>flat2java-processor</artifactId>
    <version>0.0.1-1</version>
    <scope>provided</scope>
</dependency>
```

2. **Build your project:**

The annotation processor will be automatically invoked during compilation. Any compile-time errors or generated code will be handled as part of the build process.

## Notes
- Make sure the processor is available on the classpath during compilation.
- You can configure annotation processing options via the Maven Compiler Plugin if needed.

## Example: Customizing Annotation Processing (Optional)

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>com.github.allanhladki.flat2java</groupId>
                <artifactId>flat2java-processor</artifactId>
                <version>0.0.1-1</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

---

For more information, see the main [flat2java README](../README.md). 