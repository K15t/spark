package com.k15t.spark.confluence;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class ConfluenceIframeSparkActionHelper {

    static List<String> splitWebResourceKeys(String webResourceKeys) {
        String keys = StringUtils.defaultString(webResourceKeys);
        return Arrays.stream(StringUtils.split(keys, ','))
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
