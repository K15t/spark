package com.k15t.spark.base.util;

import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
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
     */
    public static String getIframeAdminContentWrapperTemplate() throws IOException {

        try ( InputStream templateStream =
                      DocumentOutputUtil.class.getClassLoader().getResourceAsStream("/com/k15t/spark/content_iframe_wrapper.vm") ) {
            return IOUtils.toString(templateStream, "UTF-8");
        }
        // TODO could cache to template
    }

    /**
     * <p>
     * Generates velocity context from the parameters (and a few constants) that is suitable to be used
     * with {@link #getIframeAdminContentWrapperTemplate()} velocity template
     * </p><p>
     * It is possible to pass an information string to the content window of the iframe by using the
     * argument 'iframeContextInfo' (it will be available at SPARK.iframeContext). The contents will
     * be added as a JS string, but that string can contain eg. JSON info that the iframe JS code can parse.
     * </p><p>
     * It is also possible to specify an init-callback method that will be called once the SPARK
     * initialization has run and the context info has been added to the iframe's contextWindow. The
     * method can be specified using 'initCallbackFunctionName' and a function with that name has to
     * be added into the SPARK namespace (in the iframe's context). The iframe JavaScript code should
     * first check if the iframeContext info (in SPARK namespace) is already available, and if it is not,
     * it should add the callback method to the SPARK namespace. Order of execution is not guaranteed.
     * </p>
     *
     * @param appBaseUrl base url for the SPA (must already contain trailing '/')
     * @param iframeIdToUse id to use for the iframe element
     * @param iframeContextInfo string to be added to the loaded iframe's window as 'SPARK.iframeContext'
     * @param initCallbackFunctionName a name of the function to be called once SPARK init is done, null for no callback
     * @return velocity context ready to be used with the iframe-admin-wrapper velocity template
     */
    public static Map<String, Object> generateAdminIframeTemplateContext(
            String appBaseUrl, String iframeIdToUse,
            String iframeContextInfo, String initCallbackFunctionName) throws IOException {

        // add (possibly one more) layer of "-escaping so that the string can be added to js variable with "" delimiters
        String escapedIframeContext = iframeContextInfo == null ? "null" :
                iframeContextInfo.replace("\"", "\\\"");

        String iframeSource = appBaseUrl + "?iframe_content=true";

        // no need to allow all possible js variable names, just a reasonable and safe subset
        if ( initCallbackFunctionName != null && ! initCallbackFunctionName.matches("^[a-zA-Z_$][0-9a-zA-Z_$]*$") ) {
            logger.warn("Unsafe initCallbackFunctionName, must match '^[a-zA-Z_$][0-9a-zA-Z_$]*$', was: " + initCallbackFunctionName);
            initCallbackFunctionName = null;
        }

        String iframeResizerJs = "";
        try ( InputStream iframeResizeFile =
            DocumentOutputUtil.class.getClassLoader().getResourceAsStream("/com/k15t/spark/iframeResizer.min.js")) {
            iframeResizerJs = IOUtils.toString(iframeResizeFile, "UTF-8");
        }

        HashMap<String, Object> context = new HashMap<String, Object>();

        context.put("iframeResizerJs", iframeResizerJs);
        context.put("iframeId", iframeIdToUse);
        context.put("iframeSrc", iframeSource);
        context.put("escapedIframeContext", escapedIframeContext);
        context.put("iframeInitCallback", initCallbackFunctionName);

        return context;

    }

}
