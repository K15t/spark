package com.k15t.spark.confluence;

import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.k15t.spark.base.util.DocumentOutputUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class ConfluenceIframeSparkActionHelper {

    /**
     * Renders the main html content with the iframe to use for wrapping the SPA and required
     * extra JS-code
     *
     * @param instance {@link ConfluenceIframeSpaceAppAction}
     * @param baseIframeId string to use as the id of the iframe (will get an extra suffix)
     * @return html code containing a SPARK iframe
     */
    public static String renderSparkIframeBody(ConfluenceSparkIframeAction instance, String baseIframeId) {
        try {
            long idSuffix = System.currentTimeMillis();

            String template = DocumentOutputUtil.getIframeAdminContentWrapperTemplate();

            Map<String, Object> context =
                    DocumentOutputUtil.generateAdminIframeTemplateContext(
                            instance.getSpaBaseUrl(), baseIframeId + idSuffix,
                            instance.getIframeContextInfo(), instance.getSpaQueryString());

            return VelocityUtils.getRenderedContent((CharSequence) template, context);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load iframe-space app template");
        }
    }


    static List<String> splitWebResourceKeys(String webResourceKeys) {
        String keys = StringUtils.defaultString(webResourceKeys);
        return Arrays.stream(StringUtils.split(keys, ','))
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
