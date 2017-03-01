package com.k15t.spark.confluence;

import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.k15t.spark.base.util.DocumentOutputUtil;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionProxy;
import com.opensymphony.xwork.config.entities.ActionConfig;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ServletActionContext.class, ActionContext.class, DocumentOutputUtil.class, VelocityUtils.class})
public class ConfluenceSpaceAppActionTestCommon {

    protected static ActionConfig actionConfig;
    protected static Space spaceMock;
    protected static HttpServletRequest servletRequest;

    protected static Map<String, Object> velocityContextToReturn = new HashMap<>();
    protected static String velocityTemplateToReturn = "mocked template";
    protected static Map<String, String> actionConfigParams = new HashMap<>();
    protected static String renderedVelocityToReturn = "velocity rendering result";
    protected static String requestContextPath = "/test/app";


    public void commonSetup() throws Exception {

        // Set up mocks so that the call ServletActionConfig.getContext().getActionInvocation().getProxy().getConfig()
        // will return this mock ActionConfig object instead of throwing NullPointerException or similar
        // Same for ServletActionContext.getRequest().getContextPath()

        actionConfig = mock(ActionConfig.class);

        // the ServletActionContext.getConfig() called by ConfluenceSpaceAppAction.index() is really a static method
        // on the ActionContext (that ServletActionContext extends), so the ActionContext.getConfig() has to be mocked
        PowerMockito.mockStatic(ActionContext.class);
        // but ServletActionContext.getRequest is really on ServletActionContext
        PowerMockito.mockStatic(ServletActionContext.class);

        ActionContext actionContext = mock(ActionContext.class);
        ActionInvocation actionInvocation = mock(ActionInvocation.class);
        ActionProxy actionProxy = mock(ActionProxy.class);

        Mockito.when(ActionContext.getContext()).thenReturn(actionContext);
        Mockito.when(actionContext.getActionInvocation()).thenReturn(actionInvocation);
        Mockito.when(actionInvocation.getProxy()).thenReturn(actionProxy);
        Mockito.when(actionProxy.getConfig()).thenReturn(actionConfig);

        servletRequest = mock(HttpServletRequest.class);

        Mockito.when(ServletActionContext.getRequest()).thenReturn(servletRequest);
        Mockito.when(servletRequest.getContextPath()).thenReturn(requestContextPath);

        // Return the static params map
        Mockito.when(actionConfig.getParams()).thenReturn(actionConfigParams);

        actionConfigParams.put("resource-path", "/spark/test-path/");

        // Set up spies for the
        PowerMockito.mockStatic(DocumentOutputUtil.class);

        Mockito.when(DocumentOutputUtil.generateAdminIframeTemplateContext(anyString(), anyString(),
                anyString(), anyString(), anyString())).thenReturn(velocityContextToReturn);

        Mockito.when(DocumentOutputUtil.getIframeAdminContentWrapperTemplate()).thenReturn(velocityTemplateToReturn);

        // Mock up the conflunce velocity util
        PowerMockito.mockStatic(VelocityUtils.class);

        Mockito.when(VelocityUtils.getRenderedContent(anyString(), Mockito.anyMap())).
                thenReturn(renderedVelocityToReturn);

        // add a mock for current space context
        spaceMock = mock(Space.class);

    }


}
