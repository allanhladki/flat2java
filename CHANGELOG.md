# Changelog

All notable changes to this project are documented in this file. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses
[Semantic Versioning](https://semver.org/).

## [0.1.0] - 2026-10-26

Initial release. Requires Java 17.

### Added

**Core (`flat2java-core`)**

- `Flat2Java`, an immutable and thread-safe entry point with `write`, `read` and `recordLength`.
  The mapping of each class is validated on first use and cached per instance.
- `@FlatField` to map a field to a fixed-width column, with `length`, `position`, `precision`,
  `dateTimeFormat`, `padding` and `occurs`.
- Field types: `String`, `int`/`Integer`, `long`/`Long`, `BigInteger`, `BigDecimal` with implied or
  explicit decimal places, `LocalDate` and `LocalDateTime` with fixed-width patterns and strict
  parsing.
- Nested records: a field whose type is a class or record with `@FlatField` fields is written
  inline. Its `length` must equal the sum of the nested fields.
- `List` and array fields with a fixed number of elements (`occurs`), of any supported type
  including nested records.
- Java records, read through their canonical constructor.
- Superclass fields are included, superclass first. `position` sets an explicit field order.
- `Flat2JavaOptions` for null handling, string truncation and decimal formatting, built with a
  builder or read from `FLAT2JAVA_*` environment variables with `fromEnvironment()`.
- Unchecked exceptions under `Flat2JavaException`: `MappingException`, `SerializationException` and
  `DeserializationException`. Read errors give the column range of the failing field, and no
  message includes field values.

**Annotation processor (`flat2java-processor`)**

- Reports invalid mappings at compile time with the same rules and messages as the runtime.

**Spring (`flat2java-spring`)**

- Spring Boot 3 auto-configuration of `Flat2JavaOptions` and `Flat2Java` beans from `flat2java.*`
  properties.

**Quarkus (`flat2java-quarkus`)**

- CDI producers of `Flat2JavaOptions` and `Flat2Java` beans configured from MicroProfile Config.

[0.1.0]: https://github.com/allanhladki/flat2java/releases/tag/v0.1.0
