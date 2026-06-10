package com.k15t.spark.base.util;


import java.util.Arrays;
import java.util.regex.Pattern;


/**
 * String utility methods replacing previously used {@code org.apache.commons.lang3.StringUtils} functionality.
 */
public final class StringUtil {

    private StringUtil() {
    }


    /**
     * Checks if a string is not blank (not null, not empty, not whitespace only).
     */
    public static boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }


    /**
     * Returns the substring before the first occurrence of the separator.
     * If the separator is not found, the original string is returned.
     * Returns {@code null} if the input is {@code null}.
     */
    public static String substringBefore(String str, String separator) {
        if (str == null) {
            return null;
        }
        if (separator == null) {
            return str;
        }
        int pos = str.indexOf(separator);
        if (pos < 0) {
            return str;
        }
        return str.substring(0, pos);
    }


    /**
     * Splits a string by the given character separator, omitting empty strings from the result.
     * Returns an empty array if the input is {@code null}.
     */
    public static String[] split(String str, char separator) {
        return split(str, String.valueOf(separator));
    }


    /**
     * Splits a string by the given string separator, omitting empty strings from the result.
     * Returns an empty array if the input is {@code null}.
     */
    public static String[] split(String str, String separator) {
        if (str == null || str.isEmpty()) {
            return new String[0];
        }
        // Split by literal character, then filter out empty entries (mimics commons-lang3 behavior)
        return Arrays.stream(str.split(Pattern.quote(separator)))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }


    /**
     * Removes the given suffix from the end of the string if present.
     */
    public static String removeEnd(String str, String suffix) {
        if (str == null || suffix == null || !str.endsWith(suffix)) {
            return str;
        }
        return str.substring(0, str.length() - suffix.length());
    }


    /**
     * Removes the given prefix from the start of the string if present.
     */
    public static String removeStart(String str, String prefix) {
        if (str == null || prefix == null || !str.startsWith(prefix)) {
            return str;
        }
        return str.substring(prefix.length());
    }


    /**
     * Returns the input string or an empty string if the input is {@code null}.
     */
    public static String defaultString(String str) {
        return str == null ? "" : str;
    }


    /**
     * Null-safe check if a string ends with the given suffix.
     */
    public static boolean endsWith(String str, String suffix) {
        if (str == null || suffix == null) {
            return str == null && suffix == null;
        }
        return str.endsWith(suffix);
    }


    /**
     * Prepends the given prefix to the string if it is not already present.
     */
    public static String prependIfMissing(String str, String prefix) {
        if (str == null || prefix == null) {
            return str;
        }
        if (str.startsWith(prefix)) {
            return str;
        }
        return prefix + str;
    }


    /**
     * Replaces all occurrences of the search strings with the corresponding replacement strings.
     */
    public static String replaceEach(String text, String[] searchList, String[] replacementList) {
        if (text == null || text.isEmpty() || searchList == null || replacementList == null
                || searchList.length == 0 || replacementList.length == 0) {
            return text;
        }
        if (searchList.length != replacementList.length) {
            throw new IllegalArgumentException("Search and replacement lists must be the same length");
        }
        // Simultaneous replacement (matches commons-lang3 behavior):
        // scan for the earliest match among all search strings, replace it, then continue from after the replacement.
        StringBuilder buf = new StringBuilder(text.length());
        int textIndex = 0;
        while (textIndex < text.length()) {
            int bestIndex = -1;
            int bestPos = text.length();
            for (int i = 0; i < searchList.length; i++) {
                if (searchList[i] == null || searchList[i].isEmpty() || replacementList[i] == null) {
                    continue;
                }
                int pos = text.indexOf(searchList[i], textIndex);
                if (pos != -1 && pos < bestPos) {
                    bestPos = pos;
                    bestIndex = i;
                }
            }
            if (bestIndex == -1) {
                buf.append(text, textIndex, text.length());
                break;
            }
            buf.append(text, textIndex, bestPos);
            buf.append(replacementList[bestIndex]);
            textIndex = bestPos + searchList[bestIndex].length();
        }
        return buf.toString();
    }


    /**
     * Escapes the characters {@code &, <, >, ", '} with their HTML entity equivalents,
     * matching the behavior of {@code StringEscapeUtils.escapeHtml4} from commons-lang3.
     */
    public static String escapeHtml(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(str.length() + 16);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }


    /**
     * Trims the string and returns {@code null} if the result is empty.
     */
    public static String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
