package com.k15t.spark.atlassian;

import com.k15t.spark.base.RequestProperties;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

import java.net.URI;
import java.util.Locale;


public class AtlassianRequestProperties extends RequestProperties {

    private String appPrefix;
    private String baseUrl;


    /**
     * Constructs the specific properties for an Atlassian product e.g. Confluence or Jira.
     *
     * @param baseUrl Url which should be used as base. It is recommend to use a relative one
     * @param appPrefix Configured url pattern of the servlet configuration
     * @param local Local to provide the best matching message properties for
     */
    public AtlassianRequestProperties(
            AtlassianAppServlet appServlet, HttpServletRequest request, String baseUrl, String appPrefix, Locale local) {

        super(appServlet, request);
        this.appPrefix = appPrefix;
        this.locale = local;
        this.baseUrl = baseUrl;
    }


    @Override
    protected String getUrlLocalPart() {
        if (urlLocalPart == null) {
            urlLocalPart = StringUtils.removeStart(request.getPathInfo(), appPrefix);
        }
        return StringUtils.defaultString(urlLocalPart);
    }


    @Override
    public URI getUri() {
        return super.getUri(baseUrl);
    }

}
