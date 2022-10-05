package com.k15t.spark.atlassian;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.k15t.spark.base.AppServlet;
import com.k15t.spark.base.RequestProperties;
import com.k15t.spark.base.util.DocumentOutputUtil;
import com.k15t.spark.base.util.UrlUtil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public abstract class AtlassianAppServlet extends AppServlet {

    private final LoginUriProvider loginUriProvider;
    private final UserManager userManager;
    private final TemplateRenderer templateRenderer;
    private final LocaleResolver localeResolver;
    private final ApplicationProperties applicationProperties;
    private final long pluginModifiedTimestamp;


    protected AtlassianAppServlet(LoginUriProvider loginUriProvider, UserManager userManager, TemplateRenderer templateRenderer,
            LocaleResolver localeResolver, ApplicationProperties applicationProperties, ApplicationContext applicationContext) {

        this.loginUriProvider = Objects.requireNonNull(loginUriProvider);
        this.userManager = Objects.requireNonNull(userManager);
        this.templateRenderer = Objects.requireNonNull(templateRenderer);
        this.localeResolver = Objects.requireNonNull(localeResolver);
        this.applicationProperties = Objects.requireNonNull(applicationProperties);

        this.pluginModifiedTimestamp = Objects.requireNonNull(applicationContext).getStartupDate();
    }


    @Override
    protected RequestProperties getRequestProperties(HttpServletRequest request) {
        return new AtlassianRequestProperties(this, request, applicationProperties.getBaseUrl(UrlMode.RELATIVE_CANONICAL),
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
