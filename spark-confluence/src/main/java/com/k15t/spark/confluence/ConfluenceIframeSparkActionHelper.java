package com.k15t.spark.confluence;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class ConfluenceIframeSparkActionHelper {

    private static final Logger logger = LoggerFactory.getLogger(ConfluenceIframeSparkActionHelper.class);


    static List<String> splitWebResourceKeys(String webResourceKeys) {
        String keys = StringUtils.defaultString(webResourceKeys);
        return Arrays.stream(StringUtils.split(keys, ','))
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    // Replace this method with a direct call to ConfluenceActionSupport.getActiveRequest().getQueryString() when dropping support for
    // Confluence 9 and older.
    public static String getQueryString(ConfluenceActionSupport action) {
        try {
            @SuppressWarnings({"JavaReflectionMemberAccess", "RedundantSuppression"})
            Method getActiveRequest = ConfluenceActionSupport.class.getDeclaredMethod("getActiveRequest");
            getActiveRequest.setAccessible(true);
            HttpServletRequest request = (HttpServletRequest) getActiveRequest.invoke(action);
            return request != null ? request.getQueryString() : null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.debug("Calling StaticHttpContext.getActiveRequest() failed. This is expected on Confluence versions 8 and 9.", e);
        }

        try {
            @SuppressWarnings({"JavaReflectionMemberAccess", "RedundantSuppression"})
            Method getCurrentRequest = ConfluenceActionSupport.class.getDeclaredMethod("getCurrentRequest");
            getCurrentRequest.setAccessible(true);
            Object javaxRequest = getCurrentRequest.invoke(action);
            if (javaxRequest == null) {
                return null;
            }
            Method getQueryString = javaxRequest.getClass().getMethod("getQueryString");
            return (String) getQueryString.invoke(javaxRequest);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.debug("Calling ConfluenceActionSupport.getCurrentRequest() failed. This is expected on Confluence 10 and later.", e);
        }

        String msg = logger.isDebugEnabled()
                ? "See above stack trace for more information."
                : "Enable DEBUG logging for class 'com.k15t.spark.confluence.ConfluenceIframeSparkActionHelper' for further information.";

        throw new IllegalStateException("None of the code branches for Confluence 8/9 nor 10 worked. " + msg);
    }

}
