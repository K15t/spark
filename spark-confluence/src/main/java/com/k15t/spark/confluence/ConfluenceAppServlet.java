package com.k15t.spark.confluence;

import com.atlassian.confluence.core.ConfluenceSystemProperties;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.k15t.spark.atlassian.AtlassianAppServlet;
import org.springframework.context.ApplicationContext;


public class ConfluenceAppServlet extends AtlassianAppServlet {

    /**
     * Despite mentioned in https://confluence.atlassian.com/display/DOC/Recognised+System+Properties
     * "confluence.dev.mode" is not included in {@link ConfluenceSystemProperties#isDevMode()}.
     */
    public final static String QUIRKY_DEV_MODE = "confluence.dev.mode";

    protected ConfluenceAppServlet(LoginUriProvider loginUriProvider,
            UserManager userManager, TemplateRenderer templateRenderer,
            LocaleResolver localeResolver, ApplicationProperties applicationProperties,
            ApplicationContext applicationContext) {

        super(loginUriProvider, userManager, templateRenderer, localeResolver, applicationProperties, applicationContext);
    }


    @Override
    protected boolean isDevMode() {
        return ConfluenceSystemProperties.isDevMode() ||
                Boolean.getBoolean(QUIRKY_DEV_MODE);
    }

}
