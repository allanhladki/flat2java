# flat2java-processor

Annotation processor that reports invalid `@FlatField` declarations as compilation errors. It
applies the same rules, with the same messages, as the validation `flat2java-core` performs at
runtime, so a mapping that compiles will not fail with a `MappingException` later. The one
exception is a missing no-arg constructor on a top-level mapped class: the processor cannot tell
which classes are read.

## Setup

Register the processor on the annotation processor path. Since JDK 23, `javac` no longer runs
processors found on the regular classpath unless `-proc:full` is set, so a plain dependency is not
enough.

Maven:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>io.github.allanhladki.flat2java</groupId>
                <artifactId>flat2java-processor</artifactId>
                <version>0.1.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

Gradle:

```kotlin
dependencies {
    annotationProcessor("io.github.allanhladki.flat2java:flat2java-processor:0.1.0")
}
```

If the build already declares other processors in `annotationProcessorPaths`, such as Lombok or
MapStruct, add this one to the same list.

## Checks

- `length` greater than zero. `precision` not negative, smaller than `length`, and only on
  `BigDecimal`.
- Supported field type, or a concrete nested class or record with `@FlatField` fields. `Object`, interfaces,
  abstract classes and type variables are rejected.
- `dateTimeFormat` present on `LocalDate`/`LocalDateTime` fields only. It must be a valid pattern
  with a fixed width equal to `length`.
- `padding` only on `String` fields. `@FlatField` not on `static` fields.
- `position` all-or-nothing across the class hierarchy, unique and contiguous from 1.
- Nested records: `length` equal to the sum of the nested fields, no cycles, a no-arg constructor
  (not required for Java records).
- `List` and array fields: `occurs` set and greater than zero, and only on those fields; an element
  type that is not itself a list or array; no raw `List` or wildcard.

Example output:

```
Payment.java:12: error: Payment.date: dateTimeFormat 'yyyyMMdd' produces 8 characters, but length is 10
    private LocalDate date;
                      ^
```
