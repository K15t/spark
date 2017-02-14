package com.k15t.spark.atlassian;

import com.atlassian.plugin.servlet.descriptors.BaseServletModuleDescriptor;
import com.atlassian.plugins.rest.common.util.ReflectionUtils;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.k15t.spark.base.AppServlet;
import com.k15t.spark.base.RequestProperties;
import com.k15t.spark.base.util.DocumentOutputUtil;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.osgi.context.BundleContextAware;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;


abstract public class AtlassianAppServlet extends AppServlet implements BundleContextAware {

    private ServiceTracker loginUriProviderTracker;
    private ServiceTracker userManagerTracker;
    private ServiceTracker templateRendererTracker;
    private ServiceTracker localeResolverTracker;
    private ServiceTracker applicationProperties;

    private String appPrefix;
    private long pluginModifiedTimestamp;


    @Override
    public void init() throws ServletException {
        super.init();
        appPrefix = getAppPrefixFromServletConfig();
    }


    private String getAppPrefixFromServletConfig() {
        ServletConfig servletConfig = getServletConfig();
        String urlPattern = getUrlPattern(servletConfig);

        return StringUtils.removeEnd(urlPattern, "*");
    }


    /**
     * @return the first url pattern as configured in the servlet module in the atlassian-plugin.xml,
     * e.g. {@code <url-pattern>/hello-world*</url-pattern>}
     */
    private String getUrlPattern(ServletConfig servletConfig) {
        // This implementation is a hack, because we have to get to the ModuleDescriptor of the
        // servlet module and we use reflection for that.
        // (Alternatively, we could try to go through the pluginAccessor)

        Field field = getDescriptorField(servletConfig.getClass());
        if (field == null) {
            throw new RuntimeException("Could not detect app prefix from servlet module.");
        }

        BaseServletModuleDescriptor descriptor = (BaseServletModuleDescriptor) ReflectionUtils.getFieldValue(field, servletConfig);
        if ((descriptor.getPaths() == null) || (descriptor.getPaths().size() < 1)) {
            throw new RuntimeException("Could not detect app prefix from servlet module.");
        }

        return (String) descriptor.getPaths().get(0);
    }


    private Field getDescriptorField(Class<? extends ServletConfig> clazz) {
        for (Field field : ReflectionUtils.getDeclaredFields(clazz)) {
            if (field.getType().isAssignableFrom(BaseServletModuleDescriptor.class)) {
                return field;
            }
        }

        return null;
    }


    @Override
    protected RequestProperties getRequestProperties(HttpServletRequest request) {
        return new AtlassianRequestProperties(this, request, getApplicationProperties().getBaseUrl(
                UrlMode.RELATIVE_CANONICAL), appPrefix, getLocaleResolver().getLocale(request));
    }


    protected boolean isHtmlContentType(String contentType) {
        return StringUtils.substringBefore(contentType, ";").equals("text/html");
    }


    @Override
    protected String renderVelocity(String template, RequestProperties props) throws IOException {
        Map<String, Object> context = getVelocityContext(props.getRequest());
        String rendered = getTemplateRenderer().renderFragment(template, context);
        return rendered;
    }


    @Override
    protected String prepareIndexHtml(String indexHtml, RequestProperties props) throws IOException {

        Document document = Jsoup.parse(indexHtml, props.getUri().toString());

        if (!isDevMode()) {
            applyCacheKeysToResourceUrls(document, props);
        }

        if ( isAskingIframeContent(props)) {

            prepareIframeContentIndex(document);

        } else if (isIframeAdminApp(document)) {

            prepareAdminIframeWrapperIndex(document, props);

        } else if (isAdminApp(document)) {
            // The Confluence decorators ignore anything inside the <head> of a velocity template. Thus we
            // move it into the body.
            Elements scriptsAndStyles = document.head().children().not("title,meta,content").remove();
            document.body().prepend(scriptsAndStyles.outerHtml());

        } else if (isDialogApp(document) && props.isRequestedWithAjax()) {
            // * The Confluence decorators ignore anything inside the <head> of a velocity template. Thus we
            //   move it into the body
            // * For page apps, we need to wrap all body content with a div to load that into the modal dialog.

            Elements appWrapper = document.body().prepend("<div id=\"spark-dialog-app-wrapper\"/>").select("#spark-dialog-app-wrapper");

            Elements headContent = document.head().children().not("title,meta[name=decorator],content").remove();
            Elements allBodyContent = document.body().children().not("#spark-dialog-app-wrapper").remove();

            appWrapper.append(allBodyContent.outerHtml())
                    .append(headContent.outerHtml());

            fixScriptSrcs(appWrapper);
            fixLinkHrefs(appWrapper);
        }

        // don't let jsoup generate unwanted blanks. Otherwise decorator settings
        // like <content tag="selectedWebItem">...</content> don;t work.
        document.outputSettings().prettyPrint(false);
        indexHtml = document.outerHtml();
        return indexHtml;
    }


