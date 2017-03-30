package com.k15t.spark.confluence;

import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.google.common.collect.ImmutableMap;
import com.k15t.spark.base.Keys;
import com.k15t.spark.base.util.DocumentOutputUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.times;


public class ConflunceIframeSparkActionHelperTest extends ConfluenceSpaceAppActionTestCommon {

    private static ConfluenceSparkIframeAction testInstance = new ConfluenceSparkIframeAction() {
        @Override
        public String getIframeContextInfo() {
            return "test-context";
        }


        @Override
        public String getSpaQueryString() {
            return "test-query-string=test";
        }


        @Override
        public String getSpaBaseUrl() {
            return "/test/app/";
        }


        @Override
        public String getTitleAsHtml() {
            return "test-title";
        }


        @Override
        public String getSelectedWebItem() {
            return "test-web-item";
        }


        @Override
        public String getBodyAsHtml() {
            // this would really be used only in the velocity rendering
            return "test-body";
        }
    };


    @Before
    public void setup() throws Exception {
        super.commonSetup();

    }


    @Test
    public void renderSparkIframeBody() throws IOException {

        // this test specifies the implementation bit too exactly because the actual result is hard to check
        // so this tests mostly that the work is delegated to expected helpers with expected arguments

        String result = ConfluenceIframeSparkActionHelper.renderSparkIframeBody(testInstance, servletRequest, "test_base_id_");

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                eq(requestContextPath + "/test/app/"), startsWith("test_base_id_"),
                eq("test-context"), eq("test-query-string=test"));

        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.getIframeAdminContentWrapperTemplate();

        // check that the admin template and context dummies were used for rendering body
        // not very ideal test because ties how the rendering is expected to be done which is unimportant...
        // on the other hand making the VelocityUtils call without mocking it would require lots of other setup
        PowerMockito.verifyStatic(times(1));
        VelocityUtils.getRenderedContent(velocityTemplateToReturn, velocityContextToReturn);

        // expect to return the 'rendered' content
        Assert.assertEquals(renderedVelocityToReturn, result);

        // the velocity template used is checked DocumentOutputUtil, so here is enough to test that expected content is used

    }


    @Test
    public void spaBaseUrlIsTakenFromActionConfig() {
        Mockito.when(actionConfig.getParams()).thenReturn(ImmutableMap.of(Keys.SPARK_SPA_BASE_URL, "/hello/world"));
        Assert.assertEquals("/hello/world", ConfluenceIframeSparkActionHelper.defaultGetSpaBaseUrl(actionContext));
    }


    @Test(expected = IllegalStateException.class)
    public void spaBaseUrlIsTakenFromActionConfig_MissingParam() {
        Mockito.when(actionConfig.getParams()).thenReturn(Collections.emptyMap());
        ConfluenceIframeSparkActionHelper.defaultGetSpaBaseUrl(actionContext);
    }


    @Test
    public void defaultGetSelectedWebItem_FromActionConfig() {
        Mockito.when(actionConfig.getParams()).thenReturn(ImmutableMap.of(Keys.SPARK_SELECTED_WEB_ITEM_KEY, "test-item"));
        Assert.assertEquals("test-item", ConfluenceIframeSparkActionHelper.defaultGetSelectedWebItem(servletRequest, actionContext));
    }


    @Test
    public void defaultGetSelectedWebItem_RequestParamOverwritesActionConfig() {
        Mockito.when(actionConfig.getParams()).thenReturn(ImmutableMap.of(Keys.SPARK_SELECTED_WEB_ITEM_KEY, "test-item"));
        Mockito.when(servletRequest.getParameter(Keys.SPARK_SELECTED_WEB_ITEM_KEY)).thenReturn("from-request");
        Assert.assertEquals("from-request", ConfluenceIframeSparkActionHelper.defaultGetSelectedWebItem(servletRequest, actionContext));
    }


    @Test
    public void getSelectedWebItem_AllParamsMissing() {
        Mockito.when(actionConfig.getParams()).thenReturn(Collections.emptyMap());
        Mockito.when(servletRequest.getParameter(Keys.SPARK_SELECTED_WEB_ITEM_KEY)).thenReturn(null);
        Assert.assertNull(ConfluenceIframeSparkActionHelper.defaultGetSelectedWebItem(servletRequest, actionContext));
    }

}
