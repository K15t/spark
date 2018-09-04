package com.k15t.spark.atlassian;

import com.atlassian.plugin.servlet.descriptors.BaseServletModuleDescriptor;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.base.Preconditions;
import com.k15t.spark.base.AppServlet;
import com.k15t.spark.base.RequestProperties;
import com.k15t.spark.base.util.DocumentOutputUtil;
import com.k15t.spark.base.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public abstract class AtlassianAppServlet extends AppServlet {

    private final LoginUriProvider loginUriProvider;
    private final UserManager userManager;
    private final TemplateRenderer templateRenderer;
    private final LocaleResolver localeResolver;
    private final ApplicationProperties applicationProperties;
    private final long pluginModifiedTimestamp;

    private String appPrefix;


    protected AtlassianAppServlet(LoginUriProvider loginUriProvider, UserManager userManager, TemplateRenderer templateRenderer,
            LocaleResolver localeResolver, ApplicationProperties applicationProperties, ApplicationContext applicationContext) {

        this.loginUriProvider = Preconditions.checkNotNull(loginUriProvider);
        this.userManager = Preconditions.checkNotNull(userManager);
        this.templateRenderer = Preconditions.checkNotNull(templateRenderer);
        this.localeResolver = Preconditions.checkNotNull(localeResolver);
        this.applicationProperties = Preconditions.checkNotNull(applicationProperties);

        this.pluginModifiedTimestamp = Preconditions.checkNotNull(applicationContext).getStartupDate();
    }


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

        BaseServletModuleDescriptor descriptor = (BaseServletModuleDescriptor) getFieldValue(field, servletConfig);
        if ((descriptor.getPaths() == null) || (descriptor.getPaths().size() < 1)) {
            throw new RuntimeException("Could not detect app prefix from servlet module.");
        }

        return (String) descriptor.getPaths().get(0);
    }


    private Field getDescriptorField(Class<? extends ServletConfig> clazz) {
        for (Field field : getDeclaredFields(clazz)) {
            if (field.getType().isAssignableFrom(BaseServletModuleDescriptor.class)) {
                return field;
            }
        }

        return null;
    }


    private static List<Field> getDeclaredFields(Class clazz) {
        if (clazz == null) {
            return new ArrayList<>();
        } else {
            final List<Field> superFields = getDeclaredFields(clazz.getSuperclass());
            superFields.addAll(0, Arrays.asList(clazz.getDeclaredFields()));
            return superFields;
        }
    }


    private static Object getFieldValue(Field field, Object object) {
        final boolean accessible = field.isAccessible();
        try {
            if (!accessible) {
                field.setAccessible(true);
            }
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not access '" + field + "' from '" + object + "'", e);
        } finally {
            if (!accessible) {
                field.setAccessible(false);
            }
        }
    }


    @Override
    protected RequestProperties getRequestProperties(HttpServletRequest request) {
        return new AtlassianRequestProperties(this, request, applicationProperties.getBaseUrl(UrlMode.RELATIVE_CANONICAL), appPrefix,
                localeResolver.getLocale(request));
    }


    protected boolean isHtmlContentType(String contentType) {
        return "text/html".equals(StringUtils.substringBefore(contentType, ";"));
    }


    @Override
    protected String renderVelocity(String template, RequestProperties props) throws IOException {
        Map<String, Object> context = getVelocityContext(props.getRequest());
        return templateRenderer.renderFragment(template, context);
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
        Locale locale = localeResolver.getLocale(props.getRequest());
        DocumentOutputUtil.applyCacheKeysToResourceUrls(document, pluginModifiedTimestamp, locale);
    }


    private boolean isAdminApp(Document document) {
        return (document.select("meta[name=decorator][content=atl.admin]").size() != 0);
    }


    private boolean isDialogApp(Document document) {
        return (document.select("meta[name=decorator][content=spark.dialog-app]").size() != 0);
    }


    /**
     * Change relative references to load js from the app servlet.
     */
    private void fixScriptSrcs(Elements appWrapper) {
        Elements scriptElements = appWrapper.select("script[src$=.js]");

        for (Element scriptEl : scriptElements) {
            String baseUrl = scriptEl.baseUri();
            String resourceUrl = scriptEl.attr("src");
            String url = UrlUtil.rebaseUrl(baseUrl, resourceUrl);
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
            String baseUrl = linkEl.baseUri();
            String resourceUrl = linkEl.attr("href");
            String url = UrlUtil.rebaseUrl(baseUrl, resourceUrl);
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
        return userManager.getRemoteUser(props.getRequest()) == null;
    }


    protected boolean hasPermissions(RequestProperties props) {
        UserProfile user = userManager.getRemoteUser(props.getRequest());
        return (user != null && userManager.isSystemAdmin(user.getUserKey()));
    }


    private void redirectToLogin(RequestProperties props, HttpServletResponse response) throws IOException {
        response.sendRedirect(loginUriProvider.getLoginUri(props.getUri()).toASCIIString());
    }

}
