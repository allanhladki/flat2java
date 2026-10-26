<div align="center">

# flat2java-quarkus

**CDI producers for flat2java in Quarkus applications.**

[![Maven Central](https://img.shields.io/maven-central/v/io.github.allanhladki.flat2java/flat2java-quarkus?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.allanhladki.flat2java/flat2java-quarkus)
![Java 17+](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white)
![Quarkus](https://img.shields.io/badge/Quarkus-CDI-4695EB?logo=quarkus&logoColor=white)
![MicroProfile Config](https://img.shields.io/badge/MicroProfile-Config-E8A33D)

[Installation](#installation) · [Usage](#usage) · [Properties](#properties) · [Replacing the beans](#replacing-the-beans) · [Native image](#native-image) · [Project README](../README.md)

</div>

---

Makes a `Flat2Java` bean available for injection, configured from `flat2java.*` properties.

> [!NOTE]
> This is a plain CDI library, not a Quarkus extension. Quarkus discovers it through the
> `beans.xml` in the jar.

## Installation

```xml
<dependency>
    <groupId>io.github.allanhladki.flat2java</groupId>
    <artifactId>flat2java-quarkus</artifactId>
    <version>0.1.0</version>
</dependency>
```

<details>
<summary>Gradle</summary>

```kotlin
dependencies {
    implementation("io.github.allanhladki.flat2java:flat2java-quarkus:0.1.0")
}
```

</details>

## Usage

```java
@ApplicationScoped
public class PaymentFileService {

    @Inject
    Flat2Java flat;

    public Payment parse(String line) {
        return flat.read(line, Payment.class);
    }
}
```

A complete Quarkus application that uses this module is in
[flat2java-quarkus-demo](https://github.com/allanhladki/flat2java-quarkus-demo).

## Properties

`application.properties`, with the default values:

```properties
flat2java.allow-null-strings=true
# flat2java.null-value-padding defaults to a space
flat2java.fail-on-string-truncation=false
flat2java.decimal.use-separator=false
flat2java.decimal.separator=.
flat2java.decimal.rounding-mode=unnecessary
```

The keys are the same as in the Spring module and are described in the
[project README](../README.md#configuration).

> [!TIP]
> MicroProfile Config also maps environment variables such as `FLAT2JAVA_DECIMAL_SEPARATOR` to
> these keys.

## Replacing the beans

`Flat2JavaOptions` and `Flat2Java` are produced as `@Singleton @DefaultBean`s. A producer of either
type in the application takes precedence:

```java
@Produces
@Singleton
Flat2JavaOptions options() {
    return Flat2JavaOptions.builder().failOnStringTruncation(true).build();
}
```

## Native image

flat2java reads and writes fields through reflection. When building a native executable, register
every mapped class, including nested classes and records, for reflection:

```java
@RegisterForReflection
public class Payment { ... }
```

> [!WARNING]
> Native compilation is not covered by this project's tests.
