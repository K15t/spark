package com.k15t.spark.base.util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Locale;


public class DocumentOutputUtil {

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
     * Generates HTML code for an iframe with source at 'appBaseUrl' and tries to also add
     * automatic iframeResizer functionality
     *
     * Generates script node for loading iframeResizer lib (from default path), iframe node
     * loading the app from appBaseUrl, and script node with bootstrap code for the iframeResizer
     *
     * IframeResizer scripts (iframeResizer.min.js and iframeResizer.contentWindow.min.js) have to
     * be placed at 'appBaseUrl(/)'libs/spark (appBaseUrl should already contain the trailing '/')
     *
     * @param appBaseUrl base url for the SPA (must already contain trailing '/')
     * @param iframeIdToUse id to use for the iframe element
     * @return the generated HTML code as a string
     */
    public static String generateResizedIframeHtml(String appBaseUrl, String iframeIdToUse) {

        // script element trying to load the iframeResizer lib
        String body = "<script src='" + appBaseUrl + "libs/spark/iframeResizer.min.js'></script>";

        // main iframe element loading the app content with 'iframe_content=true' param
        body += ("<iframe id='" + iframeIdToUse
            + "' scrolling='no' style='width: 100%; border: none;' src='"
            +    appBaseUrl + "?iframe_content=true'>"
            + "</iframe>");

        // run the iframeResizer initialitazion
        body += ("<script>\n"
                + "if (iFrameResize) {\n"
                + "iFrameResize({'autoResize': true, 'heightCalculationMethod': 'max'}, "
                + "'#" + iframeIdToUse + "');\n"
                + "}\n</script>");

        return body;

    }

}
