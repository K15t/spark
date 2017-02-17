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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.mock;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ServletActionContext.class, ActionContext.class, DocumentOutputUtil.class, VelocityUtils.class})
public class ConfluenceSpaceAppActionTest {

    private static ActionConfig actionConfig;
    private static ConfluenceSpaceAppAction actionInstance;
    private static Space spaceMock;
    private static HttpServletRequest servletRequest;

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

        actionInstance.setSpace(spaceMock);

    }

    @Test
    public void runActionWithIframeAppResourcePath() throws IOException {

        // set the resource path to point to the iframe test app
        actionConfigParams.put("resource-path", "com/k15t/spark/confluence/testiframeapp/");
        Mockito.when(spaceMock.getKey()).thenReturn("test-space-key");

        // running index (the method marked to be run by xwork) should initialize the velocity context
        // represented by the action object (ie. getBody() method should return what should be the parameter
        // of 'body' velocity key) when rendering the template matching its return value

        String result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                eq(requestContextPath + "/"), eq("spark_space_adm_iframe"),
                anyString(), isNull(String.class), isNull(String.class));

        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.getIframeAdminContentWrapperTemplate();

        // check that the admin template and context dummies were used for rendering body
        // not very ideal test because ties how the rendering is expected to be done which is unimportant...
        // on the other hand making the VelocityUtils call without mocking it would require lots of other setup
        PowerMockito.verifyStatic(times(1));
        VelocityUtils.getRenderedContent(velocityTemplateToReturn, velocityContextToReturn);

        // expect the rendering result to be used as the 'bodyAsHtml' to be inserted into the action's template
        String genBody = actionInstance.getBodyAsHtml();
        Assert.assertEquals(renderedVelocityToReturn, genBody);

        // the velocity template used is checked DocumentOutputUtil, so here is enough to test that expected content is used

    }

    @Test
    public void iframeSpaceActionDefaultContextInfo() throws IOException {

        // set the resource path to point to the iframe test app
        actionConfigParams.put("resource-path", "com/k15t/spark/confluence/testiframeapp/");

        Mockito.when(spaceMock.getKey()).thenReturn("test-space-key");

        String result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                anyString(), anyString(),
                eq("{\"space_key\": \"test-space-key\"}"), isNull(String.class), isNull(String.class));


        Mockito.when(spaceMock.getKey()).thenReturn("KEY57");

        result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                anyString(), anyString(),
                eq("{\"space_key\": \"KEY57\"}"), isNull(String.class), isNull(String.class));


    }

    @Test
    public void iframeSpaceActionDefaultQueryParamHandling() throws IOException {

        // set the resource path to point to the iframe test app
        actionConfigParams.put("resource-path", "com/k15t/spark/confluence/testiframeapp/");

        Mockito.when(servletRequest.getQueryString()).thenReturn("?space_key=TEST&user=admin");

        String result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                anyString(), anyString(),
                anyString(), isNull(String.class), eq("?space_key=TEST&user=admin"));


        Mockito.when(servletRequest.getQueryString()).thenReturn("");

        result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                anyString(), anyString(),
                anyString(), isNull(String.class), eq(""));


        Mockito.when(servletRequest.getQueryString()).thenReturn("test_param=value&other=42&third=75");

        result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                anyString(), anyString(),
                anyString(), isNull(String.class), eq("test_param=value&other=42&third=75"));

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

        Document genBodyDoc = Jsoup.parse("<html><head></head><body>" + genBody + "</body></html>");

        // check that the marker element from the test index-file's body was included in the result
        Elements theMarkerElement = genBodyDoc.select("#should_be_added");
        Assert.assertEquals(1, theMarkerElement.size());

        // test that the scripts and styles with relative paths were fixed to work in Confluence context
        Assert.assertEquals("/test/app/noniframe/testapp/baseurl/test-script.js",
                genBodyDoc.body().select("script").get(0).attr("src"));
        Assert.assertEquals("/test/app/noniframe/testapp/baseurl/test-styles.css",
                genBodyDoc.body().select("link").get(0).attr("href"));

    }


}
