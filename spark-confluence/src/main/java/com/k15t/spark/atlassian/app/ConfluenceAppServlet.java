package com.k15t.spark.atlassian.app;

import com.k15t.spark.atlassian.AtlassianAppServlet;
import com.k15t.spark.base.AppServlet;
import org.apache.commons.lang3.BooleanUtils;


public class ConfluenceAppServlet extends AtlassianAppServlet {

    @Override
    protected boolean isDevMode() {
        return BooleanUtils.toBoolean(System.getProperty(AppServlet.Keys.DEV_MODE)) ||
                BooleanUtils.toBoolean(System.getProperty(Keys.CONF_DEV_MODE_1)) ||
                BooleanUtils.toBoolean(System.getProperty(Keys.CONF_DEV_MODE_2));
    }


    protected class Keys {
        public final static String CONF_DEV_MODE_1 = "confluence.devmode";
        public final static String CONF_DEV_MODE_2 = "confluence.dev.mode";
    }

}
