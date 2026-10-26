# flat2java-spring

Spring Boot auto-configuration that registers a `Flat2Java` bean configured from `flat2java.*`
properties. Requires Spring Boot 3. The module is built against 3.3.

```xml
<dependency>
    <groupId>io.github.allanhladki.flat2java</groupId>
    <artifactId>flat2java-spring</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Usage

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

## Properties

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

The values shown are the defaults. Their meaning is described in the
[project README](../README.md#configuration). Spring's relaxed binding also accepts the
environment variables of the same name, such as `FLAT2JAVA_DECIMAL_SEPARATOR`.

## Replacing the beans

The auto-configuration defines two beans, `Flat2JavaOptions` and `Flat2Java`, and backs off when
the application defines either one:

```java
@Bean
Flat2JavaOptions flat2JavaOptions() {
    return Flat2JavaOptions.builder().failOnStringTruncation(true).build();
}
```

To use several configurations at once, for example for two file layouts, define the extra
`Flat2Java` instances as qualified beans with `Flat2Java.create(options)`.
