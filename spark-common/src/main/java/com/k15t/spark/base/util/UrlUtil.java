package com.k15t.spark.base.util;

import org.apache.commons.lang3.StringUtils;


public class UrlUtil {

    public static String rebaseUrl(String baseUrl, String relativeUrl) {
        StringBuilder url = new StringBuilder(StringUtils.removeEnd(baseUrl, "/"));

        if (StringUtils.isNotEmpty(relativeUrl)) {
            for (String segment : StringUtils.split(StringUtils.defaultString(relativeUrl), '/')) {
                url.append("/").append(segment);
            }
            // Add trailing slash if required.
            if (StringUtils.endsWith(relativeUrl, "/")) {
                url.append("/");
            }
        } else if (StringUtils.endsWith(baseUrl, "/")) {
            url.append("/");
        }

        return url.toString();
    }

}
