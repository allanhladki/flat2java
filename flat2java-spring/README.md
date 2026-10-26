<div align="center">

# flat2java-spring

**Spring Boot auto-configuration for flat2java.**

[![Maven Central](https://img.shields.io/maven-central/v/io.github.allanhladki.flat2java/flat2java-spring?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.allanhladki.flat2java/flat2java-spring)
![Java 17+](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3-6DB33F?logo=springboot&logoColor=white)

[Installation](#installation) · [Usage](#usage) · [Properties](#properties) · [Replacing the beans](#replacing-the-beans) · [Project README](../README.md)

</div>

---

Registers a `Flat2Java` bean configured from `flat2java.*` properties. Requires Spring Boot 3; the
module is built against 3.3.

## Installation

```xml
<dependency>
    <groupId>io.github.allanhladki.flat2java</groupId>
    <artifactId>flat2java-spring</artifactId>
    <version>0.1.0</version>
</dependency>
```

<details>
<summary>Gradle</summary>

```kotlin
dependencies {
    implementation("io.github.allanhladki.flat2java:flat2java-spring:0.1.0")
}
```

</details>

## Usage

Inject `Flat2Java` like any other bean:

```java
@Service
public class PaymentFileService {

    private final Flat2Java flat;

    public PaymentFileService(Flat2Java flat) {
        this.flat = flat;
    }

    public String export(Payment payment) {
        return flat.write(payment);
    }
}
```

A complete Spring Boot application that uses this module is in
[flat2java-spring-demo](https://github.com/allanhladki/flat2java-spring-demo).

## Properties

The values shown are the defaults.

<table>
<tr><th><code>application.yml</code></th><th><code>application.properties</code></th></tr>
<tr>
<td>

```yaml
flat2java:
  allow-null-strings: true
  null-value-padding: " "
  fail-on-string-truncation: false
  decimal:
    use-separator: false
    separator: "."
    rounding-mode: unnecessary
```

</td>
<td>

```properties
flat2java.allow-null-strings=true
# flat2java.null-value-padding defaults to a space
flat2java.fail-on-string-truncation=false
flat2java.decimal.use-separator=false
flat2java.decimal.separator=.
flat2java.decimal.rounding-mode=unnecessary
```

</td>
</tr>
</table>

The meaning of each property is described in the [project README](../README.md#configuration).

> [!TIP]
> Spring's relaxed binding also accepts environment variables of the same name, such as
> `FLAT2JAVA_DECIMAL_SEPARATOR`.

## Replacing the beans

The auto-configuration defines two beans and backs off when the application defines either one:

| Bean | Condition |
|---|---|
| `Flat2JavaOptions` | `@ConditionalOnMissingBean`, built from the properties |
| `Flat2Java` | `@ConditionalOnMissingBean`, built from the `Flat2JavaOptions` bean |

```java
@Bean
Flat2JavaOptions flat2JavaOptions() {
    return Flat2JavaOptions.builder().failOnStringTruncation(true).build();
}
```

To use several configurations at once, for example for two file layouts, define the extra
`Flat2Java` instances as qualified beans with `Flat2Java.create(options)`.
