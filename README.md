# CsvMagikk

A lightweight Java CSV parser and serializer.

## Features

- Parse CSV into `String[][]`
- Serialize `String[][]` and `List<String[]>` into CSV
- Escape individual CSV fields
- Support quoted fields
- Support escaped quotes
- Support commas and newlines inside quoted fields
- Support CRLF, CR, and LF row separators
- Custom delimiters
- CSV structure validation

## Requirements

- Java 17+

## Usage

Create a default instance:

<pre><code>CsvMagikk csv = CsvMagikk.create();</code></pre>

### Parse CSV

<pre><code>String input =
    "Name,Age,City\r\n" +
    "John,30,London\r\n" +
    "Jane,25,Paris";

String[][] result = csv.parseCsv(input);</code></pre>

Result:

<pre><code>[
    ["Name", "Age", "City"],
    ["John", "30", "London"],
    ["Jane", "25", "Paris"]
]</code></pre>

## Serialization

### Serialize a 2D Array

<pre><code>String[][] data = {
    {"Name", "Age", "City"},
    {"John", "30", "London"},
    {"Jane", "25", "Paris"}
};

String csvString = csv.toCsv(data);</code></pre>

Produces:

<pre><code>Name,Age,City\r\n
John,30,London\r\n
Jane,25,Paris\r\n</code></pre>

## Custom Delimiters

The default configuration uses `,` as the column delimiter and `"` as the string delimiter.

Custom delimiters can be configured:

<pre><code>CsvMagikk csv = CsvMagikk.create(';', '\'');</code></pre>

## Validation

Malformed CSV and rows with inconsistent column counts are rejected:

<pre><code>String input =
    "Name,Age,City\r\n" +
    "John,30,London\r\n" +
    "Jane,25";

csv.parseCsv(input);</code></pre>

This throws `IllegalArgumentException`.

## API

<pre><code>CsvMagikk.create();

CsvMagikk.create(char columnDelimiter, char stringDelimiter);

String[][] parseCsv(String csv);

String toCsv(String[][] csv);

String toCsv(List&lt;String[]&gt; csv);

String toCsvRow(String[] columns);

String escape(String cell);</code></pre>
