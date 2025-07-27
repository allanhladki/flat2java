# Flat2Java

A Java library for converting positional plain-text (fixed-length fields) to Java objects, similar to COBOL's data structures.

## Description

This library provides functionality to parse fixed-length text fields and convert them into Java objects. It's particularly useful when working with legacy systems or flat file formats that use fixed-length field definitions.

## Features

- Parse fixed-length text fields into Java objects
- Support for different data types (String, Integer, Date, etc.)
- Annotation-based field mapping
- Customizable field definitions
- Easy to use API

## Usage

```java
public class MyRecord {
    @FlatField(length = 10)
    private String id;

    @FlatField(length = 8, dateTimeFormat = "yyyyMMdd")
    private LocalDate date;

    @FlatField(length = 10, precision = 2)
    private BigDecimal amount;
}

// Extract field metadata in declaration order
List<FieldMetadata> metadata = FieldMetadataExtractor.extract(MyRecord.class);
```

**Note:** The order of fields is determined by their declaration order in the class (and superclasses, if using inheritance).

## Requirements

- Java 11 or higher
- Maven 3.6 or higher
- SLF4J with Logback for logging
- JUnit 5 for testing

## Installation

Add the following dependency to your project:

```xml
<dependency>
    <groupId>com.github.allan</groupId>
    <artifactId>flat2java</artifactId>
    <version>0.0.1-2</version>
</dependency>
```

## License

This project is licensed under the MIT License - see the LICENSE file for details. 