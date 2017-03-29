package com.k15t.spark.atlassian;

import com.k15t.spark.base.RequestProperties;
import com.k15t.spark.base.util.DocumentOutputUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;


/**
 * The servlet implementation (or sub-class) to use for SPARK iframe functionality.
 */
public abstract class AtlassianIframeContentServlet extends AtlassianAppServlet {

    // AtlassianAppServlet handles some heavy lifting required for living in the plugin servlet environment
    // eg. finding out the real servlet path and handling caching in that environment


    @Override
    protected String prepareIndexHtml(String indexHtml, RequestProperties props) throws IOException {
        Document document = Jsoup.parse(indexHtml, props.getUri().toString());

        if (!isDevMode()) {
            applyCacheKeysToResourceUrls(document, props);
        }

        prepareIframeContentIndex(document);
        customizeIframeContentDocument(document);

        document.outputSettings().prettyPrint(false);
        indexHtml = document.outerHtml();
        return indexHtml;
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
    private void prepareIframeContentIndex(Document document) throws IOException {
        // load contentWindow part of the iFrameResizer (inject as inline script), otherwise left the app untouched
        String iframeResizerContentWindowJs = DocumentOutputUtil.getIframeContentWindowJs();
        document.head().append("\n<script>\n" + iframeResizerContentWindowJs + "\n</script>\n");
    }


    /**
     * Callback that can be implemented by sub classes in order to modify the iframe content document, for example to inject information.
     */
    protected void customizeIframeContentDocument(Document document) {
        // Noop by default
    }


    @Override
    protected boolean hasPermissions(RequestProperties props) {
        // TODO this could also be turned into an abstract method
        return true;
    }

}
