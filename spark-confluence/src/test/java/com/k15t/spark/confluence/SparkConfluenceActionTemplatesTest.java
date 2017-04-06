package com.k15t.spark.confluence;

import com.k15t.spark.base.util.SparkTestUtils;
import org.junit.Test;


public class SparkConfluenceActionTemplatesTest {

    // some simple tests meant to act as an extra reminder to keep templates and actions using them in sync


    @Test
    public void confluenceSpaceAppActionWorksWithDefaultTemplate() throws Exception {
        SparkTestUtils.testActionClassHasTemplateProps(
                ConfluenceSpaceAppAction.class, "com/k15t/spark/confluence/space-app.vm", "action.viewHelper");
    }


    @Test
    public void confluenceIframeSpaceActionWorksWithDefaultTemplate() throws Exception {
        SparkTestUtils.testActionClassHasTemplateProps(
                ConfluenceIframeSpaceAppAction.class, "com/k15t/spark/confluence/iframe-space-app.vm.example",
                "action.viewHelper", "requiredResourceKey");

    }


    @Test
    public void confluenceIframeAdminActionWorksWithDefaultTemplate() throws Exception {
        SparkTestUtils.testActionClassHasTemplateProps(
                ConfluenceIframeAdminAppAction.class, "com/k15t/spark/confluence/iframe-admin-app.vm.example",
                "requiredResourceKey");

    }

}
