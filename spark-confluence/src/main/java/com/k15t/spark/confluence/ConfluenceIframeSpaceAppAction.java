package com.k15t.spark.confluence;

import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.confluence.spaces.actions.SpaceAware;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.k15t.spark.base.util.DocumentOutputUtil;
import com.opensymphony.webwork.ServletActionContext;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Map;


/**
 * Class that can be extended for creating an action that opens a SPA in an iframe in the Space Tools view.
 */
public abstract class ConfluenceIframeSpaceAppAction extends AbstractSpaceAction implements SpaceAware {

    private String body;


    /**
     * This method will be called by Confluence to output the iframe SPA wrapper
     * <p/>
     * Override to add permissions checks.
     */
    public String index() {

        try {
            String appBaseUrl = ServletActionContext.getRequest().getContextPath() + "/" +
                    StringUtils.removeStart(getSpaBaseUrl(), "/");
            long idSuffix = System.currentTimeMillis();

            String template = DocumentOutputUtil.getIframeAdminContentWrapperTemplate();
            Map<String, Object> context = DocumentOutputUtil.generateAdminIframeTemplateContext(
                    appBaseUrl, "spark_space_adm_iframe_" + idSuffix,
                    getIframeContextInfo(), getIframeContextInitializedCallbackName(), getSpaQueryString());

            this.body = VelocityUtils.getRenderedContent(template, context);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load iframe-space app template");
        }

        return INPUT;
    }


    /**
     * <p>
     * The result of this method will be injected into the context of the loaded iframe as SPARK.iframeContext
     * </p><p>
     * The JS variable will be a string. To pass structured information eg. JSON can be used.
     * </p><p>
     * This is the main method of injecting context specific information to a space app iframe. The passed
     * information can be customized to fit the needed use case by sub-classing this class with an
     * action class that will override this method (and by using that class in the correct atlassian module
     * specification).
     * </p>
     *
     * @return string that will be attached to SPARK.iframeContext variable as a JS string
     */
    protected String getIframeContextInfo() {
        return "{\"space_key\": \"" + getSpaceKey() + "\"}";
    }


    /**
     * <p>
     * It is possible to specify a callback function name that will be called once the context information
     * is injected into the iframe.
     * </p><p>
     * A function with the given name must be present in the SPARK object in the iframe's global context. If the
     * function with given name (or SPARK) object is not present, nothing is called. This can happen also in
     * normal operation because of initialization race conditions.
     * </p><p>
     * Correct way to use the init-callback method is to first check in the iframe's normal init-method to check
     * whether the SPARK.iframeContext already is present, and if not, then add the SPARK object (if needed) and
     * the init method with correct name to it.
     * </p><p>
     * Defaults to 'contextInitializedCallback'. If no initialization method is needed, null can be returned.
     * </p>
     *
     * @return name of an initialization callback (on SPARK global object in iframe's context), or null if not needed
     */
    protected String getIframeContextInitializedCallbackName() {
        return "contextInitializedCallback";
    }


    /**
     * <p>
     * The query parameter to add as the current parameter to the url of the SPA app when it is loaded into
     * the iframe context ("iframe_content=true" will always be added and used by the SPARK framework)
     * </p><p>
     * Default implementation returns the query string used when running the action
     * </p>
     *
     * @return query string to use when loading the SPA in the iframe
     */
    protected String getSpaQueryString() {
        return ServletActionContext.getRequest().getQueryString();
    }


    /**
     * <p>
     * Returns the base url of the single page application (the browser visible url to the app resources
     * relative to the Conflunce context path)
     * </p>
     *
     * @return the base url of the spa app
     */
    protected abstract String getSpaBaseUrl();


    /**
     * @return string to be used as the title of the iframe wrapper page
     */
    public abstract String getTitleAsHtml();


    /**
     * @return space tools item to be marked as selected
     */
    public abstract String getSelectedSpaceToolsWebItem();


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
