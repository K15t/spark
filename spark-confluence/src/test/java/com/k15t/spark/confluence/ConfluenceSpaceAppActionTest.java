package com.k15t.spark.confluence;

import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.k15t.spark.base.util.DocumentOutputUtil;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionProxy;
import com.opensymphony.xwork.config.entities.ActionConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mock;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ServletActionContext.class, ActionContext.class, DocumentOutputUtil.class, VelocityUtils.class})
public class ConfluenceSpaceAppActionTest {

    private static ActionConfig actionConfig;
    private static ConfluenceSpaceAppAction actionInstance;
    private static Space spaceMock;

    private static Map<String, Object> velocityContextToReturn = new HashMap<>();
    private static String velocityTemplateToReturn = "mocked template";
    private static Map<String, String> actionConfigParams = new HashMap<>();
    private static String renderedVelocityToReturn = "velocity rendering result";
    private static String requestContextPath = "/test/app";

    @Before
    public void setup() throws Exception {

        actionInstance = new ConfluenceSpaceAppAction();

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

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);

        Mockito.when(ServletActionContext.getRequest()).thenReturn(servletRequest);
        Mockito.when(servletRequest.getContextPath()).thenReturn(requestContextPath);

        // Return the static params map
        Mockito.when(actionConfig.getParams()).thenReturn(actionConfigParams);

        actionConfigParams.put("resource-path", "/spark/test-path/");

        // Set up spies for the
        PowerMockito.mockStatic(DocumentOutputUtil.class);

        Mockito.when(DocumentOutputUtil.generateAdminIframeTemplateContext(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(velocityContextToReturn);

        Mockito.when(DocumentOutputUtil.getIframeAdminContentWrapperTemplate()).thenReturn(velocityTemplateToReturn);

        // Mock up the conflunce velocity util
        PowerMockito.mockStatic(VelocityUtils.class);

        Mockito.when(VelocityUtils.getRenderedContent(Mockito.anyString(), Mockito.anyMap())).
                thenReturn(renderedVelocityToReturn);

        // add a mock for current space context
        spaceMock = mock(Space.class);

        actionInstance.setSpace(spaceMock);

    }

    @Test
    public void runActionWithIframeAppResourcePath() {

        // set the resource path to point to the iframe test app
        actionConfigParams.put("resource-path", "com/k15t/spark/confluence/testiframeapp/");
        Mockito.when(spaceMock.getKey()).thenReturn("test-space-key");

        // running index (the method marked to be run by xwork) should initialize the velocity context
        // represented by the action object (ie. getBody() method should return what should be the parameter
        // of 'body' velocity key) when rendering the template matching its return value

        String result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        String genBody = actionInstance.getBodyAsHtml();

        Assert.assertEquals(renderedVelocityToReturn, genBody);

    }

    @Test
    public void runActionWithNonIframeAppResourcePath() {

        // set the resource path to point to the non-iframe test app
        actionConfigParams.put("resource-path", "com/k15t/spark/confluence/testappnoiframe/");

        // running index (the method marked to be run by xwork) should initialize the velocity context
        // represented by the action object (ie. getBody() method should return what should be the parameter
        // of 'body' velocity key) when rendering the template matching its return value

        String result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        String genBody = actionInstance.getBodyAsHtml();

        Document genBodyDoc = Jsoup.parse(genBody);

        Elements theMarkerEr = genBodyDoc.select("#should_be_added");

        Assert.assertEquals(1, theMarkerEr.size());

    }


}
