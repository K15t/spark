package com.k15t.spark.confluence;

import com.atlassian.confluence.core.ConfluenceSystemProperties;
import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.confluence.spaces.actions.SpaceAware;
import com.atlassian.sal.api.message.LocaleResolver;
import com.k15t.spark.base.Keys;
import com.k15t.spark.base.util.DocumentOutputUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Locale;


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
    private ApplicationContext applicationContext;

    private String title;
    private String body;

    // The setters for these are invoked by xwork's / struts' StaticParametersInterceptor as long as it is referenced in the action config:
    //			<action name="test" class="some.test.TestAction" method="execute">
    //				<interceptor-ref name="static-params"/>
    //				<param name="SparkResourcePath">...</param>
    //				...
    //			</action>
    private String sparkResourcePath;
    private String sparkSelectedWebItemKey;


    public void setSparkResourcePath(String sparkResourcePath) {
        this.sparkResourcePath = sparkResourcePath;
    }


    public void setSparkSelectedWebItemKey(String sparkSelectedWebItemKey) {
        this.sparkSelectedWebItemKey = sparkSelectedWebItemKey;
    }


    /**
     * This method will be called by Confluence to output the SPA.
     * <p/>
     * Override to add permissions checks.
     */
    @SuppressWarnings("unused") // references by add-ons xwork definition for Space Apps.
    public String index() {
        String indexHtmlPath = sparkResourcePath + "/index.html";

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
                    applyCacheKeysToResourceUrls(document);
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

        return "input";
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
                    fileIn = Files.newInputStream(resource.toPath());
                    break;
                }
            }
        }

        return fileIn;
    }


    protected void applyCacheKeysToResourceUrls(Document document) {
        // If the applicationContext is not injected, we cannot cache resources - bad luck.
        if (applicationContext != null) {
            HttpServletRequest request = getCurrentRequest();
            long pluginModifiedTimestamp = applicationContext.getStartupDate();
            Locale locale = localeResolver.getLocale(request);
            DocumentOutputUtil.applyCacheKeysToResourceUrls(document, pluginModifiedTimestamp, locale);
        }
    }


    private String prepareBody(Document document) throws IOException {
        fixRelativeReferences(document);
        moveRequiredHeaderContentToBody(document);
        return document.body().html();
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

        return getCurrentRequest().getContextPath() + "/" + StringUtils.removeStart(baseElement.attr("content"), "/");
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
        return sparkSelectedWebItemKey;
    }


    public LocaleResolver getLocaleResolver() {
        return localeResolver;
    }


    public void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }


    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
