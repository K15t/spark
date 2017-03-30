package com.k15t.spark.confluence;

import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.confluence.spaces.actions.SpaceAware;
import com.opensymphony.webwork.ServletActionContext;

import java.util.List;


/**
 * Class that can be extended for creating an action that opens a SPA in an iframe in the Space Tools view.
 */
public abstract class ConfluenceIframeSpaceAppAction extends AbstractSpaceAction implements SpaceAware, ConfluenceSparkIframeAction {

    private String body;


    /**
     * This method will be called by Confluence to output the iframe SPA wrapper
     * <p/>
     * Override to add permissions checks.
     */
    public String index() {
        this.body =
                ConfluenceIframeSparkActionHelper.renderSparkIframeBody(this, ServletActionContext.getRequest(),
                        "spark_space_adm_iframe_");

        return INPUT;
    }


    @Override
    public String getIframeContextInfo() {
        return "{\"space_key\": \"" + getSpaceKey() + "\"}";
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
        return ConfluenceIframeSparkActionHelper
                .defaultGetSelectedWebItem(ServletActionContext.getRequest(), ServletActionContext.getContext());
    }


    @Override
    public List<String> getRequiredResourceKeys() {
        return ConfluenceIframeSparkActionHelper.defaultGetRequiredResourceKeys(ServletActionContext.getContext());
    }


    @Override
    public boolean isSpaceRequired() {
        return true;
    }


    @Override
    public boolean isViewPermissionRequired() {
        return true;
    }


    /**
     * @return main body html of the iframe wrapper
     */
    public String getBodyAsHtml() {
        return body;
    }

}
