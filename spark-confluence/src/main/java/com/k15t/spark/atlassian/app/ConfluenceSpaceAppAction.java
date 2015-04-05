package com.k15t.spark.atlassian.app;

import com.atlassian.confluence.core.ConfluenceSystemProperties;
import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.confluence.spaces.actions.SpaceAware;
import com.k15t.spark.base.AppServlet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * The AbstractAppAction must be extended when a frontend app needs to be displayed in the space admin section.
 */
public abstract class ConfluenceSpaceAppAction extends AbstractSpaceAction implements SpaceAware {

    private final static Logger logger = LoggerFactory.getLogger(ConfluenceSpaceAppAction.class);


    @Override
    public boolean isSpaceRequired() {
        return true;
    }


    @Override
    public boolean isViewPermissionRequired() {
        return true;
    }


    public String getTitleAsHtml() {
        return getTitle();
    }


    /**
     * @return the title to be displayed as h1 in the space admin section.
     */
    public abstract String getTitle();


    /**
     * @return index.html of this frontend app to be included in the space admin section.
     */
    public String getIndexHtmlAsHtml() {
        try {
            InputStream indexHtml = loadIndexHtml();
            if (indexHtml == null) {
                logger.error("No template found for path '" + getIndexHtmlPath() + "'.");
                return "<p>No template found for path <em>" + getIndexHtmlPath() + "</em>. Please check logs.</p>";
            }

            Document document = Jsoup.parse(indexHtml, "UTF-8", "");

            fixRelativeReferences(document);
            moveRequiredHeaderContentToBody(document);

            return document.body().html();

        } catch (IOException e) {
            logger.error("Error parsing HTML of template '" + getIndexHtmlPath() + "'.", e);
            return "<p>Error parsing HTML of template '" + getIndexHtmlPath() + "'</p>"
                    + "<pre>" + ExceptionUtils.getStackTrace(e) + "</pre>";
        }
    }


    private InputStream loadIndexHtml() throws IOException {
        if (ConfluenceSystemProperties.isDevMode()) {
            InputStream dev = loadFromDevelopmentDirectory(getIndexHtmlPath());
            if (dev != null)
                return dev;
        }

        return getClass().getClassLoader().getResourceAsStream(getIndexHtmlPath());
    }


    protected InputStream loadFromDevelopmentDirectory(String localPath) throws IOException {
        InputStream fileIn = null;
        String resourceDirectoryPaths = System.getProperty(AppServlet.Keys.PLUGIN_RESOURCE_DIRECTORIES);

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


    /**
     * @return the path to index.html.
     */
    public abstract String getIndexHtmlPath();


    /**
     * Change relative references to load JS and CSS from the frontend app.
     */
    private void fixRelativeReferences(Document document) {
        Elements elements = document.head().select("link[href$=.css],script[src$=.js]");
        for (Element element : elements) {
            if (element.tagName().equals("script") && isRelativeReference(element.attr("src"))) {
                element.attr("src", getAppBaseUrl() + element.attr("src"));
            } else if (element.tagName().equals("link") && isRelativeReference(element.attr("href"))) {
                element.attr("href", getAppBaseUrl() + element.attr("href"));
            }
        }
    }


    /**
     * @return the (global) base URL of the app.
     */
    protected abstract String getAppBaseUrl();


    private boolean isRelativeReference(String reference) {
        return StringUtils.isNotBlank(reference) &&
                !(reference.startsWith("http://") || reference.startsWith("https://") || reference.startsWith("/"));
    }


    private void moveRequiredHeaderContentToBody(Document document) {
        Elements scriptsAndStyles = document.head().children().not("title,meta[name=decorator],content");
        scriptsAndStyles.remove();
        document.body().prepend(scriptsAndStyles.outerHtml());
    }

}
