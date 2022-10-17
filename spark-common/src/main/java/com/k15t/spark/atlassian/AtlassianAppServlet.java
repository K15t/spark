package com.k15t.spark.atlassian;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.k15t.spark.base.AppServlet;
import com.k15t.spark.base.RequestProperties;
import com.k15t.spark.base.util.DocumentOutputUtil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;


public abstract class AtlassianAppServlet extends AppServlet {

    private final LoginUriProvider loginUriProvider;
    private final UserManager userManager;
    private final LocaleResolver localeResolver;
    private final ApplicationProperties applicationProperties;
    private final long pluginModifiedTimestamp;


    protected AtlassianAppServlet(LoginUriProvider loginUriProvider, UserManager userManager, LocaleResolver localeResolver,
            ApplicationProperties applicationProperties, ApplicationContext applicationContext) {

        this.loginUriProvider = Objects.requireNonNull(loginUriProvider);
        this.userManager = Objects.requireNonNull(userManager);
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
    protected String customizeHtml(String indexHtml, RequestProperties props) throws IOException {
        if (!isDevMode()) {
            Document document = Jsoup.parse(indexHtml, props.getUri().toString());
            applyCacheKeysToResourceUrls(document, props);
            document.outputSettings().prettyPrint(false);
            indexHtml = document.outerHtml();
        }

        return indexHtml;
    }


    protected void applyCacheKeysToResourceUrls(Document document, RequestProperties props) {
        Locale locale = localeResolver.getLocale(props.getRequest());
        DocumentOutputUtil.applyCacheKeysToResourceUrls(document, pluginModifiedTimestamp, locale);
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
