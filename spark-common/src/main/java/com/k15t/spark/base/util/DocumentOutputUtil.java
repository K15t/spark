package com.k15t.spark.base.util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;


public class DocumentOutputUtil {

    private final static Logger logger = LoggerFactory.getLogger(DocumentOutputUtil.class);

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
     * It is possible to pass an information string to the content window of the iframe by using the
     * argument 'iframeContextInfo' (it will be available at SPARK.iframeContext). The contents will
     * be added as a JS string, but that string can contain eg. JSON info that the iframe JS code can parse.
     *
     * It is also possible to specify an init-callback method that will be called once the SPARK
     * initialization has run and the context info has been added to the iframe's contextWindow. The
     * method can be specified using 'initCallbackFunctionName' and a function with that name has to
     * be added into the SPARK namespace (in the iframe's context). The iframe JavaScript code should
     * first check if the iframeContext info (in SPARK namespace) is already available, and if it is not,
     * it should add the callback method to the SPARK namespace. Order of execution is not guaranteed.
     *
     * @param appBaseUrl base url for the SPA (must already contain trailing '/')
     * @param iframeIdToUse id to use for the iframe element
     * @param iframeContextInfo string to be added to the loaded iframe's window as 'SPARK.iframeContext'
     * @param initCallbackFunctionName a name of the function to be called once SPARK init is done, null for no callback
     * @return the generated HTML code as a string
     */
    public static String generateResizedIframeHtml(String appBaseUrl, String iframeIdToUse,
            String iframeContextInfo, String initCallbackFunctionName) {

        // add (possibly one more) layer of "-escaping so that the string can be added to js variable with "" delimiters
        String escapedIframeContext = iframeContextInfo == null ? "null" :
                iframeContextInfo.replace("\"", "\\\"");

        // no need to allow all possible js variable names, just a reasonable and safe subset
        if ( initCallbackFunctionName != null && ! initCallbackFunctionName.matches("^[a-zA-Z_$][0-9a-zA-Z_$]*$") ) {
            logger.warn("Unsafe initCallbackFunctionName, must match '^[a-zA-Z_$][0-9a-zA-Z_$]*$', was: " + initCallbackFunctionName);
            initCallbackFunctionName = null;
        }

        // script element trying to load the iframeResizer lib
        String body = "<script src='" + appBaseUrl + "libs/spark/iframeResizer.min.js'></script>";

        // main iframe element loading the app content with 'iframe_content=true' param
        body += ("<iframe id='" + iframeIdToUse
            + "' scrolling='no' style='width: 100%; border: none;' src='"
            +    appBaseUrl + "?iframe_content=true'>"
            + "</iframe>");

        // run the iframeResizer initialitazion and add context-info on iframeResizer's init callback
        // (in this implementation context injection won't work without iframeResizer)
        body += ("<script>\n"
                + "if (iFrameResize) {\n"
                + "iFrameResize({\n"
                + " 'autoResize': true,\n"
                + " 'heightCalculationMethod': 'max',\n"
                + " 'initCallback': function(loadedIframe) {"
                + "     var contentWin = loadedIframe.contentWindow;\n"
                + "     if (!contentWin.SPARK) {\n"
                + "         contentWin.SPARK = {};\n"
                + "     }\n"
                + "     contentWin.SPARK.iframeContext = \"" + escapedIframeContext + "\";\n"
                + ( initCallbackFunctionName != null ?
                        "if ( contentWin.SPARK." + initCallbackFunctionName + ") {\n"
                        + " contentWin.SPARK." + initCallbackFunctionName + "();\n"
                        + "}"
                        : ""
                    )
                + " }\n"
                + "}, '#" + iframeIdToUse + "');\n"
                + "}\n"
                + "</script>");

        return body;

    }

}
