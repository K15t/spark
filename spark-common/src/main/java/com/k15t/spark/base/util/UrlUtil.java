package com.k15t.spark.base.util;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class UrlUtil {

    public static String rebaseUrl(String baseUrl, String relativeUrl) {
        StringBuilder url = new StringBuilder(StringUtils.removeEnd(baseUrl, "/"));

        if (StringUtils.isNotEmpty(relativeUrl)) {
            List<String> segments = new ArrayList<>();
            segments.addAll(Arrays.asList(StringUtils.split(StringUtils.defaultString(relativeUrl), '/')));
            for (String segment : segments) {
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
