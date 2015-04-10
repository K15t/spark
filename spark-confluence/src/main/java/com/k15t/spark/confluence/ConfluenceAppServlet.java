package com.k15t.spark.confluence;

import com.atlassian.confluence.core.ConfluenceSystemProperties;
import com.k15t.spark.atlassian.AtlassianAppServlet;


public class ConfluenceAppServlet extends AtlassianAppServlet {

    /**
     * Despite mentioned in https://confluence.atlassian.com/display/DOC/Recognised+System+Properties
     * "confluence.dev.mode" is not included in {@link ConfluenceSystemProperties#isDevMode()}.
     */
    public final static String QUIRKY_DEV_MODE = "confluence.dev.mode";


    @Override
    protected boolean isDevMode() {
        return ConfluenceSystemProperties.isDevMode() ||
                Boolean.getBoolean(QUIRKY_DEV_MODE);
    }

}
