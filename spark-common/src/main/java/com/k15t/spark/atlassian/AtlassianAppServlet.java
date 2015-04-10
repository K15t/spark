package com.k15t.spark.atlassian;

import com.atlassian.plugin.servlet.descriptors.BaseServletModuleDescriptor;
import com.atlassian.plugins.rest.common.util.ReflectionUtils;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.k15t.spark.base.AppServlet;
import com.k15t.spark.base.RequestProperties;
import com.k15t.spark.base.util.ContentBufferingServletResponseWrapper;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.osgi.context.BundleContextAware;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;


abstract public class AtlassianAppServlet extends AppServlet implements BundleContextAware {

    private ServiceTracker loginUriProviderTracker;
    private ServiceTracker userManagerTracker;
    private ServiceTracker templateRendererTracker;
    private ServiceTracker i18nResolverTracker;

    private String appPrefix;


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
        return new AtlassianRequestProperties(this, request, appPrefix);
    }


    @Override
    protected boolean sendOutput(RequestProperties props, HttpServletResponse response) throws IOException {
        if (isHtmlContentType(props.getContentType())) {
            ContentBufferingServletResponseWrapper responseWrapper = new ContentBufferingServletResponseWrapper(response);
            if (!super.sendOutput(props, responseWrapper)) {
                return false;
            }

            String documentStr = responseWrapper.toString();
            Document document = Jsoup.parse(documentStr, props.getUri().toString());

            if (document.select("meta[name=decorator]").size() != 0) {
                // The Confluence decorators ignore anything inside the <head> of a velocity template. Thus we
                // move it into the body.

                Elements scriptsAndStyles = document.head().children().not("title,meta[name=decorator],content");
                scriptsAndStyles.remove();
                document.body().prepend(scriptsAndStyles.outerHtml());
                documentStr = document.outerHtml();
            }

            PrintWriter out = response.getWriter();
            out.write(documentStr);
            out.close();
            return true;
        } else {
            return super.sendOutput(props, response);
        }
    }


    protected boolean isHtmlContentType(String contentType) {
        return StringUtils.substringBefore(contentType, ";").equals("text/html");
    }


    @Override
    protected void renderVelocity(RequestProperties props, HttpServletResponse response, InputStream template) throws IOException {
        template.close();

        TemplateRenderer renderer = getTemplateRenderer();
        renderer.render(getPluginResourcePath(props.getLocalPath()), getVelocityContext(props.getRequest()), response.getWriter());
    }


    /**
     * Override this method to provide parameters to the Velocity context that
     * is used to render the HTML file.
     *
     * @param request
     * @return
     */
    protected Map<String, Object> getVelocityContext(HttpServletRequest request) {
        return Collections.emptyMap();
    }


    @Override
    protected String getText(String key) {
        return getI18nResolver().getRawText(key);
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
        loginUriProviderTracker = new ServiceTracker(bundleContext, LoginUriProvider.class.getName(), null);
        loginUriProviderTracker.open();

        userManagerTracker = new ServiceTracker(bundleContext, UserManager.class.getName(), null);
        userManagerTracker.open();

        templateRendererTracker = new ServiceTracker(bundleContext, TemplateRenderer.class.getName(), null);
        templateRendererTracker.open();

        i18nResolverTracker = new ServiceTracker(bundleContext, I18nResolver.class.getName(), null);
        i18nResolverTracker.open();
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


    protected I18nResolver getI18nResolver() {
        Object proxy = i18nResolverTracker.getService();
        if ((proxy != null) && (proxy instanceof I18nResolver)) {
            return (I18nResolver) proxy;
        } else {
            throw new RuntimeException("Could not get a valid TemplateRenderer proxy.");
        }
    }


    @Override
    public void destroy() {
        super.destroy();

        loginUriProviderTracker.close();
        userManagerTracker.close();
        templateRendererTracker.close();
    }

}
