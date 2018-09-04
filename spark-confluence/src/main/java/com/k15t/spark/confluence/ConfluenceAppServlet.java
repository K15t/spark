package com.k15t.spark.confluence;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.k15t.spark.atlassian.AtlassianAppServlet;
import org.springframework.context.ApplicationContext;


/**
 * @deprecated Subclasses should directly inherit from {@link AtlassianAppServlet}.
 */
@Deprecated
public class ConfluenceAppServlet extends AtlassianAppServlet {

    protected ConfluenceAppServlet(LoginUriProvider loginUriProvider,
            UserManager userManager, TemplateRenderer templateRenderer,
            LocaleResolver localeResolver, ApplicationProperties applicationProperties,
            ApplicationContext applicationContext) {

        super(loginUriProvider, userManager, templateRenderer, localeResolver, applicationProperties, applicationContext);
    }

}
