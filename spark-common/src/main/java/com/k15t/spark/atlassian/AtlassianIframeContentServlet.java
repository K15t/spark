package com.k15t.spark.atlassian;

import com.k15t.spark.base.RequestProperties;
import com.k15t.spark.base.util.DocumentOutputUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.servlet.http.HttpServletResponse;

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

        // inject the scripts needed for correct operation in a SPARK controlled iframe to the document
        String iframeContentWindowJs = DocumentOutputUtil.getIframeContentWindowJs();
        document.head().prepend("\n<script>\n" + iframeContentWindowJs + "\n</script>\n");

        customizeIframeContentDocument(document);

        document.outputSettings().prettyPrint(false);
        indexHtml = document.outerHtml();
        return indexHtml;
    }


    /**
     * Callback that can be implemented by sub classes in order to modify the iframe content document, for example to inject information.
     */
    protected void customizeIframeContentDocument(Document document) {
        // Noop by default
    }


    @Override
    protected boolean verifyPermissions(RequestProperties props, HttpServletResponse response) throws IOException {
        return true;
    }

}
