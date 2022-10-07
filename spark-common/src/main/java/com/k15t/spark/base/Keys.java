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

}
