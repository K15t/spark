package com.k15t.spark.atlassian;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.user.UserManager;
import com.k15t.spark.base.RequestProperties;
import com.k15t.spark.base.util.DocumentOutputUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.context.ApplicationContext;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


/**
 * The servlet implementation (or sub-class) to use for SPARK iframe functionality.
 */
public abstract class AtlassianIframeContentServlet extends AtlassianAppServlet {

    protected AtlassianIframeContentServlet(LoginUriProvider loginUriProvider, UserManager userManager, LocaleResolver localeResolver,
            ApplicationProperties applicationProperties, ApplicationContext applicationContext) {

        super(loginUriProvider, userManager, localeResolver, applicationProperties, applicationContext);
    }

    // AtlassianAppServlet handles some heavy lifting required for living in the plugin servlet environment
    // eg. finding out the real servlet path and handling caching in that environment


    @Override
    protected String customizeHtml(String indexHtml, RequestProperties props) throws IOException {
        Document document = Jsoup.parse(indexHtml, props.getUri().toString());

        if (!isDevMode()) {
            applyCacheKeysToResourceUrls(document, props);
        }

        // inject the scripts needed for correct operation in a SPARK controlled iframe to the document
        String iframeContentWindowJs = DocumentOutputUtil.getIframeContentWindowJs();
        document.head().prepend("\n<script>\n" + iframeContentWindowJs + "\n</script>\n");

        customizeIframeContentDocument(document, props);

        document.outputSettings().prettyPrint(false);
        indexHtml = document.outerHtml();
        return indexHtml;
    }


    /**
     * Callback that can be implemented by sub classes in order to modify the iframe content document, for example to inject information.
     */
    protected void customizeIframeContentDocument(Document document, RequestProperties props) {
        // Noop by default
    }


    @Override
    protected boolean verifyPermissions(RequestProperties props, HttpServletResponse response) throws IOException {
        return true;
    }

}
