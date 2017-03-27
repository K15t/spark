package com.k15t.spark.base.util;

import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class DocumentOutputUtil {

    private static final String IFRAME_RESIZE_JS_PATH = "com/k15t/spark/iframeResizer.min.js";

    private static final String IFRAME_CONTENT_WINDOW_JS_PATH = "com/k15t/spark/spark-dist.contentWindow.js";

    private static final String IFRAME_CONTENT_WRAPPER_TEMPLATE_PATH = "com/k15t/spark/content_iframe_wrapper.vm";


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
     * Returns JS code that should be injected into a document that is loaded into a SPARK controlled
     * iframe
     * </p><p>
     * Includes contents of the iframe contentWindow side part of the iframeResizer JS library as well
     * as some SPARK specific functions that make iframe's context available to the code embedded
     * in the iframe
     * </p>
     *
     * @return JS file to be included in SPARK iframe content document as a string
     * @throws NullPointerException if the resource cannot be found
     */
    public static String getIframeContentWindowJs() throws IOException {
        try (InputStream iframeResizeContentWindowFile =
                     DocumentOutputUtil.class.getClassLoader().
                             getResourceAsStream(IFRAME_CONTENT_WINDOW_JS_PATH)) {
            return IOUtils.toString(iframeResizeContentWindowFile, "UTF-8");
        }
    }


    /**
     * Returns the contents of the iframe host window side part of the iframeResizer JS library.
     *
     * @return iframeResizer JS file as a string
     * @throws NullPointerException if the resource cannot be found
     */
    private static String getIframeResizeJs() throws IOException {
        try (InputStream iframeResizeContentWindowFile =
                     DocumentOutputUtil.class.getClassLoader().
                             getResourceAsStream(IFRAME_RESIZE_JS_PATH)) {
            return IOUtils.toString(iframeResizeContentWindowFile, "UTF-8");
        }
    }


    /**
     * <p>
     * Returns velocity template that can be used for rendering the fragment to be used as the
     * iframe wrapper for an admin / space app
     * </p><p>
     * Generates HTML code for an iframe with source at 'appBaseUrl' and tries to also add
     * automatic iframeResizer functionality
     * </p><p>
     * Also custom context information can be injected into the app loaded in the iframe using velocity context.
     * </p><p>
     * The Velocity context used when rendering the template should be fetched by using
     * {@link #generateAdminIframeTemplateContext(String, String, String, String) generateAdminIframeTemplateContext()}
     * </p>
     *
     * @return iframe-content-wrapper Velocity template as a string
     * @throws NullPointerException if the resource cannot be found
     */
    public static String getIframeAdminContentWrapperTemplate() throws IOException {
        try (InputStream templateStream =
                     DocumentOutputUtil.class.getClassLoader().getResourceAsStream(IFRAME_CONTENT_WRAPPER_TEMPLATE_PATH)) {
            return IOUtils.toString(templateStream, "UTF-8");
        }
    }


    /**
     * <p>
     * Generates velocity context from the parameters (and a few constants) that is suitable to be used
     * with {@link #getIframeAdminContentWrapperTemplate()} velocity template
     * </p><p>
     * It is possible to communicate initialization info to the SPA loaded into the iframe by using query parameters.
     * The 'queryString' will be added to the src of the iframe in addition to an extra 'iframe_content=true'
     * parameter that is needed by the SPARK framework.
     * </p><p>
     * It is also possible to pass an information string to the content window of the iframe by using the
     * argument 'iframeContextInfo' (it will be available at SPARK.iframeContext). The contents will
     * be added as a JS string, but that string can contain eg. JSON info that the iframe JS code can parse.
     * </p>
     *
     * @param appBaseUrl base url for the SPA (must already contain trailing '/')
     * @param iframeIdToUse id to use for the iframe element
     * @param iframeContextInfo string to be added to the loaded iframe's window as 'SPARK.iframeContext'
     * @param queryString queryString to add to the url of the iframe content source (in addition to iframe_content parameter)
     * @return velocity context ready to be used with the iframe-admin-wrapper velocity template
     */
    public static Map<String, Object> generateAdminIframeTemplateContext(
            String appBaseUrl, String iframeIdToUse,
            String iframeContextInfo, String queryString) throws IOException {
        // add (possibly one more) layer of "-escaping so that the string can be added to js variable with "" delimiters
        String escapedIframeContext = iframeContextInfo == null ? "null" :
                iframeContextInfo.replace("\"", "\\\"");

        String queryStringToUse = "";
        if (queryString == null || "".equals(queryString)) {
            queryStringToUse = "?iframe_content=true";
        } else {
            if (queryString.startsWith("?")) {
                queryStringToUse = queryString + "&iframe_content=true";
            } else {
                queryStringToUse = "?iframe_content=true&" + queryString;
            }
        }

        String iframeSource = appBaseUrl + queryStringToUse;

        String iframeResizerJs = getIframeResizeJs();

        HashMap<String, Object> context = new HashMap<String, Object>();

        context.put("iframeResizerJs", iframeResizerJs);
        context.put("iframeId", iframeIdToUse);
        context.put("iframeSrc", iframeSource);
        context.put("escapedIframeContext", escapedIframeContext);

        return context;
    }

}
