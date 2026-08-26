package com.andreyprodromov.csv;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvMagikkTest {

    CsvMagikk csvMagikk = CsvMagikk.create();

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
    void escapeTextWithCRTest() {
        String input = "John, Mary and Sue went to\rtheater";

        String expected = "\"John, Mary and Sue went to\rtheater\"";
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
    void simpleParseExtractingDataCorrectlyFromCSVWithNoCRLFAtEnd() {
        String csv = "Name,Age,City\r\n" +
            "John Doe,30,New York\r\n" +
            "Jane Doe,25,Los Angeles\r\n" +
            "Alice,35,Chicago\r\n" +
            "Bob,40,Houston";

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
    void simpleParseExtractingDataCorrectlyFromCSVWithROnlyDelimiter() {
        String csv = "Name,Age,City\r" +
            "John Doe,30,New York\r" +
            "Jane Doe,25,Los Angeles\r" +
            "Alice,35,Chicago\r" +
            "Bob,40,Houston\r";

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
    void simpleParseOneLineOnlyTest() {
        String csv = "id,name,location";

        String[][] extractedCsv = csvMagikk.parseCsv(csv);

        // Headers
        assertEquals("id", extractedCsv[0][0], "Couldn't extract header row properly");
        assertEquals("name", extractedCsv[0][1], "Couldn't extract header row properly");
        assertEquals("location", extractedCsv[0][2], "Couldn't extract header row properly");
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

    @Test
    void parseCsvThrowsWhenQuoteIsNotClosedTest() {
        String csv = "Name,Age\r\nJohn,\"30";

        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.parseCsv(csv)
        );
    }

    @Test
    void parseCsvThrowsWhenTextFollowsClosingQuoteTest() {
        String csv = "Name,Age\r\n\"John\"Doe,30";

        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.parseCsv(csv)
        );
    }

    @Test
    void parseCsvThrowsWhenQuoteAppearsInsideUnquotedFieldTest() {
        String csv = "Name,Age\r\nJo\"hn,30";

        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.parseCsv(csv)
        );
    }

    @Test
    void parseCsvThrowsWhenQuoteIsNotProperlyEscapedTest() {
        String csv = "Name,Age\r\n\"John \"Doe\",30";

        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.parseCsv(csv)
        );
    }

    @Test
    void parseCsvThrowsWhenQuotedFieldIsFollowedByTextTest() {
        String csv = "Name,Age\r\n\"John\"abc,30";

        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.parseCsv(csv)
        );
    }

    @Test
    void parseCsvThrowsWhenQuotedFieldIsFollowedByCommaTextTest() {
        String csv = "Name,Age,City\r\n\"John\"abc,30,London";

        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.parseCsv(csv)
        );
    }

    @Test
    void parseCsvThrowsWhenRowsHaveDifferentNumberOfColumnsTest() {
        String csv = "Name,Age,City\r\n" +
            "John,30,London\r\n" +
            "Jane,25\r\n";

        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.parseCsv(csv)
        );
    }

    @Test
    void parseCsvThrowsWhenCsvIsNullTest() {
        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.parseCsv(null)
        );
    }

    @Test
    void parseCsvThrowsWhenCsvIsBlankTest() {
        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.parseCsv("   \t\r\n  ")
        );
    }

    @Test
    void parseCsvParsesEmptyFieldsTest() {
        String csv = "Name,Age,City\r\nJohn,,London\r\n";

        String[][] result = csvMagikk.parseCsv(csv);

        assertEquals(2, result.length);
        assertArrayEquals(
            new String[]{"Name", "Age", "City"},
            result[0]
        );
        assertArrayEquals(
            new String[]{"John", "", "London"},
            result[1]
        );
    }

    @Test
    void parseCsvParsesEmptyFirstFieldTest() {
        String csv = ",Age,City\r\n,30,London\r\n";

        String[][] result = csvMagikk.parseCsv(csv);

        assertArrayEquals(
            new String[]{"", "Age", "City"},
            result[0]
        );

        assertArrayEquals(
            new String[]{"", "30", "London"},
            result[1]
        );
    }

    @Test
    void parseCsvParsesEmptyLastFieldTest() {
        String csv = "Name,Age,City\r\nJohn,30,\r\n";

        String[][] result = csvMagikk.parseCsv(csv);

        assertArrayEquals(
            new String[]{"Name", "Age", "City"},
            result[0]
        );

        assertArrayEquals(
            new String[]{"John", "30", ""},
            result[1]
        );
    }

    @Test
    void parseCsvParsesSingleEmptyFieldTest() {
        String csv = "\"\"";

        String[][] result = csvMagikk.parseCsv(csv);

        assertEquals(1, result.length);
        assertEquals(1, result[0].length);
        assertEquals("", result[0][0]);
    }

    @Test
    void parseCsvParsesMultipleEmptyFieldTest() {
        String csv = "\"\",\"\",\"\"";

        String[][] result = csvMagikk.parseCsv(csv);

        assertEquals(1, result.length);
        assertEquals(3, result[0].length);
    }

    @Test
    void parseCsvParsesSingleRowTest() {
        String csv = "n\r" +
                     "1\r" +
                     "2\r" +
                     "\r" +
                     "4";

        String[][] result = csvMagikk.parseCsv(csv);

        assertEquals(5, result.length);
        assertEquals("", result[3][0]);
        assertEquals("4", result[4][0]);
    }

    @Test
    void parseCsvParsesCommasOnlyTest() {
        String csv = ",,,\n" +
                     ",,,\n" +
                     ",,,\n" +
                     ",,,";

        String[][] result = csvMagikk.parseCsv(csv);

        assertEquals(4, result.length);
        assertEquals(4, result[0].length);
        assertEquals("", result[0][0]);
    }

    @Test
    void parseCsvPreservesTrailingEmptyFieldTest() {
        String csv = "a,b,";

        String[][] result = csvMagikk.parseCsv(csv);

        assertArrayEquals(
            new String[]{"a", "b", ""},
            result[0]
        );
    }

    @Test
    void parseCsvPreservesTrailingEmptyFieldTestQuoted() {
        String csv = "a,b,\"\"";

        String[][] result = csvMagikk.parseCsv(csv);

        assertArrayEquals(
            new String[]{"a", "b", ""},
            result[0]
        );
    }

    @Test
    void parseCsvPreservesTrailingEmptyFieldMiddle() {
        String csv = "a,b,,c";

        String[][] result = csvMagikk.parseCsv(csv);

        assertArrayEquals(
            new String[]{"a", "b", "", "c"},
            result[0]
        );
    }

    @Test
    void parseCsvPreservesTrailingEmptyFieldMiddleQuoted() {
        String csv = "a,b,\"\",c";

        String[][] result = csvMagikk.parseCsv(csv);

        assertArrayEquals(
            new String[]{"a", "b", "", "c"},
            result[0]
        );
    }

    @Test
    void parseCsvParsesQuotedEmptyFieldTest() {
        String csv = "Name,Age\r\n\"John\",\"\"\r\n";

        String[][] result = csvMagikk.parseCsv(csv);

        assertEquals("John", result[1][0]);
        assertEquals("", result[1][1]);
    }

    @Test
    void parseCsvParsesEmbeddedNewlineInsideQuotedFieldTest() {
        String csv = "Name,Description\r\n" +
                     "John,\"Hello\r\nWorld\"\r\n";

        String[][] result = csvMagikk.parseCsv(csv);

        assertEquals("John", result[1][0]);
        assertEquals("Hello\r\nWorld", result[1][1]);
    }

    @Test
    void parseCsvParsesEmbeddedLFInsideQuotedFieldTest() {
        String csv = "Name,Description\r\n" +
                     "John,\"Hello\nWorld\"\r\n";

        String[][] result = csvMagikk.parseCsv(csv);

        assertEquals("Hello\nWorld", result[1][1]);
    }

    @Test
    void parseCsvParsesEmbeddedCRInsideQuotedFieldTest() {
        String csv = "Name,Description\r\n" +
                     "John,\"Hello\rWorld\"\r\n";

        String[][] result = csvMagikk.parseCsv(csv);

        assertEquals("Hello\rWorld", result[1][1]);
    }

    @Test
    void parseCsvParsesEscapedQuotesTest() {
        String csv = "Name\r\n\"John \"\"Johnny\"\" Doe\"\r\n";

        String[][] result = csvMagikk.parseCsv(csv);

        assertEquals(
            "John \"Johnny\" Doe",
            result[1][0]
        );
    }

    @Test
    void parseCsvParsesMultipleEscapedQuotesTest() {
        String csv = "\"\"\"Hello\"\" \"\"World\"\"\"";

        String[][] result = csvMagikk.parseCsv(csv);

        assertEquals(
            "\"Hello\" \"World\"",
            result[0][0]
        );
    }

    @Test
    void parseCsvAcceptsLFOnlyRowsTest() {
        String csv = "Name,Age\nJohn,30\nJane,25";

        String[][] result = csvMagikk.parseCsv(csv);

        assertEquals(3, result.length);
        assertArrayEquals(
            new String[]{"Name", "Age"},
            result[0]
        );
        assertArrayEquals(
            new String[]{"John", "30"},
            result[1]
        );
        assertArrayEquals(
            new String[]{"Jane", "25"},
            result[2]
        );
    }

    @Test
    void parseCsvAcceptsMixedLineEndingsTest() {
        String csv = "Name,Age\r\n" +
                     "John,30\n" +
                     "Jane,25\r" +
                     "Bob,40";

        String[][] result = csvMagikk.parseCsv(csv);

        assertEquals(4, result.length);
        assertArrayEquals(new String[]{"Name", "Age"}, result[0]);
        assertArrayEquals(new String[]{"John", "30"}, result[1]);
        assertArrayEquals(new String[]{"Jane", "25"}, result[2]);
        assertArrayEquals(new String[]{"Bob", "40"}, result[3]);
    }

    @Test
    void parseCsvHandlesSingleColumnRowsTest() {
        String csv = "Name\r\nJohn\r\nJane\r\nBob";

        String[][] result = csvMagikk.parseCsv(csv);

        assertEquals(4, result.length);
        assertArrayEquals(new String[]{"Name"}, result[0]);
        assertArrayEquals(new String[]{"John"}, result[1]);
        assertArrayEquals(new String[]{"Jane"}, result[2]);
        assertArrayEquals(new String[]{"Bob"}, result[3]);
    }

    @Test
    void parseCsvHandlesLargeNumberOfColumnsTest() {
        String csv =
            "1,2,3,4,5,6,7,8,9,10," +
            "11,12,13,14,15,16,17,18,19,20";

        String[][] result = csvMagikk.parseCsv(csv);

        assertEquals(1, result.length);
        assertEquals(20, result[0].length);
        assertEquals("1", result[0][0]);
        assertEquals("20", result[0][19]);
    }


    @Test
    void toCsvThrowsWhenInputIsNullTest() {
        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.toCsv((String[][]) null)
        );
    }

    @Test
    void toCsvThrowsWhenRowIsNullTest() {
        String[][] csv = {
            {"John", "30"},
            null
        };

        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.toCsv(csv)
        );
    }

    @Test
    void toCsvThrowsWhenRowIsEmptyTest() {
        String[][] csv = {
            {"John", "30"},
            {}
        };

        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.toCsv(csv)
        );
    }

    @Test
    void toCsvThrowsWhenCellIsNullTest() {
        String[][] csv = {
            {"John", null}
        };

        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.toCsv(csv)
        );
    }

    @Test
    void toCsvRowThrowsWhenColumnsAreNullTest() {
        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.toCsvRow(null)
        );
    }

    @Test
    void toCsvRowThrowsWhenColumnsAreEmptyTest() {
        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.toCsvRow(new String[0])
        );
    }

    @Test
    void escapeThrowsWhenCellIsNullTest() {
        assertThrows(
            IllegalArgumentException.class,
            () -> csvMagikk.escape(null)
        );
    }

    @Test
    void escapeLeavesNormalTextUnchangedTest() {
        assertEquals(
            "Hello World",
            csvMagikk.escape("Hello World")
        );
    }

    @Test
    void escapeQuotesOnlyTest() {
        assertEquals(
            "\"\"\"Hello\"\"\"",
            csvMagikk.escape("\"Hello\"")
        );
    }

    @Test
    void escapeCommaOnlyTest() {
        assertEquals(
            "\"Hello,World\"",
            csvMagikk.escape("Hello,World")
        );
    }

    @Test
    void escapeLFOnlyTest() {
        assertEquals(
            "\"Hello\nWorld\"",
            csvMagikk.escape("Hello\nWorld")
        );
    }

    @Test
    void parseAndSerializeShouldRoundTripSimpleCsvTest() {
        String[][] original = {
            {"Name", "Age", "City"},
            {"John", "30", "London"},
            {"Jane", "25", "Paris"}
        };

        String serialized = csvMagikk.toCsv(original);
        String[][] parsed = csvMagikk.parseCsv(serialized);

        assertArrayEquals(original, parsed);
    }

    @Test
    void parseAndSerializeShouldRoundTripSpecialCharactersTest() {
        String[][] original = {
            {"Name", "Description"},
            {"John", "Hello, world"},
            {"Jane", "She said \"hello\""},
            {"Bob", "Line one\r\nLine two"},
            {"Alice", "Comma, quote \" and newline\n"}
        };

        String serialized = csvMagikk.toCsv(original);
        String[][] parsed = csvMagikk.parseCsv(serialized);

        assertArrayEquals(original, parsed);
    }

    @Test
    void escapeAndParseShouldRoundTripSingleCellTest() {
        String original = "Hello, \"world\"\r\nHow are you?";

        String escaped = csvMagikk.escape(original);
        String[][] parsed = csvMagikk.parseCsv(escaped);

        assertEquals(original, parsed[0][0]);
    }

    @Test
    void customColumnDelimiterTest() {
        CsvMagikk processor = CsvMagikk.create(';', '"');

        String csv = "Name;Age;City\r\nJohn;30;London";

        String[][] result = processor.parseCsv(csv);

        assertArrayEquals(
            new String[]{"Name", "Age", "City"},
            result[0]
        );

        assertArrayEquals(
            new String[]{"John", "30", "London"},
            result[1]
        );
    }

    @Test
    void customColumnDelimiterShouldBeEscapedTest() {
        CsvMagikk processor = CsvMagikk.create(';', '"');

        assertEquals(
            "\"Hello;World\"",
            processor.escape("Hello;World")
        );
    }

    @Test
    void customStringDelimiterTest() {
        CsvMagikk processor = CsvMagikk.create(',', '\'');

        String csv = "Name,Description\r\n" +
                     "'John, Doe','He said ''hello'''";

        String[][] result = processor.parseCsv(csv);

        assertEquals("John, Doe", result[1][0]);
        assertEquals("He said 'hello'", result[1][1]);
    }

    @Test
    void customDelimitersShouldRoundTripTest() {
        CsvMagikk processor = CsvMagikk.create(';', '\'');

        String[][] original = {
            {"Name", "Description"},
            {"John; Doe", "He said 'hello'"},
            {"Jane", "Hello\nWorld"}
        };

        String serialized = processor.toCsv(original);
        String[][] parsed = processor.parseCsv(serialized);

        assertArrayEquals(original, parsed);
    }

    @Test
    void createThrowsWhenDelimitersAreTheSameTest() {
        assertThrows(
            IllegalStateException.class,
            () -> CsvMagikk.create(',', ',')
        );
    }

    @Test
    void createThrowsWhenColumnDelimiterIsCRTest() {
        assertThrows(
            IllegalStateException.class,
            () -> CsvMagikk.create('\r', '"')
        );
    }

    @Test
    void createThrowsWhenColumnDelimiterIsLFTest() {
        assertThrows(
            IllegalStateException.class,
            () -> CsvMagikk.create('\n', '"')
        );
    }

    @Test
    void createThrowsWhenStringDelimiterIsCRTest() {
        assertThrows(
            IllegalStateException.class,
            () -> CsvMagikk.create(',', '\r')
        );
    }

    @Test
    void createThrowsWhenStringDelimiterIsLFTest() {
        assertThrows(
            IllegalStateException.class,
            () -> CsvMagikk.create(',', '\n')
        );
    }
}