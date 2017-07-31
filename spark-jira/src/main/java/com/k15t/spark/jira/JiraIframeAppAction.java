package com.k15t.spark.jira;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.k15t.spark.atlassian.AtlassianSparkIframeAction;
import com.k15t.spark.base.util.DocumentOutputUtil;
import org.springframework.beans.factory.annotation.Autowired;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public abstract class JiraIframeAppAction extends JiraWebActionSupport implements AtlassianSparkIframeAction {

    @Autowired private TemplateRenderer templateRenderer;
    private String body;


    /**
     * This method will be called by Confluence to output the iframe SPA wrapper
     * <p/>
     * Override to add permissions checks.
     */
    protected String doExecute() {
        this.body = renderSparkIframeBody(this, ServletActionContext.getRequest(), "spark_jira_app_iframe_");
        return INPUT;
    }


    @Override
    public String getIframeContextInfo() {
        return null;
    }


    @Override
    public String getSpaQueryString() {
        return getHttpRequest().getQueryString();
    }


    @Override
    public String getTitleAsHtml() {
        return "Spark Iframe";
    }


    @Override
    public String getBodyAsHtml() {
        return body;
    }


    @Override
    public List<String> getRequiredResourceKeys() {
        return null;
    }


    /**
     * Renders the main html content with the iframe to use for wrapping the SPA and required
     * extra JS-code
     *
     * @param instance
     *         {@link AtlassianSparkIframeAction}
     * @param request
     *         {@link HttpServletRequest} current request
     * @param baseIframeId
     *         string to use as the id of the iframe (will get an extra suffix)
     * @return html code containing a SPARK iframe
     */
    public String renderSparkIframeBody(AtlassianSparkIframeAction instance,
            HttpServletRequest request, String baseIframeId) {
        try {
            long idSuffix = System.currentTimeMillis();

            String template = DocumentOutputUtil.getIframeAdminContentWrapperTemplate();

            Map<String, Object> context =
                    DocumentOutputUtil.generateAdminIframeTemplateContext(
                            instance.getSpaBaseUrl(), baseIframeId + idSuffix,
                            instance.getIframeContextInfo(), instance.getSpaQueryString());

            return templateRenderer.renderFragment(template, context);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load iframe-space app template");
        }
    }
}
