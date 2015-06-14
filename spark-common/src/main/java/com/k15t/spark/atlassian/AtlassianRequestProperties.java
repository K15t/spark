package com.k15t.spark.atlassian;

import com.k15t.spark.base.RequestProperties;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

import java.util.Locale;


public class AtlassianRequestProperties extends RequestProperties {

    protected final String appPrefix;


    public AtlassianRequestProperties(AtlassianAppServlet appServlet, HttpServletRequest request, String appPrefix, Locale local) {
        super(appServlet, request);
        this.appPrefix = appPrefix;
        this.locale = local;
    }


    @Override
    protected String getUrlLocalPart() {
        if (urlLocalPart == null) {
            urlLocalPart = StringUtils.removeStart(request.getPathInfo(), appPrefix);
        }

        return StringUtils.defaultString(urlLocalPart);
    }

}
