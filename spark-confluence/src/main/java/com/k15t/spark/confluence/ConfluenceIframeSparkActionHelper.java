package com.k15t.spark.confluence;

import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.k15t.spark.base.Keys;
import com.k15t.spark.base.util.DocumentOutputUtil;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.config.entities.ActionConfig;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class ConfluenceIframeSparkActionHelper {

    /**
     * Renders the main html content with the iframe to use for wrapping the SPA and required
     * extra JS-code
     *
     * @param instance {@link ConfluenceIframeSpaceAppAction}
     * @param request {@link HttpServletRequest} current request
     * @param baseIframeId string to use as the id of the iframe (will get an extra suffix)
     * @return html code containing a SPARK iframe
     */
    public static String renderSparkIframeBody(ConfluenceSparkIframeAction instance,
            HttpServletRequest request, String baseIframeId) {
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


    /**
     * Returns the base url of the single page application relative to the Confluence context path. Can be overwritten by subclasses.
     * This default implementation evaluates the action parameter with name {@link Keys#SPARK_SELECTED_WEB_ITEM_KEY}.
     *
     * @return the base url of the spa app
     */
    protected static String defaultGetSpaBaseUrl(ActionContext actionContext) {
        ActionConfig actionConfig = actionContext.getActionInvocation().getProxy().getConfig();
        Object o = actionConfig.getParams().get(Keys.SPARK_SPA_BASE_URL);
        String baseUrl = (o instanceof String) ? (String) o : null;
        if (StringUtils.isNotBlank(baseUrl)) {
            return baseUrl;
        } else {
            throw new IllegalStateException("Missing action parameter '" + Keys.SPARK_SPA_BASE_URL + "'. Either add the parameter to "
                    + "the action definition in atlassian-plugin.xml or overwrite 'getSpaBaseUrl()' in your action implementation.");
        }
    }


    /**
     * Returns the complete module key of the space tools web-item to be marked as selected. Can be overwritten by subclasses.
     * This default implementation first checks for a request parameter and then for an action parameter with name
     * {@link Keys#SPARK_SELECTED_WEB_ITEM_KEY}. If none are present {@code null} is returned.
     */
    public static String defaultGetSelectedWebItem(HttpServletRequest request, ActionContext actionContext) {
        String selectedWebItemKey = request.getParameter(Keys.SPARK_SELECTED_WEB_ITEM_KEY);
        if (selectedWebItemKey == null) {
            ActionConfig actionConfig = actionContext.getActionInvocation().getProxy().getConfig();
            Object o = actionConfig.getParams().get(Keys.SPARK_SELECTED_WEB_ITEM_KEY);
            selectedWebItemKey = (o instanceof String) ? (String) o : null;
        }
        return selectedWebItemKey;
    }


    /**
     * <p>
     * Parses a list of 'complete module key' definitions from the value of the {@link Keys#SPARK_REQUIRED_WEB_RESOURCE_KEYS} in
     * the action configuration.
     * </p>
     * <p>
     * Multiple 'complete module keys' can be specified in the action configuration parameter value separated by commas.
     * </p>
     *
     * @param actionContext {@link ActionContext} from which to fetch the action configuration
     * @return a list of strings to use for requireResource keys parsed from the action config
     */
    public static List<String> defaultGetRequiredResourceKeys(ActionContext actionContext) {
        List<String> res = new ArrayList<>();

        ActionConfig actionConfig = actionContext.getActionInvocation().getProxy().getConfig();
        Object o = actionConfig.getParams().get(Keys.SPARK_REQUIRED_WEB_RESOURCE_KEYS);

        if (o instanceof String) {
            String webResSpec = (String) o;
            String[] parts = webResSpec.split(",");
            Collections.addAll(res, parts);
        }

        return res;
    }

}
