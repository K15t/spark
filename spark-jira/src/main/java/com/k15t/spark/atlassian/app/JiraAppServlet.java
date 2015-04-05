package com.k15t.spark.atlassian.app;

import com.k15t.spark.atlassian.AtlassianAppServlet;
import com.k15t.spark.base.AppServlet;
import org.apache.commons.lang.BooleanUtils;


public class JiraAppServlet extends AtlassianAppServlet {

    @Override
    protected boolean isDevMode() {
        return BooleanUtils.toBoolean(System.getProperty(AppServlet.Keys.DEV_MODE));
    }

}
