package com.k15t.spark.confluence;

import com.atlassian.confluence.core.ConfluenceSystemProperties;
import com.k15t.spark.atlassian.AtlassianIframeAppServlet;

public class ConfluenceIframeAppServlet extends AtlassianIframeAppServlet {

    @Override
    protected boolean isDevMode() {
        return ConfluenceSystemProperties.isDevMode() ||
                Boolean.getBoolean(ConfluenceAppServlet.QUIRKY_DEV_MODE);
    }

}
