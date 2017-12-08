package com.k15t.spark.atlassian;

import com.k15t.spark.base.Keys;

import java.util.List;


public interface AtlassianSparkIframeAction {

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
    public String getIframeContextInfo();


    /**
     * <p>
     * The query parameter to add as the current parameter to the url of the SPA app when it is loaded into
     * the iframe context
     * </p><p>
     * Default implementation returns the query string used when running the action
     * </p>
     *
     * @return query string to use when loading the SPA in the iframe
     */
    public String getSpaQueryString();


    /**
     * Returns the base url of the single page application relative to the Confluence/JIRA context path. Can be overwritten by subclasses.
     * This default implementation evaluates the action parameter with name {@link Keys#SPARK_SELECTED_WEB_ITEM_KEY}.
     *
     * @return the base url of the spa app
     */
    public String getSpaBaseUrl();


    /**
     * @return string to be used as the title of the iframe wrapper page
     */
    public String getTitleAsHtml();


    /**
     * Returns the complete module key of the space tools web-item to be marked as selected. Can be overwritten by subclasses.
     * This default implementation first checks for a request parameter and then for an action parameter with name
     * {@link Keys#SPARK_SELECTED_WEB_ITEM_KEY}. If none are present {@code null} is returned.
     */
    public String getSelectedWebItem();


    /**
     * @return main body html of the iframe wrapper
     */
    public String getBodyAsHtml();


    /**
     * <p>
     * Returns a list of 'complete module keys' to resources that should be required into the template using #requireResource.
     * </p><p>
     * A #requireResource instruction is added for every 'complete module key' string in the list at the end of the head element of
     * the loaded template.
     * </p>
     *
     * @return a list of keys to require when loading the action template
     */
    public List<String> getRequiredResourceKeys();

}
