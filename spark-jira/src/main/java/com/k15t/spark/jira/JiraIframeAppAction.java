package com.k15t.spark.jira;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.k15t.spark.atlassian.AtlassianSparkIframeAction;
import com.k15t.spark.base.util.DocumentOutputUtil;

import java.util.Collections;
import java.util.List;


public abstract class JiraIframeAppAction extends JiraWebActionSupport implements AtlassianSparkIframeAction {

    private String body;


    /**
     * This method will be called by Jira to output the iframe SPA wrapper
     * <p/>
     * Override to add permissions checks.
     */
    protected String doExecute() {
        this.body = DocumentOutputUtil.renderSparkIframeBody(getSpaBaseUrl(), getSpaQueryString(), getIframeContextInfo(),
                "spark_jira_app_iframe_");
        return "input";
    }


    @Override
    public String getIframeContextInfo() {
        return null;
    }


    @Override
    public String getSpaQueryString() {
        return getHttpRequest().getQueryString();
    }


    @Override
    public String getTitleAsHtml() {
        return "Spark Iframe";
    }


    @Override
    public String getBodyAsHtml() {
        return body;
    }


    @Override
    public List<String> getRequiredResourceKeys() {
        return Collections.emptyList();
    }

}
