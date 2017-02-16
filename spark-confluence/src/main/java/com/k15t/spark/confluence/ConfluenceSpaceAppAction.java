package com.k15t.spark.confluence;

import com.atlassian.confluence.core.ConfluenceSystemProperties;
import com.atlassian.confluence.plugin.descriptor.PluginAwareActionConfig;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.confluence.spaces.actions.SpaceAware;
import com.atlassian.sal.api.message.LocaleResolver;
import com.k15t.spark.base.Keys;
import com.k15t.spark.base.util.DocumentOutputUtil;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.config.entities.ActionConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;


/**
 * The AbstractAppAction must be extended when a SPARK app needs to be displayed in the space admin section.
 */
public class ConfluenceSpaceAppAction extends AbstractSpaceAction implements SpaceAware {

    private final static Logger logger = LoggerFactory.getLogger(ConfluenceSpaceAppAction.class);

    /**
     * The base URL of the AdminApp Servlet (used to load SPA resources)
     */
    public static final String SPA_BASE_URL = "spark.app-base-url";

    private LocaleResolver localeResolver;

    private String resourcePath;
    private String title;
    private String body;
    private String selectedSpaceToolsWebItem;


    /**
     * This method will be called by Confluence to output the SPA.
     * <p/>
     * Override to add permissions checks.
     */
    @SuppressWarnings("unused") // references by add-ons xwork definition for Space Apps.
    public String index() {
        ActionConfig actionConfig = ServletActionContext.getContext().getActionInvocation().getProxy().getConfig();
        initFromActionConfig(actionConfig);
        initFromIndexHtml(actionConfig);
        return INPUT;
    }


    private void initFromActionConfig(ActionConfig actionConfig) {
        this.resourcePath = getResourcePathAsObject(actionConfig);
        this.selectedSpaceToolsWebItem = getSelectedSpaceToolsWebItem(actionConfig);
    }


    private String getResourcePathAsObject(ActionConfig actionConfig) {
        Object resourcePathAsObject = actionConfig.getParams().get("resource-path");
        Validate.notNull(resourcePathAsObject, "No 'resource-path' param found for package 'actionConfig.getPackageName()'.");
        return StringUtils.removeEnd((String) resourcePathAsObject, "/");
    }


    private String getSelectedSpaceToolsWebItem(ActionConfig actionConfig) {
        Object selectedSpaceToolsWebItemAsObject = actionConfig.getParams().get("selectedSpaceToolsWebItem");

        if (selectedSpaceToolsWebItemAsObject instanceof String) {
            return (String) selectedSpaceToolsWebItemAsObject;
        } else {
            logger.warn("No 'selectedSpaceToolsWebItem' param for " + actionConfig.getClassName());
            return "";
        }
    }


    private void initFromIndexHtml(ActionConfig actionConfig) {
        String indexHtmlPath = resourcePath + "/index.html";

        try {
            InputStream indexHtml = loadIndexHtml(indexHtmlPath);
            if (indexHtml == null) {
                logger.error("No template found for path '" + indexHtmlPath + "'.");
                this.title = "Error loading index.html";
                this.body = "<p>No template found for path <em>" + indexHtmlPath + "</em>. Please check logs.</p>";

            } else {
                Document document = Jsoup.parse(indexHtml, "UTF-8", "");

                this.title = document.title();

                if (!isDevMode()) {
                    applyCacheKeysToResourceUrls(document, actionConfig);
                }

                this.body = prepareBody(document);
            }

        } catch (IOException e) {
            logger.error("Error parsing HTML of template '" + indexHtmlPath + "'.", e);
            this.title = "Error loading index.html";
            this.body = "<p>Error parsing HTML of template '" + indexHtmlPath + "':<br/>"
                    + e.getMessage()
                    + "</p><pre>" + ExceptionUtils.getStackTrace(e) + "</pre>";
        }
    }


    private InputStream loadIndexHtml(String indexHtmlPath) throws IOException {
        if (ConfluenceSystemProperties.isDevMode()) {
            InputStream dev = loadFromDevelopmentDirectory(indexHtmlPath);
            if (dev != null) {
                return dev;
            }
        }

        return getClass().getClassLoader().getResourceAsStream(indexHtmlPath);
    }


    protected InputStream loadFromDevelopmentDirectory(String localPath) throws IOException {
        InputStream fileIn = null;
        String resourceDirectoryPaths = System.getProperty(Keys.SPARK_DEV_RESOURCE_DIRECTORIES);

        if (resourceDirectoryPaths == null) {
            return null;
        }

        for (String resourceDirectoryPath : StringUtils.split(resourceDirectoryPaths, ",")) {
            resourceDirectoryPath = resourceDirectoryPath.trim();
            resourceDirectoryPath = StringUtils.removeEnd(resourceDirectoryPath, "/") + "/";
            File resourceDirectory = new File(resourceDirectoryPath);

            if (resourceDirectory.isDirectory()) {
                File resource = new File(resourceDirectoryPath + localPath);
                if (resource.canRead()) {
                    fileIn = FileUtils.openInputStream(resource);
                    break;
                }
            }
        }

        return fileIn;
    }


