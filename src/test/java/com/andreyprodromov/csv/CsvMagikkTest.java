package com.andreyprodromov.csv;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvMagikkTest {

    @Test
    void escapeTextWithQuotesAndCommasTest() {
        CsvMagikk magikk = new CsvMagikk();

        String input = "\"John, Mary and Sue went to \"Awesome Productions\" theater";

        String expected = "\"\"\"John, Mary and Sue went to \"\"Awesome Productions\"\" theater\"";
        String actual = magikk.escape(input);

        Assertions.assertEquals(expected, actual, "CsvMagikk does not escape string delimiters or cell delimiters properly");
    }
}