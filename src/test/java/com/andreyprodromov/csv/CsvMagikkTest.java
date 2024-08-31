package com.andreyprodromov.csv;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvMagikkTest {

    CsvMagikk csvMagikk = new CsvMagikk();

    @Test
    void escapeTextWithQuotesAndCommasTest() {
        String input = "\"John, Mary and Sue went to \"Awesome Productions\" theater";

        String expected = "\"\"\"John, Mary and Sue went to \"\"Awesome Productions\"\" theater\"";
        String actual = csvMagikk.escape(input);

        assertEquals(expected, actual, "CsvMagikk does not escape string delimiters or cell delimiters properly");
    }

    @Test
    void escapeTextWithNewlinesTest() {
        String input = "John, Mary and Sue went to\r\ntheater";

        String expected = "\"John, Mary and Sue went to\r\ntheater\"";
        String actual = csvMagikk.escape(input);

        assertEquals(expected, actual, "CsvMagikk does not escape cells with newlines properly");
    }

    @Test
    void simpleParseExtractingDataCorrectlyFromCSVWithoutSpecialGimmicksTest() {
        String csv = "Name,Age,City\r\n" +
                     "John Doe,30,New York\r\n" +
                     "Jane Doe,25,Los Angeles\r\n" +
                     "Alice,35,Chicago\r\n" +
                     "Bob,40,Houston\r\n";

        String[][] extractedCsv = csvMagikk.parseCsv(csv);

        // Headers
        assertEquals("Name", extractedCsv[0][0], "Couldn't extract header row properly");
        assertEquals("Age", extractedCsv[0][1], "Couldn't extract header row properly");
        assertEquals("City", extractedCsv[0][2], "Couldn't extract header row properly");

        // First row
        assertEquals("John Doe", extractedCsv[1][0], "Couldn't extract first data row properly");
        assertEquals("30", extractedCsv[1][1], "Couldn't extract first data row properly");
        assertEquals("New York", extractedCsv[1][2], "Couldn't extract first data row properly");

        // Second row
        assertEquals("Jane Doe", extractedCsv[2][0], "Couldn't extract second data row properly");
        assertEquals("25", extractedCsv[2][1], "Couldn't extract second data row properly");
        assertEquals("Los Angeles", extractedCsv[2][2], "Couldn't extract second data row properly");

        // Third row
        assertEquals("Alice", extractedCsv[3][0], "Couldn't extract third data row properly");
        assertEquals("35", extractedCsv[3][1], "Couldn't extract third data row properly");
        assertEquals("Chicago", extractedCsv[3][2], "Couldn't extract third data row properly");

        // Fourth row
        assertEquals("Bob", extractedCsv[4][0], "Couldn't extract fourth data row properly");
        assertEquals("40", extractedCsv[4][1], "Couldn't extract fourth data row properly");
        assertEquals("Houston", extractedCsv[4][2], "Couldn't extract fourth data row properly");
    }

    @Test
    void simpleParseExtractingHeadersCorrectlyTest() {
        String csv = """
            id,name,location
            1,John Doe,New York
            2,James Doe,\"\"\"New\"\" York\"
            3,Mary Doe,\"New, York\"
            4,Jack Doe,\"\"\"New\"\", York\"
            5,\"\"\"Jane Doe\",\",\"\"New\"\", York\"
            6,\"\"\"Jim Doe\",\",\"\"New\"\",\r\n York\"
            """;

        String[][] extractedCsv = csvMagikk.parseCsv(csv);

        // Headers
        assertEquals("id", extractedCsv[0][0], "Couldn't extract header row properly");
        assertEquals("name", extractedCsv[0][1], "Couldn't extract header row properly");
        assertEquals("location", extractedCsv[0][2], "Couldn't extract header row properly");
    }

    @Test
    void simpleParseExtractingSimpleRowTest() {
        String csv = """
            id,name,location
            1,John Doe,New York
            2,James Doe,\"\"\"New\"\" York\"
            3,Mary Doe,\"New, York\"
            4,Jack Doe,\"\"\"New\"\", York\"
            5,\"\"\"Jane Doe\",\",\"\"New\"\", York\"
            6,\"\"\"Jim Doe\",\",\"\"New\"\",\r\n York\"
            """;

        String[][] extractedCsv = csvMagikk.parseCsv(csv);

        // First row without any csv gimmicks
        assertEquals("1", extractedCsv[1][0], "Couldn't extract first data row properly");
        assertEquals("John Doe", extractedCsv[1][1], "Couldn't extract first data row properly");
        assertEquals("New York", extractedCsv[1][2], "Couldn't extract first data row properly");
    }

    @Test
    void parseWhenHavingQuotesInNameTest() {
        String csv = """
            id,name,location
            1,John Doe,New York
            2,James Doe,\"\"\"New\"\" York\"
            3,Mary Doe,\"New, York\"
            4,Jack Doe,\"\"\"New\"\", York\"
            5,\"\"\"Jane Doe\",\",\"\"New\"\", York\"
            6,\"\"\"Jim Doe\",\",\"\"New\"\",\r\n York\"
            """;

        String[][] extractedCsv = csvMagikk.parseCsv(csv);

        // Second row with quotes -> "New" York
        assertEquals("2", extractedCsv[2][0], "Couldn't extract second data row properly");
        assertEquals("James Doe", extractedCsv[2][1], "Couldn't extract second data row properly");
        assertEquals("\"New\" York", extractedCsv[2][2], "Couldn't extract second data row properly. Quotes could be a potential problem");
    }

    @Test
    void parseWhenHavingCommasInCellTest() {
        String csv = """
            id,name,location
            1,John Doe,New York
            2,James Doe,\"\"\"New\"\" York\"
            3,Mary Doe,\"New, York\"
            4,Jack Doe,\"\"\"New\"\", York\"
            5,\"\"\"Jane Doe\",\",\"\"New\"\", York\"
            6,\"\"\"Jim Doe\",\",\"\"New\"\",\r\n York\"
            """;

        String[][] extractedCsv = csvMagikk.parseCsv(csv);

        // Third row with commas -> New, York
        assertEquals("3", extractedCsv[3][0], "Couldn't extract third data row properly");
        assertEquals("Mary Doe", extractedCsv[3][1], "Couldn't extract third data row properly");
        assertEquals("New, York", extractedCsv[3][2], "Couldn't extract third data row properly. Commas could be a potential problem");
    }

    @Test
    void parseWhenHavingQuotesAndCommasInCellTest() {
        String csv = """
            id,name,location
            1,John Doe,New York
            2,James Doe,\"\"\"New\"\" York\"
            3,Mary Doe,\"New, York\"
            4,Jack Doe,\"\"\"New\"\", York\"
            5,\"\"\"Jane Doe\",\",\"\"New\"\", York\"
            6,\"\"\"Jim Doe\",\",\"\"New\"\",\r\n York\"
            """;

        String[][] extractedCsv = csvMagikk.parseCsv(csv);

        // Fourth with quotes and commas -> "New", York
        assertEquals("4", extractedCsv[4][0], "Couldn't extract fourth data row properly");
        assertEquals("Jack Doe", extractedCsv[4][1], "Couldn't extract fourth data row properly");
        assertEquals("\"New\", York", extractedCsv[4][2], "Couldn't extract fourth data row properly");

        // Fifth with quotes and commas -> "Jane Doe -> ,"New", York
        assertEquals("5", extractedCsv[5][0], "Couldn't extract fifth data row properly");
        assertEquals("\"Jane Doe", extractedCsv[5][1], "Couldn't extract fifth data row properly");
        assertEquals(",\"New\", York", extractedCsv[5][2], "Couldn't extract fifth data row properly");
    }

    @Test
    void parseWhenHavingQuotesNewlinesAndCommasInCellTest() {
        String csv = """
            id,name,location
            1,John Doe,New York
            2,James Doe,\"\"\"New\"\" York\"
            3,Mary Doe,\"New, York\"
            4,Jack Doe,\"\"\"New\"\", York\"
            5,\"\"\"Jane Doe\",\",\"\"New\"\", York\"
            6,\"\"\"Jim Doe\",\",\"\"New\"\",\r\n York\"
            """;

        String[][] extractedCsv = csvMagikk.parseCsv(csv);

        // Sixth with quotes, commas and newlines  -> Jim Doe, "New",\r\n York
        assertEquals("6", extractedCsv[6][0], "Couldn't extract sixth data row properly");
        assertEquals("\"Jim Doe", extractedCsv[6][1], "Couldn't extract sixth data row properly");
        assertEquals(",\"New\",\r\n York", extractedCsv[6][2], "Couldn't extract sixth data row properly");
    }

    @Test
    void validationWhenCSVIsValidTest() {
        String csv = "Name,Age,City\r\n" +
                     "John Doe,30,New York\r\n" +
                     "Jane Doe,25,Los Angeles\r\n" +
                     "Alice,35,Chicago\r\n" +
                     "Bob,40,Houston\r\n";

        boolean validationResult = csvMagikk.isValidCsv(csv, true);
        assertTrue(validationResult, "Did not return true on valid csv");
    }

    @Test
    void validationWhenCSVIsValidAndContainsSpecialSymbolsTest() {
        String csv = """
            id,name,location
            1,John Doe,New York
            2,James Doe,\"\"\"New\"\" York\"
            3,Mary Doe,\"New, York\"
            4,Jack Doe,\"\"\"New\"\", York\"
            5,\"\"\"Jane Doe\",\",\"\"New\"\", York\"
            6,\"\"\"Jim Doe\",\",\"\"New\"\",\r\n York\"
            """;

        boolean validationResult = csvMagikk.isValidCsv(csv, true);
        assertTrue(validationResult, "Did not return true on valid csv");
    }

    @Test
    void validationWhenCSVIsInvalidExtraCommaTest() {
        // Has one extra comma on first row
        String csv = """
            id,name,location,
            1,John Doe,New York
            2,James Doe,\"\"\"New\"\" York\"
            3,Mary Doe,\"New, York\"
            4,Jack Doe,\"\"\"New\"\", York\"
            5,\"\"\"Jane Doe\",\",\"\"New\"\", York\"
            6,\"\"\"Jim Doe\",\",\"\"New\"\",\r\n York\"
            """;

        boolean validationResult = csvMagikk.isValidCsv(csv, true);
        assertFalse(validationResult, "Did not return false when having an extra comma in headers");
    }

    @Test
    void validationWhenCSVIsInvalidNotQuotedAndEscapedProperlyTest() {
        // Second row uses quotes, but they are not properly escaped
        String csv = """
            id,name,location,
            1,John Doe,New York
            2,James Doe,\"New\" York
            3,Mary Doe,\"New, York\"
            4,Jack Doe,\"\"\"New\"\", York\"
            5,\"\"\"Jane Doe\",\",\"\"New\"\", York\"
            6,\"\"\"Jim Doe\",\",\"\"New\"\",\r\n York\"
            """;

        boolean validationResult = csvMagikk.isValidCsv(csv, true);
        assertFalse(validationResult, "Did not return false when having improperly escaped cell and quotes");
    }

    @Test
    void validationWhenCSVIsInvalidNotEscapedProperlyTest() {
        // Second row uses properly escaped quotes, but field is not enclosed in quotes
        String csv = """
            id,name,location,
            1,John Doe,New York
            2,James Doe,\"\"New\"\" York
            3,Mary Doe,\"New, York\"
            4,Jack Doe,\"\"\"New\"\", York\"
            5,\"\"\"Jane Doe\",\",\"\"New\"\", York\"
            6,\"\"\"Jim Doe\",\",\"\"New\"\",\r\n York\"
            """;

        boolean validationResult = csvMagikk.isValidCsv(csv, true);
        assertFalse(validationResult, "Did not return false when having improperly escaped cell");
    }

    @Test
    void toCSVWith2DArrayTest() {
        String expectedCsv = "id,name,location\r\n" +
                             "1,John Doe,New York\r\n" +
                             "2,James Doe,\"\"\"New\"\" York\"\r\n" +
                             "3,Mary Doe,\"New, York\"\r\n" +
                             "4,Jack Doe,\"\"\"New\"\", York\"\r\n" +
                             "5,\"\"\"Jane Doe\",\",\"\"New\"\", York\"\r\n" +
                             "6,\"\"\"Jim Doe\",\",\"\"New\"\",\r\n York\"\r\n";

        String[][] csv = {
            {"id", "name", "location"},
            {"1", "John Doe", "New York"},
            {"2", "James Doe", "\"New\" York"},
            {"3", "Mary Doe", "New, York"},
            {"4", "Jack Doe", "\"New\", York"},
            {"5", "\"Jane Doe", ",\"New\", York"},
            {"6", "\"Jim Doe", ",\"New\",\r\n York"},
        };

        String actualCsv = csvMagikk.toCsv(csv);

        assertEquals(expectedCsv, actualCsv, "Did not create proper CSV String");
    }

    @Test
    void toCSVWithListOfStringArraysTest() {
        String expectedCsv = "id,name,location\r\n" +
            "1,John Doe,New York\r\n" +
            "2,James Doe,\"\"\"New\"\" York\"\r\n" +
            "3,Mary Doe,\"New, York\"\r\n" +
            "4,Jack Doe,\"\"\"New\"\", York\"\r\n" +
            "5,\"\"\"Jane Doe\",\",\"\"New\"\", York\"\r\n" +
            "6,\"\"\"Jim Doe\",\",\"\"New\"\",\r\n York\"\r\n";

        List<String[]> csv = List.of(
            new String[]{"id", "name", "location"},
            new String[]{"1", "John Doe", "New York"},
            new String[]{"2", "James Doe", "\"New\" York"},
            new String[]{"3", "Mary Doe", "New, York"},
            new String[]{"4", "Jack Doe", "\"New\", York"},
            new String[]{"5", "\"Jane Doe", ",\"New\", York"},
            new String[]{"6", "\"Jim Doe", ",\"New\",\r\n York"}
        );

        String actualCsv = csvMagikk.toCsv(csv);

        assertEquals(expectedCsv, actualCsv, "Did not create proper CSV String");
    }

    @Test
    void toCSVRowTest() {
        String expectedCsvRow = "6,\"\"\"Jim Doe\",\",\"\"New\"\",\r\n York\"\r\n";

        String csvRow[] = {"6", "\"Jim Doe", ",\"New\",\r\n York"};

        String actualCsvRow = csvMagikk.toCsvRow(csvRow);

        assertEquals(expectedCsvRow, actualCsvRow, "Did not create proper CSV row String");
    }
}