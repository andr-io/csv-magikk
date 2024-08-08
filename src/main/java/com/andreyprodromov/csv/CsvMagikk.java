package com.andreyprodromov.csv;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvMagikk {
    private final char columnDelimiter;
    private final char stringDelimiter;

    private final String strDelimiter;
    private final String escapedStrDelimiter;
    private final String colDelimiter;

    private List<String> columnHeaders;

    // To test the CsvMagikk class
    public static void main(String[] args) {
        CsvMagikk csvMagikk = new CsvMagikk();
        String csv = "Name, Age, City\r\n" +
                "\"John Doe\", 30, \"New York\"\r\n" +
                "\"Jane Doe\", 25, \"Los Angeles\"\r\n" +
                "\"Alice\", 35, \"Chicago\"\r\n" +
                "\"Bob\", 40, \"Houston\"\r\n";

        if (csvMagikk.isValidCsv(csv)) {
            System.out.println("CSV is valid");
        } else {
            System.out.println("CSV is invalid");
        }

        String[][] parsedCsv = csvMagikk.parseCsv(csv);
        for (var row : parsedCsv) {
            for (var col : row) {
                System.out.print(col + " ");
            }
            System.out.println();
        }

        String csvString = csvMagikk.toCsv(parsedCsv);
        System.out.println(csvString);

        Path path = Path.of("src/main/resources/csv.csv");
        try {
            Files.writeString(path, csvString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CsvMagikk() {
        this(',', '"');
    }

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
        this.colDelimiter = String.valueOf(columnDelimiter);
    }

    public boolean isValidCsv(String csv) {
        return isValidCsv(csv, OutputStream.nullOutputStream(), false);
    }

    public boolean isValidCsv(String csv, boolean treatWarningsAsErrors) {
        return isValidCsv(csv, OutputStream.nullOutputStream(), treatWarningsAsErrors);
    }

    public boolean isValidCsv(String csv, OutputStream log) {
        return isValidCsv(csv, log, true);
    }

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

        StringBuilder sb = new StringBuilder();
        List<String[]> rows = new ArrayList<>();

        // Parse Csv
        while (idx < arr.length) {
            String[] buffer = new String[columnsCount];

            while (idx < arr.length && (arr[idx] != '\n' || !notInEscapedString)) {
                // If we reach a delimiter with an even number of quotes, then that means it is the end of a column
                if (notInEscapedString && arr[idx] == columnDelimiter) {
                    buffer[bufferIdx] = sb.toString();
                    bufferIdx++;
                    sb.setLength(0);
                    cellStartedWithRfc4180EscapedString = idx + 1 < arr.length && arr[idx + 1] == stringDelimiter;
                } else if (arr[idx] == stringDelimiter && cellStartedWithRfc4180EscapedString) {
                    notInEscapedString = !notInEscapedString;
                    if (notInEscapedString && idx + 1 < arr.length && arr[idx + 1] == stringDelimiter) {
                        sb.append(stringDelimiter);
                        idx++; // Skip next quote
                        notInEscapedString = false; // Because we move index forward we need to account for skipped quote
                    }
                } else {
                    // Only append delimiter and CR if we're in quoted text.
                    // Skips CR if at end of line.
                    if (arr[idx] != '\r' || !notInEscapedString) {
                        sb.append(arr[idx]);
                    }
                }

                idx++;
            }

            // Reached end of line, so we add the last column
            buffer[bufferIdx] = sb.toString();
            sb.setLength(0);

            // Add columns to list, reset buffer index, skip newline
            rows.add(buffer);
            bufferIdx = 0;
            idx++;

            // Check RFC4180
            cellStartedWithRfc4180EscapedString = idx < arr.length && arr[idx] == stringDelimiter;
        }

        return rows.toArray(String[][]::new);
    }

    public String toCsv(String[][] csv) {
        int columnCount = csv[0].length;
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < csv.length; row++) {
            for (int col = 0; col < columnCount; col++) {
                String cell = escape(csv[row][col]);
                sb.append(cell).append(columnDelimiter);
            }

            // Remove last delimiter and append CRLF
            sb.setLength(sb.length() - 1);
            sb.append("\r\n");
        }

        return sb.toString();
    }

    public String escape(String cell) {
        if (cell.indexOf(stringDelimiter) != -1) {
            cell = cell.replace(strDelimiter, escapedStrDelimiter);
            cell = strDelimiter + cell + strDelimiter;
        } else if (cell.indexOf('\r') != -1 || cell.indexOf('\n') != -1 || cell.indexOf(columnDelimiter) != -1) {
            cell = strDelimiter + cell + strDelimiter;
        }

        return cell;
    }

    public String toCsvRow(String[] columns) {
        StringBuilder sb = new StringBuilder();

        for (var col : columns) {
            sb.append(escape(col)).append(colDelimiter);
        }

        // Remove last delimiter and append CRLF
        sb.setLength(sb.length() - 1);
        sb.append("\r\n");

        return sb.toString();
    }

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

    // char[] arr must start with the first row of the csv
    private int calculateColumnsCountAndRecordColumns(char[] arr) {
        columnHeaders = new ArrayList<>();

        int idx = 0;
        int columnsCount = 1; // We start from one because bottom counter doesn't count last column
        StringBuilder currentColumn = new StringBuilder();

        // commas are a legal character when surrounded by apostrophes
        // e.g. "This is one, value".
        boolean evenNumberOfQuotes = true;

        while (arr[idx] != '\n' || !evenNumberOfQuotes) {
            if (arr[idx] == columnDelimiter && evenNumberOfQuotes) {
                columnHeaders.add(currentColumn.toString());
                currentColumn = new StringBuilder();

                columnsCount++;
            } else {
                currentColumn.append(arr[idx]);
            }

            if (arr[idx] == stringDelimiter) {
                evenNumberOfQuotes = !evenNumberOfQuotes;
            }

            idx++;
        }

        return columnsCount;
    }
}