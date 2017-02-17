package com.k15t.spark.atlassian;

import com.k15t.spark.base.Keys;
import com.k15t.spark.base.RequestProperties;
import com.k15t.spark.base.util.DocumentOutputUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

import java.io.IOException;


public abstract class AtlassianIframeAppServlet extends AtlassianAppServlet {

    private static final Logger logger = LoggerFactory.getLogger(AtlassianIframeAppServlet.class);

    @Override
    protected String prepareIndexHtml(String indexHtml, RequestProperties props) throws IOException {

        Document document = Jsoup.parse(indexHtml, props.getUri().toString());

        if (!isDevMode()) {
            applyCacheKeysToResourceUrls(document, props);
        }

        if ( isAskingIframeContent(props)) {

            prepareIframeContentIndex(document);

        } else {

            // serve as an admin page, this is the only way in which index is asked without iframe parameter at the moment,
            // dialog-mode adds the wrapper with JavaScript, and Space-action using Velocity

            prepareAdminIframeWrapperIndex(document, props);

        }

        // don't let jsoup generate unwanted blanks. Otherwise decorator settings
        // like <content tag="selectedWebItem">...</content> don;t work.
        document.outputSettings().prettyPrint(false);
        indexHtml = document.outerHtml();
        return indexHtml;
    }

    /**
     * @param properties current {@link RequestProperties}
     * @return true if the page is meant to be shown in an iframe
     */
    private boolean isAskingIframeContent(RequestProperties properties) {
        String iframeContentValue =
                properties.getRequest().getParameter("iframe_content");

        return "true".equals(iframeContentValue);
    }


    /**
     * Removes Atlassian decorators from the document to be served inside the iframe
     *
     * All script etc. elements and the main content of the document are left untouched
     *
     * Removes all the "meta" elements from the documents head that are not marked to be kept also
     * for the iframe content by setting an attribute "spark" to value "iframe_keep" on the element.
     * Also removes "content" elements from the body.
     *
     * Correct iframe-resizing operation requires that the libs/spark path contains iframeResizer.min.js and
     * iframeResizer.contentWindow.min.js files (the required script elements will be added automatically)
     *
     * @param document {@link Document} of the index before processing
     */
    private void prepareIframeContentIndex(Document document) {

        // remove meta arguments not marked to be kept also in the iframe, and other decorators
        // also load contentWindow part of the iFrameResizer (inject as inline script), otherwise left the app untouched

        document.head().children().select("meta").not("[spark=iframe]").remove();
        document.head().children().select("meta").removeAttr("spark");

        String iframeResizerContentWindowJs = DocumentOutputUtil.getIframeResizeContentWindowJs();
        document.head().append("\n<script>\n" + iframeResizerContentWindowJs + "\n</script>\n");

        document.body().children().select("content").remove();

    }


    /**
     * Parse the part of the index document that is to be used as the main level page containing
     * all needed Atlassian decorators and embedding rest of the content in a iframe
     *
     * Removes elements from the head that don't look like Atlassian decorators (eg. scripts and styles),
     * and also removes meta elements that are marked to be relevant to the iframe content by setting
     * "spark" attribute
     *
     * The body of the index-document will be substituted (except for "content" elements that might contain
     * instructions for the Atlassian decorators) with an iframe. The same URL of the request will be set
     * as the source of the iframe but with an extra query parameter telling that it is the content
     * (then the {@link #prepareIframeContentIndex(Document)} method will be used instead).
     *
     * Correct iframe-resizing operation requires that the libs/spark path contains iframeResizer.min.js and
     * iframeResizer.contentWindow.min.js files (the required script elements will be added automatically)
     *
     * @param document {@link Document} of the index before processing
     * @param props {@link RequestProperties} of the current request
     */
    private void prepareAdminIframeWrapperIndex(Document document, RequestProperties props) throws IOException {


        addAdminIframeWrapperMetaTags(document);

        // remove all the styles and wrappers from the iframe parent, they will only be needed in the actual content iframe
        document.head().children().not("title,meta,content").remove();
        // also remove meta attributes that the user has specified to be used when loaded into iframe
        document.head().children().select("meta").select("[spark=iframe]").remove();

        // fill the body with an iframe that will ask the actual app with 'iframe_content' query parameter
        // remove all other (non-decorator) content - it will be shown when the app is loaded with iframe_content parameter

        document.body().children().not("content").remove();

        addAdminIframeBodyDecorators(document);

        String iframeHtml = getTemplateRenderer().renderFragment(
                DocumentOutputUtil.getIframeAdminContentWrapperTemplate(),
                DocumentOutputUtil.generateAdminIframeTemplateContext(
                        props.getRequest().getRequestURI(), "spark_admin_iframe",
                        "admin", null, props.getRequest().getQueryString()));

        document.body().append(iframeHtml);

    }

    protected String getMetaDecoratorSpec() {
        return getServletConfig().getInitParameter(Keys.META_DECORATORS_FOR_ADMIN_IFRAME_WRAPPER);
    }

    protected String getBodyContentDecoratorSpec() {
        return getServletConfig().getInitParameter(Keys.BODY_DECORATORS_FOR_ADMIN_IFRAME_WRAPPER);
    }

    private void addAdminIframeWrapperMetaTags(Document document) {

        for ( Entry<String, String> pair : parsePairListInitParam(getMetaDecoratorSpec())) {
            Element metaElement = document.head().appendElement("meta");

            metaElement.attr("name", pair.getKey());
            metaElement.attr("content", pair.getValue());

        }

    }

    private void addAdminIframeBodyDecorators(Document document) {

        for ( Entry<String, String> pair : parsePairListInitParam(getBodyContentDecoratorSpec()) ) {

            Element contentDecoratorElement = document.body().appendElement("content");

            contentDecoratorElement.attr("tag", pair.getKey());
            contentDecoratorElement.text(pair.getValue());

        }

    }

    private List<Entry<String, String>> parsePairListInitParam(String initParam) {

        List<Entry<String, String>> res = new ArrayList<>();

        if ( initParam != null ) {
            String[] metaTags = initParam.split(";");

            for (String metaTag : metaTags) {

                String[] parts = metaTag.split(":");

                if ( parts.length == 2 ) {

                    String name = parts[0].trim();
                    String content = parts[1].trim();

                    SimpleEntry<String, String> pair = new SimpleEntry<String, String>(name, content);

                    res.add(pair);

                } else {

                    logger.warn("Illegal meta tag init-param: " + metaTag);

                }

            }
        }

        return res;

    }


}
