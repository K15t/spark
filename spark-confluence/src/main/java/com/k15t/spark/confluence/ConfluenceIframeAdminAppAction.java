package com.k15t.spark.confluence;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.setup.BuildInformation;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.user.User;
import com.k15t.spark.base.util.DocumentOutputUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


public abstract class ConfluenceIframeAdminAppAction extends ConfluenceActionSupport implements ConfluenceSparkIframeAction {

    // The setters for these are invoked by xwork's / struts' StaticParametersInterceptor as long as it is referenced in the action config:
    //			<action name="test" class="some.test.TestAction" method="execute">
    //				<interceptor-ref name="staticParams"/>
    //				<param name="SparkSpaBaseUrl">...</param>
    //				...
    //			</action>
    private String sparkSpaBaseUrl;
    private String sparkSelectedWebItemKey;
    private String sparkRequiredWebResourceKeys;

    private String body;


    private static final Method isConfluenceAdministrator;

    static {
        if (Integer.parseInt(BuildInformation.INSTANCE.getMarketplaceBuildNumber()) < 20452 /* Confluence 8.8 */) {
            //noinspection RedundantSuppression
            try {
                //noinspection JavaReflectionMemberAccess
                isConfluenceAdministrator = com.atlassian.confluence.security.PermissionManager.class.getMethod("isConfluenceAdministrator",
                        User.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } else {
            //noinspection RedundantSuppression
            try {
                //noinspection JavaReflectionMemberAccess
                isConfluenceAdministrator = com.atlassian.confluence.security.PermissionManager.class.getMethod("isConfluenceAdministrator",
                        ConfluenceUser.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public boolean isPermitted() {
        try {
            // After dropping support for Confluence 8.8, replace this by just calling PermissionManager.isConfluenceAdministrator directly.
            return (boolean) isConfluenceAdministrator.invoke(permissionManager, getAuthenticatedUser());
        } catch (RuntimeException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Could not invoke PermissionManager.isConfluenceAdministrator.", e);
        }
    }


    /**
     * This method will be called by Confluence to output the iframe SPA wrapper
     * <p/>
     * Override to add permissions checks.
     */
    public String index() {
        if (sparkSpaBaseUrl == null) {
            throw new IllegalStateException("\n\n"
                    + "=============================================== Spark setup error ================================================\n"
                    + "The configuration for action '" + this.getClass().getName() + "' in atlassian-plugin.xml is incorrect:\n"
                    + "The action parameter 'SparkSpaBaseUrl' is not present or not injected via the 'staticParams' interceptor.\n"
                    + "Make sure to add both the parameter and the interceptor by adding this to the action definition:\n\n"
                    + "<interceptor-ref name=\"staticParams\"/>\n"
                    + "<param name=\"SparkSpaBaseUrl\">...</param>\n"
                    + "==================================================================================================================\n"
            );
        }
        this.body = DocumentOutputUtil.renderSparkIframeBody(getSpaBaseUrl(), getSpaQueryString(), getIframeContextInfo(),
                "spark_admin_iframe_");
        return "input";
    }


    @Override
    public String getBodyAsHtml() {
        return body;
    }


    @Override
    public String getIframeContextInfo() {
        return "admin";
    }


    @Override
    public String getSpaQueryString() {
        return ConfluenceIframeSparkActionHelper.getQueryString(this);
    }


    @Override
    public String getSpaBaseUrl() {
        return sparkSpaBaseUrl;
    }


    public void setSparkSpaBaseUrl(String sparkSpaBaseUrl) {
        this.sparkSpaBaseUrl = sparkSpaBaseUrl;
    }


    @Override
    public String getSelectedWebItem() {
        return sparkSelectedWebItemKey;
    }


    public void setSparkSelectedWebItemKey(String sparkSelectedWebItemKey) {
        this.sparkSelectedWebItemKey = sparkSelectedWebItemKey;
    }


    @Override
    public List<String> getRequiredResourceKeys() {
        return ConfluenceIframeSparkActionHelper.splitWebResourceKeys(sparkRequiredWebResourceKeys);
    }


    public void setSparkRequiredWebResourceKeys(String sparkRequiredWebResourceKeys) {
        this.sparkRequiredWebResourceKeys = sparkRequiredWebResourceKeys;
    }

}
