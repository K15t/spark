package com.k15t.spark.atlassian;

import com.k15t.spark.base.RequestProperties;

import javax.servlet.http.HttpServletRequest;

import java.net.URI;
import java.util.Locale;


public class AtlassianRequestProperties extends RequestProperties {

    private final String baseUrl;


    /**
     * Constructs the specific properties for an Atlassian product e.g. Confluence or Jira.
     *
     * @param baseUrl Url which should be used as base. It is recommended to use a relative one
     * @param locale Local to provide the best matching message properties for
     */
    public AtlassianRequestProperties(
            AtlassianAppServlet appServlet, HttpServletRequest request, String baseUrl, Locale locale) {

        super(appServlet, request);

        if ("/plugins/servlet".equals(request.getServletPath())) {
            // This indicates usage of an url-pattern like '/app/ui*', which makes request.getServletPath() and request.getPathInfo() return
            // unexpected values, breaking RequestProperties.getUrlLocalPart in turn.
            throw new IllegalStateException("\n\n"
                    + "=============================================== Spark setup error ================================================\n"
                    + "The configuration for servlet '" + appServlet.getClass().getName() + "' in atlassian-plugin.xml is incorrect:\n"
                    + "<url-pattern> elements with path segments containing static prefixes mixed with asterisk (e.g. "
                    + "'<url-pattern>/my-servlet*</url-pattern>') are not supported because that breaks in-app path detection.\n\n"
                    + "To fix this place the asterisk into a separate path segment: <url-pattern>/my-servlet/*</url-pattern>\n"
                    + "Add another pattern if the servlet must be available without trailing '/': <url-pattern>/my-servlet</url-pattern>\n"
                    + "==================================================================================================================\n"
            );
        }

        super.locale = locale;
        this.baseUrl = baseUrl;
    }


    @Override
    public URI getUri() {
        return super.getUri(baseUrl);
    }

}
