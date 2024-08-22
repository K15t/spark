package com.k15t.spark.base.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class NgTranslateMessageBundleProviderTest {

    @Test
    public void escapeJson() {
        String input = "Hello \"World of backslashes (\\) !\"\nNew line with German umlauts äöüßÄÖÜ";
        String escaped = NgTranslateMessageBundleProvider.escapeJson(input);
        assertEquals("Hello \\\"World of backslashes (\\\\) !\\\"\\nNew line with German umlauts äöüßÄÖÜ", escaped);
    }

}
