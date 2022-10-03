package com.k15t.spark.base.util;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DocumentOutputUtil {

    private static final Pattern IFRAME_ID_PREFIX_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-]+$");

    private static final String ESCAPED_IFRAME_CONTENT_WINDOW_JS;
    private static final String IFRAME_CONTENT_WRAPPER_TEMPLATE;
    private static final String ESCAPED_SPARK_JS;


    static {
        ClassLoader classLoader = DocumentOutputUtil.class.getClassLoader();

        try (InputStream in = classLoader.getResourceAsStream("com/k15t/spark/spark-dist.contentWindow.js")) {
            ESCAPED_IFRAME_CONTENT_WINDOW_JS = escapeJavascriptForScriptTag(StreamUtil.toString(in));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        try (InputStream in = classLoader.getResourceAsStream("com/k15t/spark/content_iframe_wrapper.html")) {
            IFRAME_CONTENT_WRAPPER_TEMPLATE = StreamUtil.toString(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        try (InputStream in = classLoader.getResourceAsStream("com/k15t/spark/spark-dist.js")) {
            ESCAPED_SPARK_JS = escapeJavascriptForScriptTag(StreamUtil.toString(in));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    /**
     * Escapes the provided javascript code for insertion into an HTML {@code <script>} tag body.
     * <p>According to
     * <a href="https://html.spec.whatwg.org/multipage/scripting.html#restrictions-for-contents-of-script-elements">the docs</a>:
     * </p>
     * <pre>{@code
     * The easiest and safest way to avoid the rather strange restrictions described in this section is to always escape an ASCII
     * case-insensitive match for "<!--" as "\x3C!--", "<script" as "\x3Cscript", and "</script" as "\x3C/script" when these sequences
     * appear in literals in scripts (e.g. in strings, regular expressions, or comments), and to avoid writing code that uses such
     * constructs in expressions. Doing so avoids the pitfalls that the restrictions in this section are prone to triggering: namely,
     * that, for historical reasons, parsing of script blocks in HTML is a strange and exotic practice that acts unintuitively in
     * the face of these sequences.
     * }</pre>
     */
    static String escapeJavascriptForScriptTag(String javascript) {
        String escaped = Pattern.compile("<!--", Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                .matcher(javascript)
                .replaceAll(Matcher.quoteReplacement("\\x3C!--"));
        escaped = Pattern.compile("<script", Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                .matcher(escaped)
                .replaceAll(Matcher.quoteReplacement("\\x3Cscript"));
        escaped = Pattern.compile("</script", Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                .matcher(escaped)
                .replaceAll(Matcher.quoteReplacement("\\x3C/script"));
        return escaped;
    }



    public static void applyCacheKeysToResourceUrls(Document document, long pluginModifiedTimestamp, Locale locale) {
        String cacheKey = getCacheKeyPathSegments(pluginModifiedTimestamp, locale);

        Elements injectedScripts = document.select("script[data-spark-injected]");
        for (Element script : injectedScripts) {
            script.attr("src", cacheKey + "/" + script.attr("src"));
        }

        Elements injectedStyles = document.select("link[data-spark-injected]");
        for (Element style : injectedStyles) {
            style.attr("href", cacheKey + "/" + style.attr("href"));
        }
    }


    private static String getCacheKeyPathSegments(long pluginModifiedTimestamp, Locale locale) {
        return "_/" + pluginModifiedTimestamp + "/" + locale.toString();
    }


    /**
     * <p>
     * Returns JS code that should be injected into a document that is loaded into a SPARK controlled iframe.
     * </p><p>
     * Includes contents of the iframe contentWindow side part of the iframeResizer JS library as well as some SPARK specific functions
     * that make iframe's context available to the code embedded in the iframe
     * </p><p>
     * The returned JS is escaped for insertion into an HTML {@code <script>} tag.
     * </p>
     *
     * @return JS file to be included in SPARK iframe content document as a string
     * @throws NullPointerException if the resource cannot be found
     */
    public static String getIframeContentWindowJs() {
        return ESCAPED_IFRAME_CONTENT_WINDOW_JS;
    }


    /**
     * <p>
     * Renders the main HTML content with the iframe to use for wrapping the SPA and required extra JS-code.
     * </p><p>
     * It is possible to communicate initialization info to the SPA loaded into the iframe by using query parameters.
     * The 'queryString' will be added to the src of the iframe.
     * </p><p>
     * It is also possible to pass an information string to the content window of the iframe by using the
     * argument 'iframeContextInfo' (it will be available using SPARK.getContextData()). The contents will
     * be added as a JS string, but that string can contain eg. JSON info that the iframe JS code can parse.
     * </p>
     *
     * @param spaBaseUrl base url for the SPA (without contextPath prefix)
     * @param spaQueryString queryString to add to the url of the iframe content source
     * @param iframeContextInfo string that will be available in the iframe's context using SPARK.getContextData()
     * @param iframeIdPrefix prefix for iframe ID. Must match {@link DocumentOutputUtil#IFRAME_ID_PREFIX_PATTERN}
     * @return html code containing a SPARK iframe
     */
    public static String renderSparkIframeBody(String spaBaseUrl, String spaQueryString, String iframeContextInfo, String iframeIdPrefix) {
        if (!IFRAME_ID_PREFIX_PATTERN.matcher(iframeIdPrefix).matches()) {
            throw new IllegalArgumentException("Invalid iframeIdPrefix. Must match " + IFRAME_ID_PREFIX_PATTERN);
        }

        String iframeId = iframeIdPrefix + System.currentTimeMillis();
        String iframeContext = String.valueOf(iframeContextInfo);
        String iframeSource = spaBaseUrl;
        if (StringUtils.isNotBlank(spaQueryString)) {
            iframeSource += StringUtils.prependIfMissing(spaQueryString, "?");
        }

        return StringUtils.replaceEach(
                IFRAME_CONTENT_WRAPPER_TEMPLATE,
                new String[] {"{{iframeId}}", "{{iframeSrc}}", "{{iframeContext}}", "{{sparkJs}}"},
                new String[] {
                        iframeId, // Not escaped because inserted into both JS and HTML contexts and must match IFRAME_ID_PREFIX_PATTERN
                        StringEscapeUtils.escapeHtml4(iframeSource),
                        StringEscapeUtils.escapeHtml4(iframeContext),
                        ESCAPED_SPARK_JS
                }
        );
    }

}