    protected void applyCacheKeysToResourceUrls(Document document, ActionConfig actionConfig) {
        // If the actionConfig doesn't know the plugin, we cannot cache resources - bad luck.
        if (actionConfig instanceof PluginAwareActionConfig) {
            HttpServletRequest request = ServletActionContext.getRequest();
            long pluginModifiedTimestamp = ((PluginAwareActionConfig) actionConfig).getPlugin().getDateLoaded().getTime();
            Locale locale = localeResolver.getLocale(request);
            DocumentOutputUtil.applyCacheKeysToResourceUrls(document, pluginModifiedTimestamp, locale);
        }
    }


    private String prepareBody(Document document) throws IOException {

        if ( isIframeSpaceApp(document) ) {

            String appBaseUrl = getAppBaseUrl(document);

            String template = DocumentOutputUtil.getIframeAdminContentWrapperTemplate();
            Map<String, Object> context = DocumentOutputUtil.generateAdminIframeTemplateContext(
                    appBaseUrl, "spark_space_adm_iframe",
                    getIframeContextInfo(), getIframeContextInitializedCallbackName(), getSpaQueryString());

            return VelocityUtils.getRenderedContent(template, context);

        } else {

            fixRelativeReferences(document);
            moveRequiredHeaderContentToBody(document);
            return document.body().html();
        }
    }


    /**
     * Change relative references to load JS and CSS from the SPARK app.
     */
    private void fixRelativeReferences(Document document) throws IOException {
        Elements elements = document.select("link[href$=.css],script[src$=.js]");
        String appBaseUrl = getAppBaseUrl(document);

        for (Element element : elements) {
            if (element.tagName().equals("script") && isRelativeReference(element.attr("src"))) {
                element.attr("src", appBaseUrl + element.attr("src"));
            } else if (element.tagName().equals("link") && isRelativeReference(element.attr("href"))) {
                element.attr("href", appBaseUrl + element.attr("href"));
            }
        }
    }


    /**
     * @return the (global) base URL of the app.
     */
    protected String getAppBaseUrl(Document document) throws IOException {
        Elements baseElement = document.select("meta[name=" + SPA_BASE_URL + "]");

        if (baseElement == null) {
            throw new IOException("Could not find meta element for SPA resources, e.g. "
                    + "<meta name=\"spark.app-base-url\" content=\"/plugins/servlet/hello-world/\">.");
        }

        return ServletActionContext.getRequest().getContextPath() + "/" +
                StringUtils.removeStart(baseElement.attr("content"), "/");
    }


    /**
     * @param document loaded {@link Document}
     * @return true if the app is marked to be an 'iframe-space-app' (ie. main content should be loaded in an iframe)
     */
    private boolean isIframeSpaceApp(Document document) {
        return (document.select("meta[name=decorator][content=spark.iframe-space-app]").size() != 0);
    }


    /**
     * The result of this method will be injected into the context of the loaded iframe as SPARK.iframeContext
     *
     * The JS variable will be a string. To pass structured information eg. JSON can be used.
     *
     * This is the main method of injecting context specific information to a space app iframe. The passed
     * information can be customized to fit the needed use case by sub-classing this class with an
     * action class that will override this method (and by using that class in the correct atlassian module
     * specification).
     *
     * @return string that will be attached to SPARK.iframeContext variable as a JS string
     */
    protected String getIframeContextInfo() {

        Space space = getSpace();

        String res = "{\"space_key\": \"" + space.getKey() + "\"}";

        return res;
    }


    /**
     * It is possible to specify a callback function name that will be called once the context information
     * is injected into the iframe.
     *
     * A function with the given name must be present in the SPARK object in the iframe's global context. If the
     * function with given name (or SPARK) object is not present, nothing is called. This can happen also in
     * normal operation because of initialization race conditions.
     *
     * Correct way to use the init-callback method is to first check in the iframe's normal init-method to check
     * whether the SPARK.iframeContext already is present, and if not, then add the SPARK object (if needed) and
     * the init method with correct name to it.
     *
     * If no initialization method is needed, null can be returned.
     *
     * @return name of an initialization callback (on SPARK global object in iframe's context), or null if not needed
     */
    protected String getIframeContextInitializedCallbackName() {
        return null;
    }


    /**
     * The query parameter to add as the current parameter to the url of the SPA app when it is loaded into
     * the iframe context ("iframe_content=true" will always be added and used by the SPARK framework)
     *
     * Default implementation returns the query string used when running the action
     *
     * @return query string to use when loading the SPA in the iframe
     */
    protected String getSpaQueryString() {
        return ServletActionContext.getRequest().getQueryString();
    }

    private boolean isRelativeReference(String reference) {
        return StringUtils.isNotBlank(reference) &&
                !(reference.startsWith("http://") || reference.startsWith("https://") || reference.startsWith("/"));
    }


    private void moveRequiredHeaderContentToBody(Document document) {
        Elements scriptsAndStyles = document.head().children().not("title,base,meta,content");
        scriptsAndStyles.remove();
        document.body().prepend(scriptsAndStyles.outerHtml());
    }


    @Override
    public boolean isSpaceRequired() {
        return true;
    }


    @Override
    public boolean isViewPermissionRequired() {
        return true;
    }


    public String getTitleAsHtml() {
        return title;
    }


    /**
     * @return index.html of this SPARK app to be included in the space tools section.
     */
    public String getBodyAsHtml() {
        return body;
    }


    public String getSelectedSpaceToolsWebItem() {
        return selectedSpaceToolsWebItem;
    }


    public LocaleResolver getLocaleResolver() {
        return localeResolver;
    }


    public void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

}
