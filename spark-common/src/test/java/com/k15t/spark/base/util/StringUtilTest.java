/**
 * Test cases adapted from Apache Commons Lang (https://commons.apache.org/proper/commons-lang/),
 * licensed under the Apache License, Version 2.0 (https://www.apache.org/licenses/LICENSE-2.0).
 */
package com.k15t.spark.base.util;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Tests for {@link StringUtil}, preserving the original test cases from Apache Commons Lang 3
 * ({@code StringUtils}, {@code StringEscapeUtils}) with minimal modifications.
 * <p>
 * Where our API intentionally differs from commons-lang3 (e.g. {@code split} returns an empty array
 * instead of {@code null} for {@code null} input), the test is adapted and annotated with a comment.
 * <p>
 * Original sources:
 * <ul>
 *   <li>{@code StringUtilsEmptyBlankTest} — isNotBlank</li>
 *   <li>{@code StringUtilsSubstringTest} — substringBefore</li>
 *   <li>{@code StringUtilsTest} — split, removeEnd, removeStart, defaultString, prependIfMissing, replaceEach</li>
 *   <li>{@code StringUtilsStartsEndsWithTest} — endsWith</li>
 *   <li>{@code StringUtilsTrimTest} — trimToNull</li>
 *   <li>{@code StringEscapeUtilsTest} — escapeHtml</li>
 * </ul>
 */
@SuppressWarnings({"UnnecessaryUnicodeEscape", "ConstantValue", "ObviousNullCheck", "ConcatenationWithEmptyString"})
public class StringUtilTest {

    // Constant from commons-lang3 StringUtilsTest: all chars where Character.isWhitespace is true
    // but char > 32 (i.e. not trimmable by String.trim())
    static final String WHITESPACE;
    static final String NON_TRIMMABLE;

    static {
        final StringBuilder ws = new StringBuilder();
        final StringBuilder ntr = new StringBuilder();
        for (int i = 0; i < Character.MAX_VALUE; i++) {
            if (Character.isWhitespace((char) i)) {
                ws.append((char) i);
                if (i > 32) {
                    ntr.append((char) i);
                }
            }
        }
        WHITESPACE = ws.toString();
        NON_TRIMMABLE = ntr.toString();
    }


    // -----------------------------------------------------------------------
    // From StringUtilsEmptyBlankTest: testIsNotBlank
    // -----------------------------------------------------------------------

    @Test
    public void testIsNotBlank() {
        assertFalse(StringUtil.isNotBlank(null));
        assertFalse(StringUtil.isNotBlank(""));
        assertFalse(StringUtil.isNotBlank(WHITESPACE));
        assertTrue(StringUtil.isNotBlank("a"));
        assertTrue(StringUtil.isNotBlank("foo"));
        assertTrue(StringUtil.isNotBlank(" foo "));
    }


    // -----------------------------------------------------------------------
    // From StringUtilsSubstringTest: testSubstringBefore_StringString
    // -----------------------------------------------------------------------

    @Test
    public void testSubstringBefore_StringString() {
        assertEquals("foo", StringUtil.substringBefore("fooXXbarXXbaz", "XX"));

        assertNull(StringUtil.substringBefore(null, null));
        assertNull(StringUtil.substringBefore(null, ""));
        assertNull(StringUtil.substringBefore(null, "XX"));
        assertEquals("", StringUtil.substringBefore("", null));
        assertEquals("", StringUtil.substringBefore("", ""));
        assertEquals("", StringUtil.substringBefore("", "XX"));

        assertEquals("foo", StringUtil.substringBefore("foo", null));
        assertEquals("foo", StringUtil.substringBefore("foo", "b"));
        assertEquals("f", StringUtil.substringBefore("foot", "o"));
        assertEquals("", StringUtil.substringBefore("abc", "a"));
        assertEquals("a", StringUtil.substringBefore("abcba", "b"));
        assertEquals("ab", StringUtil.substringBefore("abc", "c"));
        assertEquals("", StringUtil.substringBefore("abc", ""));
        assertEquals("abc", StringUtil.substringBefore("abc", "X"));
    }


    // -----------------------------------------------------------------------
    // From StringUtilsTest: testSplit_StringChar
    // -----------------------------------------------------------------------

