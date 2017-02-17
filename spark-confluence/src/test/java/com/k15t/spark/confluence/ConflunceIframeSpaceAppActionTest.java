package com.k15t.spark.confluence;

import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.k15t.spark.base.util.DocumentOutputUtil;
import com.opensymphony.xwork.Action;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.times;

public class ConflunceIframeSpaceAppActionTest extends ConfluenceSpaceAppActionTestCommon {

    private ConfluenceIframeSpaceAppAction actionInstance;

    @Before
    public void setup() throws Exception {
        super.commonSetup();

        actionInstance = new ConfluenceIframeSpaceAppAction();
        actionInstance.setSpace(spaceMock);

        // set the resource path to point to the test app
        actionConfigParams.put("resource-path", "com/k15t/spark/confluence/testspa/");

    }

    @Test
    public void runActionWithIframeAppResourcePath() throws IOException {

        Mockito.when(spaceMock.getKey()).thenReturn("test-space-key");

        // running index (the method marked to be run by xwork) should initialize the velocity context
        // represented by the action object (ie. getBody() method should return what should be the parameter
        // of 'body' velocity key) when rendering the template matching its return value

        String result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                eq(requestContextPath + "/spark/space/testapp/baseurl/"), eq("spark_space_adm_iframe"),
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
    public void useSubclassSpecifiedQueryParam() throws Exception {

        ConfluenceIframeSpaceAppAction instanceOverridingQuery = new ConfluenceIframeSpaceAppAction() {

            @Override
            protected String getSpaQueryString() {
                return "start_view=admin_v42";
            }

        };
        instanceOverridingQuery.setSpace(spaceMock);

        Mockito.when(servletRequest.getQueryString()).thenReturn("query_param=to_be_overwritten");

        String result = instanceOverridingQuery.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                eq(requestContextPath + "/spark/space/testapp/baseurl/"), eq("spark_space_adm_iframe"),
                anyString(), isNull(String.class), eq("start_view=admin_v42"));

        // small sanity check that action was really completed as expected
        String genBody = instanceOverridingQuery.getBodyAsHtml();
        Assert.assertEquals(renderedVelocityToReturn, genBody);

    }

    @Test
    public void useSubclassSpecifiedIframeContextParams() throws Exception {

        ConfluenceIframeSpaceAppAction instanceOverridingIframeContext = new ConfluenceIframeSpaceAppAction() {

            @Override
            protected String getIframeContextInfo() {
                return "use_special_test_spa_type";
            }

            @Override
            protected String getIframeContextInitializedCallbackName() {
                return "sparkInitReady";
            }
        };

        String result = instanceOverridingIframeContext.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                anyString(), anyString(),
                eq("use_special_test_spa_type"), eq("sparkInitReady"), anyString());

        // small sanity check that action was really completed as expected
        String genBody = instanceOverridingIframeContext.getBodyAsHtml();
        Assert.assertEquals(renderedVelocityToReturn, genBody);

        ConfluenceIframeSpaceAppAction otherIframeContextOverrideTest = new ConfluenceIframeSpaceAppAction() {

            @Override
            protected String getIframeContextInfo() {
                return "space-key: " + getSpace().getKey() + "; space-name: " + getSpace().getName() +
                        "; start-view: space-admin;";
            }

            @Override
            protected String getIframeContextInitializedCallbackName() {
                return "sparkInit2";
            }
        };
        otherIframeContextOverrideTest.setSpace(spaceMock);
        Mockito.when(spaceMock.getKey()).thenReturn("key111");
        Mockito.when(spaceMock.getName()).thenReturn("space name");

        result = otherIframeContextOverrideTest.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                anyString(), anyString(),
                eq("space-key: key111; space-name: space name; start-view: space-admin;"),
                eq("sparkInit2"), anyString());

        // small sanity check that action was really completed as expected
        genBody = otherIframeContextOverrideTest.getBodyAsHtml();
        Assert.assertEquals(renderedVelocityToReturn, genBody);

    }


}
