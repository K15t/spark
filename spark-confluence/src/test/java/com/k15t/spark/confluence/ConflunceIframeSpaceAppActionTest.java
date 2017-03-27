package com.k15t.spark.confluence;

import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.google.common.collect.ImmutableMap;
import com.k15t.spark.base.Keys;
import com.k15t.spark.base.util.DocumentOutputUtil;
import com.opensymphony.xwork.Action;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.times;


public class ConflunceIframeSpaceAppActionTest extends ConfluenceSpaceAppActionTestCommon {

    private ConfluenceIframeSpaceAppAction actionInstance;

    private static String testSpaceKey = "test-space-key";

    public static class ConfluenceIframeSpaceAppActionBaseTestImpl extends ConfluenceIframeSpaceAppAction {

        @Override
        protected String getSpaBaseUrl() {
            return "/spark/space/testapp/baseurl/";
        }


        @Override
        public String getTitleAsHtml() {
            return null;
        }


        @Override
        public String getSelectedSpaceToolsWebItem() {
            return null;
        }


        @Override
        public String getSpaceKey() {
            return testSpaceKey;
        }

    }


    @Before
    public void setup() throws Exception {
        super.commonSetup();

        actionInstance = new ConfluenceIframeSpaceAppActionBaseTestImpl();
        actionInstance.setSpace(spaceMock);

    }


    @Test
    public void runActionWithIframeAppResourcePath() throws IOException {

        testSpaceKey = "test-space-key";

        // running index (the method marked to be run by xwork) should initialize the velocity context
        // represented by the action object (ie. getBody() method should return what should be the parameter
        // of 'body' velocity key) when rendering the template matching its return value

        String result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                eq(requestContextPath + "/spark/space/testapp/baseurl/"), startsWith("spark_space_adm_iframe_"),
                anyString(), isNull(String.class));

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

        testSpaceKey = "test-space-key";

        String result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                anyString(), anyString(),
                eq("{\"space_key\": \"test-space-key\"}"), isNull(String.class));


        testSpaceKey = "KEY57";

        result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                anyString(), anyString(),
                eq("{\"space_key\": \"KEY57\"}"), isNull(String.class));

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
                anyString(), eq("?space_key=TEST&user=admin"));


        Mockito.when(servletRequest.getQueryString()).thenReturn("");

        result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                anyString(), anyString(),
                anyString(), eq(""));


        Mockito.when(servletRequest.getQueryString()).thenReturn("test_param=value&other=42&third=75");

        result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                anyString(), anyString(),
                anyString(), eq("test_param=value&other=42&third=75"));

    }


    @Test
    public void useSubclassSpecifiedQueryParam() throws Exception {

        ConfluenceIframeSpaceAppAction instanceOverridingQuery = new ConfluenceIframeSpaceAppActionBaseTestImpl() {

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
                eq(requestContextPath + "/spark/space/testapp/baseurl/"), startsWith("spark_space_adm_iframe"),
                anyString(), eq("start_view=admin_v42"));

        // small sanity check that action was really completed as expected
        String genBody = instanceOverridingQuery.getBodyAsHtml();
        Assert.assertEquals(renderedVelocityToReturn, genBody);

    }


    @Test
    public void useSubclassSpecifiedIframeContextParams() throws Exception {

        ConfluenceIframeSpaceAppAction instanceOverridingIframeContext = new ConfluenceIframeSpaceAppActionBaseTestImpl() {

            @Override
            protected String getIframeContextInfo() {
                return "use_special_test_spa_type";
            }

        };

        String result = instanceOverridingIframeContext.index();

        Assert.assertEquals(Action.INPUT, result);

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                anyString(), anyString(),
                eq("use_special_test_spa_type"), anyString());

        // small sanity check that action was really completed as expected
        String genBody = instanceOverridingIframeContext.getBodyAsHtml();
        Assert.assertEquals(renderedVelocityToReturn, genBody);

        ConfluenceIframeSpaceAppAction otherIframeContextOverrideTest = new ConfluenceIframeSpaceAppActionBaseTestImpl() {

            @Override
            protected String getIframeContextInfo() {
                return "space-key: " + getSpace().getKey() + "; space-name: " + getSpace().getName() +
                        "; start-view: space-admin;";
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
                eq("space-key: key111; space-name: space name; start-view: space-admin;"), anyString());

        // small sanity check that action was really completed as expected
        genBody = otherIframeContextOverrideTest.getBodyAsHtml();
        Assert.assertEquals(renderedVelocityToReturn, genBody);

    }


    @Test
    public void spaBaseUrlIsTakenFromActionConfig() {
        Mockito.when(actionConfig.getParams()).thenReturn(ImmutableMap.of(Keys.SPARK_SPA_BASE_URL, "/hello/world"));
        actionInstance = new ConfluenceIframeSpaceAppAction() {
            @Override
            public String getTitleAsHtml() {
                return null;
            }
        };
        Assert.assertEquals("/hello/world", actionInstance.getSpaBaseUrl());
    }


    @Test(expected = IllegalStateException.class)
    public void spaBaseUrlIsTakenFromActionConfig_MissingParam() {
        Mockito.when(actionConfig.getParams()).thenReturn(Collections.emptyMap());
        actionInstance = new ConfluenceIframeSpaceAppAction() {
            @Override
            public String getTitleAsHtml() {
                return null;
            }
        };
        actionInstance.getSpaBaseUrl();
    }


    @Test
    public void getSelectedWebItem_FromActionConfig() {
        Mockito.when(actionConfig.getParams()).thenReturn(ImmutableMap.of(Keys.SPARK_SELECTED_WEB_ITEM_KEY, "test-item"));
        actionInstance = new ConfluenceIframeSpaceAppAction() {
            @Override
            public String getTitleAsHtml() {
                return null;
            }
        };
        Assert.assertEquals("test-item", actionInstance.getSelectedSpaceToolsWebItem());
    }


    @Test
    public void getSelectedWebItem_RequestParamOverwritesActionConfig() {
        Mockito.when(actionConfig.getParams()).thenReturn(ImmutableMap.of(Keys.SPARK_SELECTED_WEB_ITEM_KEY, "test-item"));
        Mockito.when(servletRequest.getParameter(Keys.SPARK_SELECTED_WEB_ITEM_KEY)).thenReturn("from-request");
        actionInstance = new ConfluenceIframeSpaceAppAction() {
            @Override
            public String getTitleAsHtml() {
                return null;
            }
        };
        Assert.assertEquals("from-request", actionInstance.getSelectedSpaceToolsWebItem());
    }


    @Test
    public void getSelectedWebItem_AllParamsMissing() {
        Mockito.when(actionConfig.getParams()).thenReturn(Collections.emptyMap());
        Mockito.when(servletRequest.getParameter(Keys.SPARK_SELECTED_WEB_ITEM_KEY)).thenReturn(null);
        actionInstance = new ConfluenceIframeSpaceAppAction() {
            @Override
            public String getTitleAsHtml() {
                return null;
            }
        };
        Assert.assertNull(actionInstance.getSelectedSpaceToolsWebItem());
    }

}
