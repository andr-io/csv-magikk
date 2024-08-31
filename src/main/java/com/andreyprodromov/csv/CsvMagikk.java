package com.andreyprodromov.csv;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


/**
 * This class provides functionality for manipulation and validation of csv Strings.
 */
public class CsvMagikk {

    // Logic related
    private final char columnDelimiter;
    private final char stringDelimiter;

    // Performance and cache related
    private final String strDelimiter;
    private final String escapedStrDelimiter;
    private final StringBuilder parserBuilder = new StringBuilder();
    private final StringBuilder escapedCellBuilder = new StringBuilder();
    private final StringBuilder toCsvBuilder = new StringBuilder();
    private final StringBuilder toCsvRowBuilder = new StringBuilder();


    /**
     * Creates a default object that has a COMMA for column delimiter and DOUBLE QUOTE for string delimiter
     */
    public CsvMagikk() {
        this(',', '"');
    }

    /**
     * @param columnDelimiter the column delimiter to be used with parsing, creating or validating csv Strings
     * @param stringDelimiter the string delimiter to be used with parsing, creating or validating csv Strings
     */
    public CsvMagikk(char columnDelimiter, char stringDelimiter) {
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

        this.strDelimiter = String.valueOf(stringDelimiter);
        this.escapedStrDelimiter = strDelimiter + strDelimiter;
    }

    /**
     * @param csv the csv String to check
     * @return true if file is RFC 4180 compliant
     */
    public boolean isValidCsv(String csv) {
        return isValidCsv(csv, OutputStream.nullOutputStream(), false);
    }

    public boolean isValidCsv(String csv, boolean treatWarningsAsErrors) {
        return isValidCsv(csv, OutputStream.nullOutputStream(), treatWarningsAsErrors);
    }

    /**
     * @param csv the csv String to check
     * @param log the output where errors and warnings to be printer
     * @return true if file is RFC 4180 compliant and has no warnings
     */
    public boolean isValidCsv(String csv, OutputStream log) {
        return isValidCsv(csv, log, true);
    }

    /**
     * @param csv the csv String to check
     * @param log the output where errors and warnings to be printer
     * @param treatWarningsAsErrors if warnings should be treated as errors
     * @return true if file is RFC 4180 compliant
     */
    public boolean isValidCsv(String csv, OutputStream log, boolean treatWarningsAsErrors) {
        PrintStream out = new PrintStream(log);
        boolean hasErrors = false;
        boolean hasWarnings = false;

        if (csv == null) {
            out.println("ERROR: csv is null");
            return false;
        }

        if (csv.isBlank()) {
            out.println("ERROR: csv is blank");
            return false;
        }

        char[] arr = csv.toCharArray();
        int headerColumnCount = calculateColumnsCount(arr);
        int idx = 0;
        int currentColumnCount = 0;
        boolean notInEscapedString = true;
        int rowNumber = 1;
        boolean cellStartedWithRfc4180EscapedString = false;

        while (idx < arr.length) {
            while (idx < arr.length && (arr[idx] != '\n' || !notInEscapedString)) {
                // If we reach a delimiter with an even number of quotes, then that means it is the end of a column
                if (arr[idx] == columnDelimiter && notInEscapedString) {
                    if (cellStartedWithRfc4180EscapedString && idx > 0 && arr[idx - 1] != stringDelimiter) {
                        out.printf(
                                "ERROR: row number %d has a column that started with opening quote, but didn't use closing quote%n",
                                rowNumber
                        );

                        hasErrors = true;
                    }

                    currentColumnCount++;
                    cellStartedWithRfc4180EscapedString = idx + 1 < arr.length && arr[idx + 1] == stringDelimiter;
                } else if (arr[idx] == stringDelimiter) {
                    if (!cellStartedWithRfc4180EscapedString) {
                        out.printf(
                                "ERROR: row number %d appears to use quotes without enclosing field in quotes%n",
                                rowNumber
                        );

                        hasErrors = true;
                    } else {
                        notInEscapedString = !notInEscapedString;
                        if (notInEscapedString && idx + 1 < arr.length && arr[idx + 1] == stringDelimiter) {
                            idx++; // Skip next quote
                            notInEscapedString = false; // Because we move index forward we need to account for skipped quote
                        }
                    }
                }

                if (idx < arr.length && idx + 1 < arr.length && arr[idx] == '\r' && arr[idx + 1] != '\n') {
                    out.printf(
                            "WARNING: row number %d uses CR without LF%n",
                            rowNumber
                    );

                    hasWarnings = true;
                }

                idx++;
            }

            // Reached end of line, so we add the last column
            currentColumnCount++;

            if (currentColumnCount != headerColumnCount) {
                out.printf(
                        "ERROR: row number %d has different number of columns (Expected: %d, Actual: %d)%n",
                        rowNumber,
                        headerColumnCount,
                        currentColumnCount
                );

                hasErrors = true;
            }

            if (!notInEscapedString) {
                out.printf(
                        "ERROR: last column in row number %d does not have properly escaped quotes%n",
                        rowNumber
                );

                hasErrors = true;
            }

            // Prepare for next csv row
            currentColumnCount = 0;
            idx++;
            rowNumber++;

            if (idx + 1 < arr.length && (arr[idx + 1] == '\r' || arr[idx + 1] == '\n')) {
                out.printf(
                        "WARNING: row number %d appears to have more than one newline%n",
                        rowNumber
                );

                hasWarnings = true;
            }

            // Check RFC4180
            cellStartedWithRfc4180EscapedString = idx < arr.length && arr[idx] == stringDelimiter;
        }

        return !(hasErrors || (treatWarningsAsErrors && hasWarnings));
    }

