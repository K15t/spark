package com.k15t.spark.confluence;

import com.atlassian.confluence.core.ConfluenceSystemProperties;
import com.k15t.spark.atlassian.AtlassianIframeContentServlet;


public class ConfluenceIframeContentServlet extends AtlassianIframeContentServlet {

    @Override
    protected boolean isDevMode() {
        return ConfluenceSystemProperties.isDevMode() ||
                Boolean.getBoolean(ConfluenceAppServlet.QUIRKY_DEV_MODE);
    }

}
