package com.k15t.spark.confluence;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.k15t.spark.atlassian.AtlassianSparkIframeAction;
import com.opensymphony.webwork.ServletActionContext;

import java.util.List;


public abstract class ConfluenceIframeAdminAppAction extends ConfluenceActionSupport implements AtlassianSparkIframeAction {

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
    public List<String> getRequiredResourceKeys() {
        return ConfluenceIframeSparkActionHelper.defaultGetRequiredResourceKeys(ServletActionContext.getContext());
    }


    @Override
    public String getBodyAsHtml() {
        return body;
    }

}