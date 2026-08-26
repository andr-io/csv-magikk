package com.andreyprodromov.csv;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides parsing, serialization, and escaping functionality for CSV data.
 *
 * <p>A {@code CsvMagikk} instance is configured with a column delimiter and a
 * string delimiter. By default, comma ({@code ,}) is used as the column
 * delimiter and double quote ({@code "}) is used as the string delimiter.</p>
 *
 * <p>Quoted fields may contain column delimiters, line breaks, and escaped
 * string delimiters. A string delimiter inside a quoted field is represented
 * by two consecutive string delimiters.</p>
 *
 * <p>For example, with the default configuration:</p>
 *
 * <pre>{@code
 * name,description
 * John,"Hello, world"
 * Jane,"She said ""hello"""
 * }</pre>
 *
 * <p>The parser accepts both CRLF ({@code \r\n}) and individual CR or LF
 * characters as row separators. Rows may also contain embedded line breaks
 * when those line breaks occur inside a quoted field.</p>
 */
public class CsvMagikk {

    private static final int INITIAL_COLUMN_COUNT = 16;

    // Logic related
    private final char columnDelimiter;
    private final char stringDelimiter;

    // Performance
    private final StringBuilder parserBuilder = new StringBuilder();
    private final StringBuilder escapedCellBuilder = new StringBuilder();
    private final StringBuilder toCsvBuilder = new StringBuilder();
    private final StringBuilder toCsvRowBuilder = new StringBuilder();

    private CsvMagikk(char columnDelimiter, char stringDelimiter) {
        if (columnDelimiter == stringDelimiter) {
            throw new IllegalStateException("Cannot have same columnDelimiter and stringDelimiter");
        }

        if (columnDelimiter == '\r') {
            throw new IllegalStateException("Cannot use CR for columnDelimiter");
        }

        if (columnDelimiter == '\n') {
            throw new IllegalStateException("Cannot use LF for columnDelimiter");
        }

        if (stringDelimiter == '\r') {
            throw new IllegalStateException("Cannot use CR for stringDelimiter");
        }

        if (stringDelimiter == '\n') {
            throw new IllegalStateException("Cannot use LF for stringDelimiter");
        }

        this.columnDelimiter = columnDelimiter;
        this.stringDelimiter = stringDelimiter;
    }

    /**
     * Parses a CSV string into a two-dimensional array of strings.
     *
     * <p>Each CSV row is represented by a {@code String[]} and the returned
     * {@code String[][]} contains all parsed rows.</p>
     *
     * <p>Quoted fields are supported. A quoted field may contain the configured
     * column delimiter, CR/LF characters, and escaped string delimiters. Two
     * consecutive string delimiters inside a quoted field are interpreted as
     * one literal string delimiter.</p>
     *
     * <p>All rows must contain the same number of columns. If a row contains a
     * different number of columns, an {@link IllegalArgumentException} is thrown.</p>
     *
     * <p>The final row does not need to end with a line separator.</p>
     *
     * @param csv the CSV string to parse
     * @return a two-dimensional array containing the parsed rows and columns
     * @throws IllegalArgumentException if {@code csv} is {@code null}, blank,
     *         malformed, contains an unterminated quoted field, or contains rows
     *         with different numbers of columns
     */
    public String[][] parseCsv(String csv) {
        if (csv == null) {
            throw new IllegalArgumentException("CSV string cannot be null");
        }

        if (csv.isBlank()) {
            throw new IllegalArgumentException("Cannot parse a blank file");
        }

        // Edge case
        if (csv.equals("\"\"")) {
            return new String[][] {
                new String[]{""}
            };
        }

        parserBuilder.setLength(0);

        char[] chars = csv.toCharArray();
        int length = chars.length;
        int index = 0;

        List<String[]> rows = new ArrayList<>();

        String[] currentRow = new String[INITIAL_COLUMN_COUNT];
        int columnIndex = 0;
        int expectedColumnCount = -1;

        boolean inQuotes = false;

        while (index < length) {
            char c = chars[index];

            if (inQuotes) {
                if (c == stringDelimiter) {
                    // Escaped quote: "" -> "
                    if (index + 1 < length && chars[index + 1] == stringDelimiter) {
                        parserBuilder.append(stringDelimiter);
                        index += 2;
                        continue;
                    }

                    // Closing quote
                    inQuotes = false;
                    index++;

                    // After a closing quote we only allow:
                    // delimiter, newline, or end of input.
                    if (index < length) {
                        c = chars[index];

                        if (c != columnDelimiter && c != '\r' && c != '\n') {
                            throw new IllegalArgumentException(
                                "The CSV file is malformed"
                            );
                        }
                    }

                    continue;
                }

                // Append a whole block until the next quote.
                int start = index;

                while (index < length && chars[index] != stringDelimiter) {
                    index++;
                }

                parserBuilder.append(chars, start, index - start);
                continue;
            }

            // Opening quote
            if (c == stringDelimiter) {
                inQuotes = true;
                index++;
                continue;
            }

            // End of column
            if (c == columnDelimiter) {
                if (columnIndex == currentRow.length) {
                    currentRow = java.util.Arrays.copyOf(
                        currentRow,
                        currentRow.length * 2
                    );
                }

                currentRow[columnIndex++] = parserBuilder.toString();
                parserBuilder.setLength(0);

                index++;
                continue;
            }

            // End of row
            if (c == '\r' || c == '\n') {
                if (columnIndex == currentRow.length) {
                    currentRow = java.util.Arrays.copyOf(
                        currentRow,
                        currentRow.length * 2
                    );
                }

                currentRow[columnIndex++] = parserBuilder.toString();
                parserBuilder.setLength(0);

                // Check column count.
                if (expectedColumnCount == -1) {
                    expectedColumnCount = columnIndex;
                } else if (columnIndex != expectedColumnCount) {
                    throw new IllegalArgumentException(
                        "Row has " + columnIndex
                            + " columns, expected " + expectedColumnCount
                    );
                }

                rows.add(
                    columnIndex == currentRow.length
                        ? currentRow
                        : java.util.Arrays.copyOf(currentRow, columnIndex)
                );

                currentRow = new String[currentRow.length];
                columnIndex = 0;

                // CRLF is one newline.
                if (c == '\r'
                    && index + 1 < length
                    && chars[index + 1] == '\n') {
                    index += 2;
                } else {
                    index++;
                }

                continue;
            }

            // Ordinary text.
            // Find the next CSV control character and append the entire block.
            int start = index;

            while (index < length) {
                c = chars[index];

                if (c == stringDelimiter
                    || c == columnDelimiter
                    || c == '\r'
                    || c == '\n') {
                    break;
                }

                index++;
            }

            parserBuilder.append(chars, start, index - start);
        }

        if (inQuotes) {
            throw new IllegalArgumentException(
                "CSV contains an unterminated quoted field"
            );
        }

        // Handle a final row without a trailing newline.
        if (columnIndex > 0 || !parserBuilder.isEmpty()) {
            if (columnIndex == currentRow.length) {
                currentRow = java.util.Arrays.copyOf(
                    currentRow,
                    currentRow.length * 2
                );
            }

            currentRow[columnIndex++] = parserBuilder.toString();

            // Check final row.
            if (expectedColumnCount != -1 && columnIndex != expectedColumnCount) {
                throw new IllegalArgumentException(
                    "Row has " + columnIndex
                        + " columns, expected " + expectedColumnCount
                );
            }

            rows.add(
                columnIndex == currentRow.length
                    ? currentRow
                    : java.util.Arrays.copyOf(currentRow, columnIndex)
            );
        }

        return rows.toArray(String[][]::new);
    }

    /**
     * Serializes a two-dimensional array of CSV data into a CSV string.
     *
     * <p>Every row is terminated with CRLF ({@code \r\n}). Fields containing the
     * column delimiter, string delimiter, CR, or LF are automatically quoted.
     * String delimiters inside quoted fields are escaped by doubling them.</p>
     *
     * <p>For example, the value {@code Hello, "world"} becomes:</p>
     *
     * <pre>{@code
     * "Hello, ""world"""
     * }</pre>
     *
     * @param csv the rows and columns to serialize
     * @return the CSV representation of the supplied data
     * @throws IllegalArgumentException if {@code csv} or any row is {@code null},
     *         or if a row contains no columns
     */
    public String toCsv(String[][] csv) {
        if (csv == null) {
            throw new IllegalArgumentException("CSV cannot be null");
        }

        toCsvBuilder.setLength(0);

        for (String[] row : csv) {
            appendCsvRow(row, toCsvBuilder);
        }

        return toCsvBuilder.toString();
    }

    /**
     * Serializes a list of CSV rows into a CSV string.
     *
     * <p>Every row is terminated with CRLF ({@code \r\n}). Fields containing the
     * column delimiter, string delimiter, CR, or LF are automatically quoted.
     * String delimiters inside quoted fields are escaped by doubling them.</p>
     *
     * @param csv the list of rows to serialize
     * @return the CSV representation of the supplied rows
     * @throws IllegalArgumentException if {@code csv} or any row is {@code null},
     *         or if a row contains no columns
     */
    public String toCsv(List<String[]> csv) {
        if (csv == null) {
            throw new IllegalArgumentException("CSV cannot be null");
        }

        toCsvBuilder.setLength(0);

        for (String[] row : csv) {
            appendCsvRow(row, toCsvBuilder);
        }

        return toCsvBuilder.toString();
    }

    private void appendCsvRow(String[] row, StringBuilder builder) {
        if (row == null) {
            throw new IllegalArgumentException("CSV row cannot be null");
        }

        if (row.length == 0) {
            throw new IllegalArgumentException("CSV row cannot be empty");
        }

        for (int i = 0; i < row.length - 1; i++) {
            appendEscaped(row[i], builder);
            builder.append(columnDelimiter);
        }

        appendEscaped(row[row.length - 1], builder);

        builder.append('\r').append('\n');
    }

    /**
     * Serializes a single row into a CSV string.
     *
     * <p>Fields are separated using the configured column delimiter and the row
     * is terminated with CRLF ({@code \r\n}). Fields that require quoting are
     * automatically escaped.</p>
     *
     * @param columns the values to serialize as one CSV row
     * @return the serialized CSV row, including its CRLF terminator
     * @throws IllegalArgumentException if {@code columns} is {@code null},
     *         empty, or contains a {@code null} value
     */
    public String toCsvRow(String[] columns) {
        if (columns == null) {
            throw new IllegalArgumentException("CSV columns cannot be null");
        }

        if (columns.length == 0) {
            throw new IllegalArgumentException("CSV row cannot be empty");
        }

        toCsvRowBuilder.setLength(0);

        for (int i = 0; i < columns.length - 1; i++) {
            appendEscaped(columns[i], toCsvRowBuilder);
            toCsvRowBuilder.append(columnDelimiter);
        }

        appendEscaped(columns[columns.length - 1], toCsvRowBuilder);

        toCsvRowBuilder.append('\r').append('\n');

        return toCsvRowBuilder.toString();
    }

    /**
     * @param cell the cell to be escaped
     * @param out the builder where the escape cell will be appended
     */
    private void appendEscaped(String cell, StringBuilder out) {
        if (cell == null) {
            throw new IllegalArgumentException("CSV cell cannot be null");
        }

        int length = cell.length();

        for (int i = 0; i < length; i++) {
            char c = cell.charAt(i);

            if (c != stringDelimiter
                && c != columnDelimiter
                && c != '\r'
                && c != '\n') {
                continue;
            }

            out.append(stringDelimiter);
            out.append(cell, 0, i);

            for (; i < length; i++) {
                c = cell.charAt(i);

                if (c == stringDelimiter) {
                    out.append(stringDelimiter);
                }

                out.append(c);
            }

            out.append(stringDelimiter);
            return;
        }

        out.append(cell);
    }

    /**
     * Escapes a single value for use as a CSV field.
     *
     * <p>A value is enclosed in the configured string delimiter when it contains
     * the column delimiter, string delimiter, CR, or LF. String delimiters
     * contained within the value are doubled.</p>
     *
     * <p>For example, with the default delimiters:</p>
     *
     * <pre>{@code
     * escape("Hello, world")       -> "\"Hello, world\""
     * escape("She said \"Hi\"")    -> "\"She said \"\"Hi\"\"\""
     * escape("Hello")              -> "Hello"
     * }</pre>
     *
     * @param cell the value to escape
     * @return the escaped CSV field
     * @throws IllegalArgumentException if {@code cell} is {@code null}
     */
    public String escape(String cell) {
        escapedCellBuilder.setLength(0);
        appendEscaped(cell, escapedCellBuilder);
        return escapedCellBuilder.toString();
    }

    /**
     * Creates a CSV processor using comma as the column delimiter and double quote
     * as the string delimiter.
     *
     * @return a CSV processor configured for standard comma-separated values
     */
    public static CsvMagikk create() {
        return create(',', '"');
    }

    /**
     * Creates a CSV processor with custom delimiters.
     *
     * <p>The column delimiter separates fields and the string delimiter identifies
     * quoted fields. Neither delimiter may be CR or LF, and the two delimiters
     * must be different.</p>
     *
     * @param columnDelimiter the character used to separate columns
     * @param stringDelimiter the character used to quote and escape fields
     * @return a CSV processor configured with the supplied delimiters
     * @throws IllegalStateException if the delimiters are equal, or if either
     *         delimiter is CR or LF
     */
    public static CsvMagikk create(char columnDelimiter, char stringDelimiter) {
        return new CsvMagikk(columnDelimiter, stringDelimiter);
    }
}