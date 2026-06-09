package com.k15t.spark.jira;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.k15t.spark.atlassian.AtlassianSparkIframeAction;
import com.k15t.spark.base.util.DocumentOutputUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
                "spark_jira_app_iframe_", getNonceId());
        return "input";
    }


    @Override
    public String getIframeContextInfo() {
        return null;
    }


    @Override
    public String getSpaQueryString() {
        return getQueryString();
    }


    // Replace this method with a direct call to getHttpRequest().getQueryString() when dropping support for Jira 10 and older.
    private String getQueryString() {
        try {
            Method getHttpRequest = JiraWebActionSupport.class.getMethod("getHttpRequest");
            Object requestObject = getHttpRequest.invoke(this);
            if (requestObject instanceof HttpServletRequest) {
                return ((HttpServletRequest) requestObject).getQueryString();
            } else if (requestObject != null) {
                Method getQueryString = requestObject.getClass().getMethod("getQueryString");
                return (String) getQueryString.invoke(requestObject);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Could not retrieve current request or its query string.", e);
        }
        return null;
    }


    // Replace this method with a direct call to getHttpRequest().getAttribute() when dropping support for Jira 10 and older.
    private String getNonceId() {
        try {
            Method getHttpRequest = JiraWebActionSupport.class.getMethod("getHttpRequest");
            Object requestObject = getHttpRequest.invoke(this);
            if (requestObject instanceof HttpServletRequest) {
                return (String) ((HttpServletRequest) requestObject).getAttribute(DocumentOutputUtil.NONCE_ID_REQUEST_ATTRIBUTE);
            } else if (requestObject != null) {
                Method getAttribute = requestObject.getClass().getMethod("getAttribute", String.class);
                return (String) getAttribute.invoke(requestObject, DocumentOutputUtil.NONCE_ID_REQUEST_ATTRIBUTE);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Could not retrieve current request or its attributes.", e);
        }
        return null;
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
