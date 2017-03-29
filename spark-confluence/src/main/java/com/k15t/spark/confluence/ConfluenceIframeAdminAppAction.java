package com.k15t.spark.confluence;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.opensymphony.webwork.ServletActionContext;


public abstract class ConfluenceIframeAdminAppAction extends ConfluenceActionSupport implements ConfluenceSparkIframeAction {

    private String body;


    @Override
    public boolean isPermitted() {
        return permissionManager.isConfluenceAdministrator(getAuthenticatedUser());
    }


    /**
     * This method will be called by Confluence to output the iframe SPA wrapper
     * <p/>
     * Override to add permissions checks.
     */
    public String index() {

        this.body = ConfluenceIframeSparkActionHelper.renderSparkIframeBody(this,
                ServletActionContext.getRequest(), "spark_admin_iframe_");

        return INPUT;
    }


    @Override
    public String getIframeContextInfo() {
        return "admin";
    }


    @Override
    public String getIframeContextInitializedCallbackName() {
        return "contextInitializedCallback";
    }


    @Override
    public String getSpaQueryString() {
        return ServletActionContext.getRequest().getQueryString();
    }


    @Override
    public String getSpaBaseUrl() {
        return ConfluenceIframeSparkActionHelper.defaultGetSpaBaseUrl(ServletActionContext.getContext());
    }


    @Override
    public String getSelectedWebItem() {
        return ConfluenceIframeSparkActionHelper.defaultGetSelectedWebItem(
                ServletActionContext.getRequest(), ServletActionContext.getContext());
    }


    @Override
    public String getBodyAsHtml() {
        return body;
    }

}