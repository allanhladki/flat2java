<div align="center">

# flat2java-processor

**Compile-time validation of `@FlatField` mappings.**

[![Maven Central](https://img.shields.io/maven-central/v/io.github.allanhladki.flat2java/flat2java-processor?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.allanhladki.flat2java/flat2java-processor)
![Java 17+](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white)
![javac](https://img.shields.io/badge/javac-annotation%20processor-555555)

[Setup](#setup) · [Checks](#checks) · [Example output](#example-output) · [Project README](../README.md)

</div>

---

An annotation processor that reports invalid `@FlatField` declarations as compilation errors. It
applies the same rules, with the same messages, as the validation `flat2java-core` performs at
runtime, so a mapping that compiles will not fail with a `MappingException` later.

> [!NOTE]
> Two rules are checked only at runtime. A missing no-arg constructor on a top-level mapped class:
> the processor cannot tell which classes are read, and an abstract class may only be the base of
> mapped subclasses. The room for a decimal separator: it depends on the `useDecimalSeparator`
> option, which is known only when `Flat2Java` is created.

## Setup

Register the processor on the annotation processor path.

> [!IMPORTANT]
> Since JDK 23, `javac` no longer runs processors found on the regular classpath unless
> `-proc:full` is set, so a plain `<dependency>` is not enough.

**Maven**

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

<details>
<summary>Gradle</summary>

```kotlin
dependencies {
    annotationProcessor("io.github.allanhladki.flat2java:flat2java-processor:0.1.0")
}
```

</details>

> [!TIP]
> If the build already declares other processors in `annotationProcessorPaths`, such as Lombok or
> MapStruct, add this one to the same list.

## Checks

| Area | Rule |
|---|---|
| Length | `length` greater than zero. |
| Precision | Not negative, smaller than `length`, and only on `BigDecimal`. |
| Type | A supported scalar, a concrete nested class or record with `@FlatField` fields, or a `List`/array of those. `Object`, interfaces, abstract classes and type variables are rejected. |
| Dates | `dateTimeFormat` present on `LocalDate`/`LocalDateTime` fields only. It must be a valid pattern with a fixed width equal to `length`, and keep the century (no `yy`). |
| Padding | Only on `String` fields. |
| Modifiers | `@FlatField` not on `static` fields. |
| Position | All-or-nothing across the class hierarchy, unique and contiguous from 1. |
| Nested records | `length` equal to the sum of the nested fields, no cycles, a no-arg constructor (not required for Java records). Inner classes must be `static`. |
| Lists and arrays | `occurs` set, greater than zero and only on those fields; elements that are not lists or arrays; no raw `List` or wildcard. |

## Example output

```text
Payment.java:12: error: Payment.date: dateTimeFormat 'yyyyMMdd' produces 8 characters, but length is 10
    private LocalDate date;
                      ^
```