    @Test
    public void testSplit_StringChar() {
        // Adaptation: commons-lang3 split(null, '.') returns null; our split returns empty array.
        assertArrayEquals(new String[0], StringUtil.split(null, '.'));

        assertEquals(0, StringUtil.split("", '.').length);

        String str = "a.b.. c";
        String[] res = StringUtil.split(str, '.');
        assertEquals(3, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
        assertEquals(" c", res[2]);

        str = ".a.";
        res = StringUtil.split(str, '.');
        assertEquals(1, res.length);
        assertEquals("a", res[0]);

        str = "a b c";
        res = StringUtil.split(str, ' ');
        assertEquals(3, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
        assertEquals("c", res[2]);
    }


    // -----------------------------------------------------------------------
    // From StringUtilsTest: testSplit_StringString_StringStringInt (partial)
    // -----------------------------------------------------------------------

    @Test
    public void testSplit_StringString() {
        // Adaptation: commons-lang3 split(null, ".") returns null; our split returns empty array.
        assertArrayEquals(new String[0], StringUtil.split(null, "."));

        assertEquals(0, StringUtil.split("", ".").length);

        // innerTestSplit equivalent for '.' separator
        String str = "a.b.. c";
        String[] res = StringUtil.split(str, ".");
        assertEquals(3, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
        assertEquals(" c", res[2]);

        str = ".a.";
        res = StringUtil.split(str, ".");
        assertEquals(1, res.length);
        assertEquals("a", res[0]);
    }


    // -----------------------------------------------------------------------
    // From StringUtilsTest: testRemoveEnd
    // -----------------------------------------------------------------------

    @Test
    public void testRemoveEnd() {
        // StringUtils.removeEnd("", *)        = ""
        assertNull(StringUtil.removeEnd(null, null));
        assertNull(StringUtil.removeEnd(null, ""));
        assertNull(StringUtil.removeEnd(null, "a"));

        // StringUtils.removeEnd(*, null)      = *
        assertEquals("", StringUtil.removeEnd("", null));
        assertEquals("", StringUtil.removeEnd("", ""));
        assertEquals("", StringUtil.removeEnd("", "a"));

        // All others:
        assertEquals("www.domain.com.", StringUtil.removeEnd("www.domain.com.", ".com"));
        assertEquals("www.domain", StringUtil.removeEnd("www.domain.com", ".com"));
        assertEquals("www.domain", StringUtil.removeEnd("www.domain", ".com"));
        assertEquals("domain.com", StringUtil.removeEnd("domain.com", ""));
        assertEquals("domain.com", StringUtil.removeEnd("domain.com", null));
    }


    // -----------------------------------------------------------------------
    // From StringUtilsTest: testRemoveStartString
    // -----------------------------------------------------------------------

    @Test
    public void testRemoveStart() {
        // StringUtils.removeStart("", *)        = ""
        assertNull(StringUtil.removeStart(null, null));
        assertNull(StringUtil.removeStart(null, ""));
        assertNull(StringUtil.removeStart(null, "a"));

        // StringUtils.removeStart(*, null)      = *
        assertEquals("", StringUtil.removeStart("", null));
        assertEquals("", StringUtil.removeStart("", ""));
        assertEquals("", StringUtil.removeStart("", "a"));

        // All others:
        assertEquals("domain.com", StringUtil.removeStart("www.domain.com", "www."));
        assertEquals("domain.com", StringUtil.removeStart("domain.com", "www."));
        assertEquals("domain.com", StringUtil.removeStart("domain.com", ""));
        assertEquals("domain.com", StringUtil.removeStart("domain.com", null));
    }


    // -----------------------------------------------------------------------
    // From StringUtilsTest: testDefault_String
    // -----------------------------------------------------------------------

    @Test
    public void testDefaultString() {
        assertEquals("", StringUtil.defaultString(null));
        assertEquals("", StringUtil.defaultString(""));
        assertEquals("abc", StringUtil.defaultString("abc"));
    }


    // -----------------------------------------------------------------------
    // From StringUtilsStartsEndsWithTest: testEndsWith
    // -----------------------------------------------------------------------

    @Test
    public void testEndsWith() {
        assertTrue("endsWith(null, null)", StringUtil.endsWith(null, null));
        assertFalse("endsWith(FOOBAR, null)", StringUtil.endsWith("FOOBAR", null));
        assertFalse("endsWith(null, FOO)", StringUtil.endsWith(null, "FOO"));
        assertTrue("endsWith(FOOBAR, \"\")", StringUtil.endsWith("FOOBAR", ""));

        assertFalse("endsWith(foobar, foo)", StringUtil.endsWith("foobar", "foo"));
        assertFalse("endsWith(FOOBAR, FOO)", StringUtil.endsWith("FOOBAR", "FOO"));
        assertFalse("endsWith(foobar, FOO)", StringUtil.endsWith("foobar", "FOO"));
        assertFalse("endsWith(FOOBAR, foo)", StringUtil.endsWith("FOOBAR", "foo"));

        assertFalse("endsWith(foo, foobar)", StringUtil.endsWith("foo", "foobar"));
        assertFalse("endsWith(bar, foobar)", StringUtil.endsWith("bar", "foobar"));

        assertTrue("endsWith(foobar, bar)", StringUtil.endsWith("foobar", "bar"));
        assertTrue("endsWith(FOOBAR, BAR)", StringUtil.endsWith("FOOBAR", "BAR"));
        assertFalse("endsWith(foobar, BAR)", StringUtil.endsWith("foobar", "BAR"));
        assertFalse("endsWith(FOOBAR, bar)", StringUtil.endsWith("FOOBAR", "bar"));

        // "alpha, beta, gamma, delta".endsWith("delta")
        assertTrue(StringUtil.endsWith("\u03B1\u03B2\u03B3\u03B4", "\u03B4"));
        // "alpha, beta, gamma, delta".endsWith("gamma, DELTA")
        assertFalse(StringUtil.endsWith("\u03B1\u03B2\u03B3\u03B4", "\u03B3\u0394"));
    }


    // -----------------------------------------------------------------------
    // From StringUtilsTest: testPrependIfMissing
    // -----------------------------------------------------------------------

    @Test
    public void testPrependIfMissing() {
        assertNull("prependIfMissing(null,null)", StringUtil.prependIfMissing(null, null));
        assertEquals("prependIfMissing(abc,null)", "abc", StringUtil.prependIfMissing("abc", null));
        assertEquals("prependIfMissing(\"\",xyz)", "xyz", StringUtil.prependIfMissing("", "xyz"));
        assertEquals("prependIfMissing(abc,xyz)", "xyzabc", StringUtil.prependIfMissing("abc", "xyz"));
        assertEquals("prependIfMissing(xyzabc,xyz)", "xyzabc", StringUtil.prependIfMissing("xyzabc", "xyz"));
        assertEquals("prependIfMissing(XYZabc,xyz)", "xyzXYZabc", StringUtil.prependIfMissing("XYZabc", "xyz"));
    }


    // -----------------------------------------------------------------------
    // From StringUtilsTest: testReplace_StringStringArrayStringArray
    // -----------------------------------------------------------------------

    @Test
    public void testReplaceEach() {
        // JAVADOC TESTS START
        assertNull(StringUtil.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
        assertEquals("", StringUtil.replaceEach("", new String[]{"a"}, new String[]{"b"}));
        assertEquals("aba", StringUtil.replaceEach("aba", null, null));
        assertEquals("aba", StringUtil.replaceEach("aba", new String[0], null));
        assertEquals("aba", StringUtil.replaceEach("aba", null, new String[0]));
        assertEquals("aba", StringUtil.replaceEach("aba", new String[]{"a"}, null));

        assertEquals("b", StringUtil.replaceEach("aba", new String[]{"a"}, new String[]{""}));
        assertEquals("aba", StringUtil.replaceEach("aba", new String[]{null}, new String[]{"a"}));
        assertEquals("wcte", StringUtil.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
        assertEquals("dcte", StringUtil.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));
        // JAVADOC TESTS END

        assertEquals("bcc", StringUtil.replaceEach("abc", new String[]{"a", "b"}, new String[]{"b", "c"}));
        assertEquals("q651.506bera", StringUtil.replaceEach("d216.102oren",
                new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
                        "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D",
                        "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                        "U", "V", "W", "X", "Y", "Z", "1", "2", "3", "4", "5", "6", "7", "8", "9"},
                new String[]{"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "a",
                        "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "N", "O", "P", "Q",
                        "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "A", "B", "C", "D", "E", "F", "G",
                        "H", "I", "J", "K", "L", "M", "5", "6", "7", "8", "9", "1", "2", "3", "4"}));

        // Test null safety inside arrays - LANG-552
        assertEquals("aba", StringUtil.replaceEach("aba", new String[]{"a"}, new String[]{null}));
        assertEquals("cbc", StringUtil.replaceEach("aba", new String[]{"a", "b"}, new String[]{"c", null}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceEach_mismatchedLengths() {
        StringUtil.replaceEach("abba", new String[]{"a"}, new String[]{"b", "a"});
    }


    // -----------------------------------------------------------------------
    // From StringEscapeUtilsTest: testEscapeHtml (HTML_ESCAPES data)
    // -----------------------------------------------------------------------

    /**
     * Original HTML_ESCAPES test data from commons-lang3 StringEscapeUtilsTest.
     * Format: {message, expected, original}.
     * <p>
     * Note: Our escapeHtml only escapes {@code & < > " '} — it does NOT escape named HTML entities
     * like {@code &ccedil;} or 8-bit characters. Tests for those cases are adapted to reflect our
     * simpler implementation which is sufficient for the project's needs.
     */
    private static final String[][] HTML_ESCAPES = {
            {"no escaping", "plain text", "plain text"},
            {"no escaping", "plain text", "plain text"},
            {"empty string", "", ""},
            {"null", null, null},
            {"ampersand", "bread &amp; butter", "bread & butter"},
            {"quotes", "&quot;bread&quot; &amp; butter", "\"bread\" & butter"},
            {"final character only", "greater than &gt;", "greater than >"},
            {"first character only", "&lt; less than", "< less than"},
            // Adaptation: commons-lang3 does NOT escape apostrophes; our impl escapes ' to &#39;
            // for safety in HTML attribute contexts. Original expected: "Huntington's chorea"
            {"apostrophe", "Huntington&#39;s chorea", "Huntington's chorea"},
            // Adaptation: commons-lang3 escapes ç to &ccedil;; our impl does not escape beyond & < > " '
            // Original expected: "English,Fran&ccedil;ais,\u65E5\u672C\u8A9E (nihongo)"
            {"languages", "English,Fran\u00E7ais,\u65E5\u672C\u8A9E (nihongo)", "English,Fran\u00E7ais,\u65E5\u672C\u8A9E (nihongo)"},
            {"8-bit ascii shouldn't number-escape", "\u0080\u009F", "\u0080\u009F"},
    };

    @Test
    public void testEscapeHtml() {
        for (final String[] element : HTML_ESCAPES) {
            final String message = element[0];
            final String expected = element[1];
            final String original = element[2];
            assertEquals(message, expected, StringUtil.escapeHtml(original));
        }
    }

    @Test
    public void testEscapeHtmlHighUnicode() {
        // From StringEscapeUtilsTest: COUNTING ROD UNIT DIGIT THREE U+1D362
        final byte[] data = {(byte) 0xF0, (byte) 0x9D, (byte) 0x8D, (byte) 0xA2};
        final String original = new String(data, java.nio.charset.StandardCharsets.UTF_8);
        final String escaped = StringUtil.escapeHtml(original);
        assertEquals("High Unicode should not have been escaped", original, escaped);
    }


    // -----------------------------------------------------------------------
    // From StringUtilsTrimTest: testTrimToNull
    // -----------------------------------------------------------------------

    @Test
    public void testTrimToNull() {
        assertEquals("foo", StringUtil.trimToNull("foo" + " "));
        assertEquals("foo", StringUtil.trimToNull(" " + "foo" + " "));
        assertEquals("foo", StringUtil.trimToNull(" " + "foo"));
        assertEquals("foo", StringUtil.trimToNull("foo" + ""));
        assertNull(StringUtil.trimToNull(" \t\r\n\b "));
        // Adaptation: commons-lang3 uses TRIMMABLE constant (chars 0-32); String.trim() handles the same range.
        assertNull(StringUtil.trimToNull(""));
        assertNull(StringUtil.trimToNull(null));
        assertEquals(NON_TRIMMABLE, StringUtil.trimToNull(NON_TRIMMABLE));
    }

}