    protected void applyCacheKeysToResourceUrls(Document document, RequestProperties props) {
        Locale locale = getLocaleResolver().getLocale(props.getRequest());
        DocumentOutputUtil.applyCacheKeysToResourceUrls(document, pluginModifiedTimestamp, locale);
    }


    private boolean isAdminApp(Document document) {
        return (document.select("meta[name=decorator][content=atl.admin]").size() != 0);
    }


    private boolean isDialogApp(Document document) {
        return (document.select("meta[name=decorator][content=spark.dialog-app]").size() != 0);
    }


    /**
     * @param document current {@link Document}
     * @return true if the document contains meta-element marking it as 'iframe-admin-app'
     */
    private boolean isIframeAdminApp(Document document) {
        return (document.select("meta[name=decorator][content=spark.iframe-admin-app]").size() != 0);
    }


    /**
     * @param properties current {@link RequestProperties}
     * @return true if the page is meant to be shown in an iframe
     */
    private boolean isAskingIframeContent(RequestProperties properties) {
        String iframeContentValue =
                properties.getRequest().getParameter("iframe_content");

        return "true".equals(iframeContentValue);
    }


    /**
     * Removes Atlassian decorators from the document to be served inside the iframe
     *
     * All script etc. elements and the main content of the document are left untouched
     *
     * Removes all the "meta" elements from the documents head that are not marked to be kept also
     * for the iframe content by setting an attribute "spark" to value "iframe_keep" on the element.
     * Also removes "content" elements from the body.
     *
     * Correct iframe-resizing operation requires that the libs/spark path contains iframeResizer.min.js and
     * iframeResizer.contentWindow.min.js files (the required script elements will be added automatically)
     *
     * @param document {@link Document} of the index before processing
     */
    private void prepareIframeContentIndex(Document document) {

        // remove meta arguments not marked to be kept also in the iframe, and other decorators
        // also load contentWindow part of the iFrameResizer, otherwise left the app untouched

        document.head().children().select("meta").not("[spark=iframe]").remove();
        document.head().children().select("meta").removeAttr("spark");

        document.head().append("<script src='libs/spark/iframeResizer.contentWindow.min.js'></script>");

        document.body().children().select("content").remove();

    }


    /**
     * Parse the part of the index document that is to be used as the main level page containing
     * all needed Atlassian decorators and embedding rest of the content in a iframe
     *
     * Removes elements from the head that don't look like Atlassian decorators (eg. scripts and styles),
     * and also removes meta elements that are marked to be relevant to the iframe content by setting
     * "spark" attribute
     *
     * The body of the index-document will be substituted (except for "content" elements that might contain
     * instructions for the Atlassian decorators) with an iframe. The same URL of the request will be set
     * as the source of the iframe but with an extra query parameter telling that it is the content
     * (then the {@link #prepareIframeContentIndex(Document)} method will be used instead).
     *
     * Correct iframe-resizing operation requires that the libs/spark path contains iframeResizer.min.js and
     * iframeResizer.contentWindow.min.js files (the required script elements will be added automatically)
     *
     * @param document {@link Document} of the index before processing
     * @param props {@link RequestProperties} of the current request
     */
    private void prepareAdminIframeWrapperIndex(Document document, RequestProperties props) {

        // remove all the styles and wrappers from the iframe parent, they will only be needed in the actual content iframe
        document.head().children().not("title,meta,content").remove();
        // also remove meta attributes that the user has specified to be used when loaded into iframe
        document.head().children().select("meta").select("[spark=iframe]").remove();

        // fill the body with an iframe that will ask the actual app with 'iframe_content' query parameter
        // remove all other (non-decorator) content - it will be shown when the app is loaded with iframe_content parameter

        document.body().children().not("content").remove();

        String iframeHtml = DocumentOutputUtil.generateResizedIframeHtml(
                props.getRequest().getRequestURI(), "spark_admin_iframe",
                "admin", null);

        document.body().append(iframeHtml);

    }

