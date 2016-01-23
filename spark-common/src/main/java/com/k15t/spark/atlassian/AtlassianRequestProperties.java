package com.k15t.spark.atlassian;

import com.k15t.spark.base.RequestProperties;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

import java.net.URI;
import java.util.Locale;


public class AtlassianRequestProperties extends RequestProperties {

    private String appPrefix;
    private String baseUrl;


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
        // ... enforce to use the configured base url of the underlying system e.g. Confluence instead of url of the http request.
        return super.getUri(baseUrl);
    }

}