    /**
     * @param csv the csv to be parsed
     * @return a {@code String[][]} matrix created from parsing the file
     */
    public String[][] parseCsv(String csv) {
        if (csv == null) {
            throw new RuntimeException("Csv string cannot be null");
        }

        if (csv.isBlank()) {
            throw new RuntimeException("Cannot parse a blank file");
        }

        char[] arr = csv.toCharArray();
        int columnsCount = calculateColumnsCount(arr);
        int idx = 0;
        int bufferIdx = 0;
        boolean notInEscapedString = true;
        boolean cellStartedWithRfc4180EscapedString = true;

        parserBuilder.setLength(0);
        List<String[]> rows = new ArrayList<>();

        // Parse Csv
        while (idx < arr.length) {
            String[] buffer = new String[columnsCount];

            while (idx < arr.length && (arr[idx] != '\n' || !notInEscapedString)) {
                // If we reach a delimiter with an even number of quotes, then that means it is the end of a column
                if (notInEscapedString && arr[idx] == columnDelimiter) {
                    buffer[bufferIdx] = parserBuilder.toString();
                    bufferIdx++;
                    parserBuilder.setLength(0);
                    cellStartedWithRfc4180EscapedString = idx + 1 < arr.length && arr[idx + 1] == stringDelimiter;
                } else if (arr[idx] == stringDelimiter && cellStartedWithRfc4180EscapedString) {
                    notInEscapedString = !notInEscapedString;
                    if (notInEscapedString && idx + 1 < arr.length && arr[idx + 1] == stringDelimiter) {
                        parserBuilder.append(stringDelimiter);
                        idx++; // Skip next quote
                        notInEscapedString = false; // Because we move index forward we need to account for skipped quote
                    }
                } else {
                    // Only append delimiter and CR if we're in quoted text.
                    // Skips CR if at end of line.
                    if (arr[idx] != '\r' || !notInEscapedString) {
                        parserBuilder.append(arr[idx]);
                    }
                }

                idx++;
            }

            // Reached end of line, so we add the last column
            buffer[bufferIdx] = parserBuilder.toString();
            parserBuilder.setLength(0);

            // Add columns to list, reset buffer index, skip newline
            rows.add(buffer);
            bufferIdx = 0;
            idx++;

            // Check RFC4180
            cellStartedWithRfc4180EscapedString = idx < arr.length && arr[idx] == stringDelimiter;
        }

        return rows.toArray(String[][]::new);
    }

    /**
     * @param csv the csv matrix to be parsed
     * @return the {@code String} csv created from parsing the csv matrix
     */
    public String toCsv(String[][] csv) {
        int columnCount = csv[0].length;
        toCsvBuilder.setLength(0);

        for (String[] strings : csv) {
            for (int col = 0; col < columnCount; col++) {
                String cell = escape(strings[col]);
                toCsvBuilder.append(cell)
                            .append(columnDelimiter);
            }

            // Remove last delimiter and append CRLF
            toCsvBuilder.setLength(toCsvBuilder.length() - 1);
            toCsvBuilder.append("\r\n");
        }

        return toCsvBuilder.toString();
    }

    /**
     * @param columns the columns to be joined and escaped, \r\n is appended at end
     * @return the {@code String} csv row
     */
    public String toCsvRow(String[] columns) {
        toCsvRowBuilder.setLength(0);

        for (var col : columns) {
            toCsvRowBuilder.append(escape(col))
                           .append(columnDelimiter);
        }

        // Remove last delimiter and append CRLF
        toCsvRowBuilder.setLength(toCsvRowBuilder.length() - 1);
        toCsvRowBuilder.append("\r\n");

        return toCsvRowBuilder.toString();
    }

    /**
     * @param cell the cell to be escaped
     * @return the {@code String} escaped cell
     */
    public String escape(String cell) {
        escapedCellBuilder.setLength(0);

        if (cell.indexOf(stringDelimiter) != -1) {
            String escapedStringDelimiter = cell.replace(strDelimiter, escapedStrDelimiter);
            return escapedCellBuilder.append(strDelimiter)
                                     .append(escapedStringDelimiter)
                                     .append(strDelimiter)
                                     .toString();
        } else if (cell.indexOf('\n') != -1 || cell.indexOf(columnDelimiter) != -1 || cell.indexOf('\r') != -1) {
            return escapedCellBuilder.append(strDelimiter)
                                     .append(cell)
                                     .append(strDelimiter)
                                     .toString();
        }

        return cell;
    }


    /**
     * @param arr the csv String as an array
     * @return the number of columns the csv file has
     */
    private int calculateColumnsCount(char[] arr) {
        int idx = 0;
        int columnsCount = 1; // We start from one because bottom counter doesn't count last column
        boolean evenNumberOfQuotes = true;
        while (arr[idx] != '\n' || !evenNumberOfQuotes) {
            if (arr[idx] == columnDelimiter && evenNumberOfQuotes) {
                columnsCount++;
            }

            if (arr[idx] == stringDelimiter) {
                evenNumberOfQuotes = !evenNumberOfQuotes;
            }

            idx++;
        }

        return columnsCount;
    }
}