    /**
     * Change relative references to load js from the app servlet.
     */
    private void fixScriptSrcs(Elements appWrapper) {
        Elements scriptElements = appWrapper.select("script[src$=.js]");

        for (Element scriptEl : scriptElements) {
            String url = UriBuilder.fromUri(scriptEl.baseUri()).path(scriptEl.attr("src")).build().toString();
            appWrapper.append("<meta name=\"script\" content=\"" + url + "\"/>");
        }

        scriptElements.remove();
    }


    /**
     * Change relative references to load CSS from the app servlet.
     */
    private void fixLinkHrefs(Elements appWrapper) throws IOException {
        Elements linkElements = appWrapper.select("link[href$=.css]");

        for (Element linkEl : linkElements) {
            String url = UriBuilder.fromUri(linkEl.baseUri()).path(linkEl.attr("href")).build().toString();
            linkEl.attr("href", url);
        }
    }


    /**
     * Override this method to provide parameters to the Velocity context that
     * is used to render the HTML file.
     */
    protected Map<String, Object> getVelocityContext(HttpServletRequest request) {
        return Collections.emptyMap();
    }


    @Override
    protected boolean verifyPermissions(RequestProperties props, HttpServletResponse response) throws IOException {
        if (hasPermissions(props)) {
            return true;
        }

        if (isAnonymous(props) && isHtmlContentType(props.getContentType())) {
            redirectToLogin(props, response);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return false;
    }


    protected boolean isAnonymous(RequestProperties props) {
        return getUserManager().getRemoteUsername(props.getRequest()) == null;
    }


    protected boolean hasPermissions(RequestProperties props) {
        String user = getUserManager().getRemoteUsername(props.getRequest());
        return (user != null && getUserManager().isSystemAdmin(user));
    }


    private void redirectToLogin(RequestProperties props, HttpServletResponse response) throws IOException {
        response.sendRedirect(getLoginUriProvider().getLoginUri(props.getUri()).toASCIIString());
    }


    @Override
    public void setBundleContext(BundleContext bundleContext) {
        pluginModifiedTimestamp = bundleContext.getBundle().getLastModified();

        loginUriProviderTracker = new ServiceTracker(bundleContext, LoginUriProvider.class.getName(), null);
        loginUriProviderTracker.open();

        userManagerTracker = new ServiceTracker(bundleContext, UserManager.class.getName(), null);
        userManagerTracker.open();

        templateRendererTracker = new ServiceTracker(bundleContext, TemplateRenderer.class.getName(), null);
        templateRendererTracker.open();

        localeResolverTracker = new ServiceTracker(bundleContext, LocaleResolver.class.getName(), null);
        localeResolverTracker.open();

        applicationProperties = new ServiceTracker(bundleContext, ApplicationProperties.class.getName(), null);
        applicationProperties.open();
    }


    protected LoginUriProvider getLoginUriProvider() {
        Object proxy = loginUriProviderTracker.getService();
        if ((proxy != null) && (proxy instanceof LoginUriProvider)) {
            return (LoginUriProvider) proxy;
        } else {
            throw new RuntimeException("Could not get a valid LoginUriProvider proxy.");
        }
    }


    protected UserManager getUserManager() {
        Object proxy = userManagerTracker.getService();
        if ((proxy != null) && (proxy instanceof UserManager)) {
            return (UserManager) proxy;
        } else {
            throw new RuntimeException("Could not get a valid UserManager proxy.");
        }
    }


    protected TemplateRenderer getTemplateRenderer() {
        Object proxy = templateRendererTracker.getService();
        if ((proxy != null) && (proxy instanceof TemplateRenderer)) {
            return (TemplateRenderer) proxy;
        } else {
            throw new RuntimeException("Could not get a valid TemplateRenderer proxy.");
        }
    }


    protected LocaleResolver getLocaleResolver() {
        Object proxy = localeResolverTracker.getService();
        if ((proxy != null) && (proxy instanceof LocaleResolver)) {
            return (LocaleResolver) proxy;
        } else {
            throw new RuntimeException("Could not get a valid LocaleResolver proxy.");
        }
    }


    protected ApplicationProperties getApplicationProperties() {
        Object proxy = applicationProperties.getService();
        if ((proxy != null) && (proxy instanceof ApplicationProperties)) {
            return (ApplicationProperties) proxy;
        } else {
            throw new RuntimeException("Could not get a valid ApplicationProperties proxy.");
        }
    }


    @Override
    public void destroy() {
        super.destroy();
        loginUriProviderTracker.close();
        userManagerTracker.close();
        templateRendererTracker.close();
        localeResolverTracker.close();
        applicationProperties.close();
    }

}
