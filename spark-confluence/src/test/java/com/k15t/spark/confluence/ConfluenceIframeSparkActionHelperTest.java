package com.k15t.spark.confluence;

import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.k15t.spark.base.util.DocumentOutputUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.k15t.spark.confluence.ConfluenceIframeSparkActionHelper.splitWebResourceKeys;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.times;


public class ConfluenceIframeSparkActionHelperTest extends ConfluenceSpaceAppActionTestCommon {

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
            Assert.fail("unexpected usage (real rendering)");
            return null;
        }


        @Override
        public List<String> getRequiredResourceKeys() {
            Assert.fail("unexpected usage (real rendering)");
            return null;
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

        String result = ConfluenceIframeSparkActionHelper.renderSparkIframeBody(testInstance, "test_base_id_");

        // except to be called once and with expected arguments
        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.generateAdminIframeTemplateContext(
                eq("/test/app/"), startsWith("test_base_id_"),
                eq("test-context"), eq("test-query-string=test"));

        PowerMockito.verifyStatic(times(1));
        DocumentOutputUtil.getIframeAdminContentWrapperTemplate();

        // check that the admin template and context dummies were used for rendering body
        // not very ideal test because ties how the rendering is expected to be done which is unimportant...
        // on the other hand making the VelocityUtils call without mocking it would require lots of other setup
        PowerMockito.verifyStatic(times(1));
        VelocityUtils.getRenderedContent((CharSequence) velocityTemplateToReturn, velocityContextToReturn);

        // expect to return the 'rendered' content
        assertEquals(renderedVelocityToReturn, result);

        // the velocity template used is checked DocumentOutputUtil, so here is enough to test that expected content is used

    }


    @Test
    public void splitWebResourceKeys_Simple() {
        List<String> expected = Arrays.asList("com.k15t.test.sparkplugin:important-resource.js");
        assertEquals(expected, splitWebResourceKeys("com.k15t.test.sparkplugin:important-resource.js"));
        assertEquals(expected, splitWebResourceKeys("com.k15t.test.sparkplugin:important-resource.js,"));
        assertEquals(expected, splitWebResourceKeys("com.k15t.test.sparkplugin:important-resource.js ,"));
        assertEquals(expected, splitWebResourceKeys("com.k15t.test.sparkplugin:important-resource.js , ,,"));
    }


    @Test
    public void splitWebResourceKeys_Empty() {
        assertEquals(0, splitWebResourceKeys(null).size());
        assertEquals(0, splitWebResourceKeys("").size());
        assertEquals(0, splitWebResourceKeys(" ").size());
        assertEquals(0, splitWebResourceKeys(",").size());
        assertEquals(0, splitWebResourceKeys(" , ").size());
        assertEquals(0, splitWebResourceKeys(" , ,,").size());
    }


    @Test
    public void splitWebResourceKeys_MultipleCompleteModuleKeys() {
        List<String> expected = Arrays.asList("com.k15t.test.sparkplugin:important-resource.js",
                "com.k15t.test.sparkplugin:less-important-resource.js",
                "com.k15t.test.sparkplugin:some-styling.css");
        String keyString = "com.k15t.test.sparkplugin:important-resource.js, ,,com.k15t.test.sparkplugin:less-important-resource.js,"
                + "com.k15t.test.sparkplugin:some-styling.css";
        assertEquals(expected, splitWebResourceKeys(keyString));
    }

}
