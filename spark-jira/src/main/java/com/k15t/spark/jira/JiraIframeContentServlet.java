package com.k15t.spark.jira;

import com.k15t.spark.atlassian.AtlassianIframeContentServlet;
import org.apache.commons.lang.BooleanUtils;


public class JiraIframeContentServlet extends AtlassianIframeContentServlet {

    @Override
    protected boolean isDevMode() {
        return BooleanUtils.toBoolean(System.getProperty("atlassian.dev.mode"));
    }
}
