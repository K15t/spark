package com.k15t.spark.base;


public class Keys {

    /**
     * The param key used in the atlassian-plugin.xml servlet or xwork configuration.
     */
    public static final String RESOURCE_PATH = "resource-path";

    /**
     * The system property key to be used for the SPARK development directory. Must contain a comma-separated list of absolute paths
     * representing the base directories to which the app resource paths are relative to. Rule of thumb: one directory for each plugin.
     *
     * @see AppServlet#loadFromDevelopmentDirectory(java.lang.String)
     */
    public static final String SPARK_DEV_RESOURCE_DIRECTORIES = "spark.dev.dir";

    /**
     * Param key to configure to use a specific message bundle provider for using ng-translate.
     */
    public static final String NG_TRANS_MSG_BUNDLE = "ng-translate-message-bundle";

    /**
     * The param key used in the atlassian-plugin.xml action or servlet configuration for specifying the item to select
     * in the admin side menu
     */
    public static final String SPARK_SELECTED_WEB_ITEM_KEY = "spark-selected-web-item-key";

    /**
     * The param key used in the atlassian-plugin.xml action or servlet configuration for specifying the SPA base url.
     */
    public static final String SPARK_SPA_BASE_URL = "spark-spa-base-url";

    /**
     * <p>
     * The param key used in the atlassian-plugin.xml for specifying a list of resources that should be required
     * when loading certain SPARK action.
     * </p><p>
     * The parameter value should be a comma-separated list of complete web-resource module keys
     * </p>
     */
    public static final String SPARK_REQUIRED_WEB_RESOURCE_KEYS = "spark-required-web-resource-keys";

}
