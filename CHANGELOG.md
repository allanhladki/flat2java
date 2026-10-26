# Changelog

All notable changes to this project are documented in this file. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses
[Semantic Versioning](https://semver.org/).

## [0.1.0] - Unreleased

First public release. The API of the 0.0.1 release candidates was replaced.

### Changed

- Maven coordinates and packages moved to `io.github.allanhladki.flat2java`. The annotation package
  is now `io.github.allanhladki.flat2java.annotation`.
- Java 17 is required.
- `Flat2Java` is the single entry point, with `write`, `read` and `recordLength`. It replaces
  `ObjectSerializer`, `ObjectDeserializer`, `Flat2JavaFactory` and the `*Interface` types.
- Configuration is an immutable `Flat2JavaOptions` passed to `Flat2Java.create(options)`.
  Environment variables are read only by `Flat2JavaOptions.fromEnvironment()`, and invalid values
  fail instead of falling back to defaults. Several variables were renamed:
  `FLAT2JAVA_THROW_ON_STRING_TRUNCATION` → `FLAT2JAVA_FAIL_ON_STRING_TRUNCATION`,
  `FLAT2JAVA_BIGDECIMAL_USE_DECIMAL_SEPARATOR` → `FLAT2JAVA_DECIMAL_USE_SEPARATOR`,
  `FLAT2JAVA_BIGDECIMAL_DECIMAL_SEPARATOR` → `FLAT2JAVA_DECIMAL_SEPARATOR`,
  `FLAT2JAVA_BIGDECIMAL_ROUNDING_MODE` → `FLAT2JAVA_DECIMAL_ROUNDING_MODE`.
- Exceptions: `ValidationException` was renamed to `MappingException`. All errors are
  `Flat2JavaException` subclasses, whatever the entry point.
- Stricter mappings: `precision` only on `BigDecimal`, `padding` only on `String`,
  `dateTimeFormat` only on date types and required on them, no `static` fields.
- Spring: `@AutoConfiguration`, and the properties are applied (they were ignored before).
  Properties were renamed to `flat2java.fail-on-string-truncation` and
  `flat2java.decimal.{use-separator,separator,rounding-mode}`. Built against Spring Boot 3.3.
- Quarkus: a single `Flat2Java` bean, configured from MicroProfile Config.
- `flat2java-core` depends on `slf4j-api` only, and no longer ships `logback.xml` or depends on
  Logback. Log messages no longer contain field values.

### Added

- Nested records: a field whose type is a class with `@FlatField` fields.
- `@FlatField.position` for an explicit field order.
- `Flat2Java.recordLength(Class)`.
- Read errors include the column range of the failing field.
- The annotation processor checks positions, nested records, static fields and attributes on
  unsupported types.

### Removed

- `@FlatField.name`. Messages identify fields as `Class.field`.
- `@TranslateExceptions` and `ExceptionTranslationInterceptor`.
- `Flat2JavaConfig` (static configuration).

### Fixed

- `int` and `long` primitive fields could be written but not read.
- `BigDecimal` fields with `precision = 0` overflowed above `Integer.MAX_VALUE` and could write a
  wrong value without an error.
- Variable-width date patterns such as `d/M/yyyy` passed validation and truncated dates.
- Impossible dates such as `20240231` were adjusted instead of rejected.
- `BigDecimal` values with `precision = 0` were read in scientific notation (`1E+2`) when the
  decimal separator was enabled.
- A `null` `String` written with a custom `nullValuePadding` did not read back as `""`.
- Numeric columns accepted a leading `+`.
- The class layout and date formatters were rebuilt for every record. They are now built once per
  class.
