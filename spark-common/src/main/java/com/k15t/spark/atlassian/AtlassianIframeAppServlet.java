package com.k15t.spark.atlassian;

import com.k15t.spark.base.Keys;
import com.k15t.spark.base.RequestProperties;
import com.k15t.spark.base.util.DocumentOutputUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * The servlet implementation (or sub-class) to use for SPARK iframe functionality.
 */
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
     * <p>
     * Serves the index page in iframe content mode
     * </p>
     * <p>
     * The contentWindow part of the iFrameResizer will be added to the document as inline script,
     * otherwise the document will be left untouched.
     * </p>
     *
     * @param document {@link Document} of the index before processing
     */
    private void prepareIframeContentIndex(Document document) {

        // load contentWindow part of the iFrameResizer (inject as inline script), otherwise left the app untouched

        String iframeResizerContentWindowJs = DocumentOutputUtil.getIframeResizeContentWindowJs();
        document.head().append("\n<script>\n" + iframeResizerContentWindowJs + "\n</script>\n");

    }


    /**
     * <p>
     * Parse (or generate) a document to be used as the wrapper for the SPA with an iframe and needed decorators
     * to get the admin view.
     * </p><p>
     * Adds the Atlassian admin decorator meta element and a content decorator element telling which webitem should
     * be marked as selected. Then adds as the main body an iframe element that will ask loading the actual SPA.
     * </p><p>
     * Also adds iFrameResizer main window part to the page and some inline JavaScript needed for communicating all
     * the context values to the contents of the iframe.
     * </p>
     *
     * @param document {@link Document} of the index before processing
     * @param props {@link RequestProperties} of the current request
     */
    private void prepareAdminIframeWrapperIndex(Document document, RequestProperties props) throws IOException {

        // add atlassian admin decorator meta element

        Element metaElement = document.head().appendElement("meta");
        metaElement.attr("name", "decorator");
        metaElement.attr("content", "atl.admin");

        // remove all the styles and wrappers from the iframe parent, they will only be needed in the actual content iframe
        document.head().children().not("title,meta,content").remove();

        // fill the body with an iframe that will ask the actual app with 'iframe_content' query parameter
        // remove all other content - it will be shown when the app is loaded with iframe_content parameter

        document.body().children().remove();

        Element contEl = document.body().appendElement("content");
        contEl.attr("tag", "selectedWebItem");
        contEl.text(getSelectedWebItemKey());

        String iframeHtml = getTemplateRenderer().renderFragment(
                DocumentOutputUtil.getIframeAdminContentWrapperTemplate(),
                DocumentOutputUtil.generateAdminIframeTemplateContext(
                        props.getRequest().getRequestURI(), "spark_admin_iframe",
                        "admin", null, props.getRequest().getQueryString()));

        document.body().append(iframeHtml);

    }


    /**
     * Return the key of the web item. Needed for selecting the correct item in the admin side menu. The default implementation
     * returns the value of servlet init parameter {@link Keys#SPARK_SELECTED_WEB_ITEM_KEY}
     *
     * @return key of the web item that should be marked as selected in the admin side menu
     */
    public String getSelectedWebItemKey() {
        String res = getServletConfig().getInitParameter(Keys.SPARK_SELECTED_WEB_ITEM_KEY);

        return res != null ? res : "";
    }

}
