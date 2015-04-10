package com.k15t.spark.confluence;

import com.k15t.spark.atlassian.AtlassianAppServlet;
import org.apache.commons.lang.BooleanUtils;


public class JiraAppServlet extends AtlassianAppServlet {

    @Override
    protected boolean isDevMode() {
        // TODO check, if there are JIRA specific system properties and use them
        return BooleanUtils.toBoolean(System.getProperty("atlassian.dev.mode"));
    }

}
