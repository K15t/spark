package com.k15t.spark.confluence;

import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.k15t.spark.base.util.DocumentOutputUtil;
import com.opensymphony.webwork.ServletActionContext;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;


/**
 * Class that can be extended for creating an action that opens a SPA in an iframe in the Space Tools view.
 */
public class ConfluenceIframeSpaceAppAction extends ConfluenceSpaceAppAction {

    @Override
    protected final String prepareBody(Document document) throws IOException {
        String appBaseUrl = getAppBaseUrl(document);

        String template = DocumentOutputUtil.getIframeAdminContentWrapperTemplate();
        Map<String, Object> context = DocumentOutputUtil.generateAdminIframeTemplateContext(
                appBaseUrl, "spark_space_adm_iframe",
                getIframeContextInfo(), getIframeContextInitializedCallbackName(), getSpaQueryString());

        return VelocityUtils.getRenderedContent(template, context);
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
        Space space = getSpace();

        String res = "{\"space_key\": \"" + space.getKey() + "\"}";

        return res;
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


}
