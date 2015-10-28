package com.k15t.spark.atlassian;

import com.atlassian.plugin.servlet.descriptors.BaseServletModuleDescriptor;
import com.atlassian.plugins.rest.common.util.ReflectionUtils;
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
        return new AtlassianRequestProperties(this, request, appPrefix, getLocaleResolver().getLocale(request));
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

        if (isAdminApp(document)) {
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
     * Change relative references to load CSS from the app servlet.
     */
    private void fixScriptSrcs(Elements appWrapper) {
        Elements scriptElements = appWrapper.select("script[src$=.js]");

        for (Element scriptEl : scriptElements) {
            // TODO it would be better for HTTP to create paths starting with '/'
            appWrapper.append("<meta name=\"script\" content=\"" + scriptEl.absUrl("src") + "\"/>");
        }

        scriptElements.remove();
    }


    /**
     * Change relative references to load CSS from the app servlet.
     */
    private void fixLinkHrefs(Elements appWrapper) throws IOException {
        Elements linkElements = appWrapper.select("link[href$=.css]");

        for (Element linkEl : linkElements) {
            // TODO it would be better for HTTP to create paths starting with '/'
            linkEl.attr("href", linkEl.absUrl("href"));
        }
    }


    /**
     * Override this method to provide parameters to the Velocity context that
     * is used to render the HTML file.
     *
     * @param request
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


    private void redirectToLogin(RequestProperties props,
            HttpServletResponse response) throws IOException {
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


    @Override
    public void destroy() {
        super.destroy();

        loginUriProviderTracker.close();
        userManagerTracker.close();
        templateRendererTracker.close();
        localeResolverTracker.close();
    }

}
