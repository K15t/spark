package com.k15t.spark.base;

/**
 * Loads a message bundle in the corresponding locale.
 */
public interface MessageBundleProvider {

    /**
     * Checks if the requested resource is a message bundle and this provider is responsible to
     * load and response the bundle to the client.
     *
     * @return {@code true} if the provider is responsible for it
     */
    boolean isMessageBundle(RequestProperties props);


    /**
     * Loads the bundle and return it as string representation.
     */
    String loadBundle(RequestProperties props);


    /**
     * Gets the content-type of the file set on the response.
     */
    String getContentType();
}
