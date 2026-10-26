# flat2java-quarkus

CDI producers that make a `Flat2Java` bean available in Quarkus applications, configured from
`flat2java.*` properties. This is a plain CDI library, not a Quarkus extension. Quarkus discovers it
through the `beans.xml` in the jar.

```xml
<dependency>
    <groupId>io.github.allanhladki.flat2java</groupId>
    <artifactId>flat2java-quarkus</artifactId>
    <version>0.1.0</version>
</dependency>
```

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

## Properties

`application.properties`, with the default values:

```properties
flat2java.allow-null-strings=true
flat2java.null-value-padding= 
flat2java.fail-on-string-truncation=false
flat2java.decimal.use-separator=false
flat2java.decimal.separator=.
flat2java.decimal.rounding-mode=unnecessary
```

The keys are the same as in the Spring module and are described in the
[project README](../README.md#configuration). MicroProfile Config also maps environment variables
such as `FLAT2JAVA_DECIMAL_SEPARATOR` to these keys.

## Replacing the beans

`Flat2JavaOptions` and `Flat2Java` are produced as `@DefaultBean`s. A producer of either type in
the application takes precedence:

```java
@Produces
@Singleton
Flat2JavaOptions options() {
    return Flat2JavaOptions.builder().failOnStringTruncation(true).build();
}
```

## Native image

flat2java reads and writes fields through reflection. When building a native executable, register
every mapped class, including nested record classes, for reflection:

```java
@RegisterForReflection
public class Payment { ... }
```

Native compilation is not covered by this project's tests.
