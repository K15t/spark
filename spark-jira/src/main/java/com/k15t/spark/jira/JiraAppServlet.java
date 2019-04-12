package com.k15t.spark.jira;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.k15t.spark.atlassian.AtlassianAppServlet;
import org.springframework.context.ApplicationContext;
import org.apache.commons.lang.BooleanUtils;


public class JiraAppServlet extends AtlassianAppServlet {


    protected JiraAppServlet(LoginUriProvider loginUriProvider, UserManager userManager, TemplateRenderer templateRenderer,
            LocaleResolver localeResolver, ApplicationProperties applicationProperties, ApplicationContext applicationContext) {

        super(loginUriProvider, userManager, templateRenderer, localeResolver, applicationProperties, applicationContext);
    }


    @Override
    protected boolean isDevMode() {
        // TODO check, if there are JIRA specific system properties and use them
        return BooleanUtils.toBoolean(System.getProperty("atlassian.dev.mode"));
    }

}
