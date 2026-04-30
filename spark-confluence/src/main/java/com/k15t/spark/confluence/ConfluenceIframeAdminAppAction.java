package com.k15t.spark.confluence;

import com.atlassian.confluence.setup.BuildInformation;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.user.User;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public abstract class ConfluenceIframeAdminAppAction extends ConfluenceIframeAppAction {


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


}
