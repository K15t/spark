package com.k15t.spark.jira;

import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.k15t.spark.base.Keys;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;


/**
 * The AbstractAppAction must be extended when a SPARK app needs to be displayed in the admin section.
 */
public class JiraAppAction extends JiraWebActionSupport {
    private final static Logger logger = LoggerFactory.getLogger(JiraAppAction.class);

    /**
     * The base URL of the AdminApp Servlet (used to load SPA resources)
     */
    public static final String SPA_BASE_URL = "spark.app-base-url";

    protected String resourcePath;
    private String title;
    private String body;


    @Override
    protected String doExecute() throws Exception {
        initFromIndexHtml();

        return super.doExecute();
    }


    private void initFromIndexHtml() {
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
        if (JiraSystemProperties.isDevMode()) {
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

        return super.insertContextPath("/" + StringUtils.removeStart(baseElement.attr("content"), "/"));
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


    public String getTitleAsHtml() {
        return title;
    }


    /**
     * @return index.html of this SPARK app to be included in the space tools section.
     */
    public String getBodyAsHtml() {
        return body;
    }

}
