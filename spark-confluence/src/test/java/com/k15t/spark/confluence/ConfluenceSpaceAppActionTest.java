package com.k15t.spark.confluence;

import com.opensymphony.xwork.Action;
import org.hamcrest.CoreMatchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.powermock.api.mockito.PowerMockito.mock;

public class ConfluenceSpaceAppActionTest extends ConfluenceSpaceAppActionTestCommon {

    protected static ConfluenceSpaceAppAction actionInstance;

    @Before
    public void setup() throws Exception {

        super.commonSetup();

        actionInstance = new ConfluenceSpaceAppAction();

        actionInstance.setSpace(spaceMock);

        // set the resource path to point to the test app
        actionConfigParams.put("resource-path", "com/k15t/spark/confluence/testspa/");

    }

    @Test
    public void runActionWithNonIframeAppResourcePath() {

        // running index (the method marked to be run by xwork) should initialize the velocity context
        // represented by the action object (ie. getBody() method should return what should be the parameter
        // of 'body' velocity key) when rendering the template matching its return value

        String result = actionInstance.index();

        Assert.assertEquals(Action.INPUT, result);

        String genBody = actionInstance.getBodyAsHtml();

        Document genBodyDoc = Jsoup.parse("<html><head></head><body>" + genBody + "</body></html>");

        // check that the marker element from the test index-file's body was included in the result
        Elements theMarkerElement = genBodyDoc.select("#marked_element");
        Assert.assertEquals(1, theMarkerElement.size());

        // test that the scripts and styles with relative paths were fixed to work in Confluence context
        Assert.assertEquals("/test/app/spark/space/testapp/baseurl/test-script.js",
                genBodyDoc.body().select("script").get(0).attr("src"));
        Assert.assertEquals("/test/app/spark/space/testapp/baseurl/test-styles.css",
                genBodyDoc.body().select("link").get(0).attr("href"));

    }

    @Test
    public void failsWhenSpaBasePathNotSpecified() {

        // set the resource path to point to the test app
        actionConfigParams.put("resource-path", "com/k15t/spark/confluence/testspanometa/");

        String result = actionInstance.index();
        Assert.assertEquals(Action.INPUT, result);

        Assert.assertThat(actionInstance.getBodyAsHtml(),
                CoreMatchers.containsString("Error parsing HTML"));

    }

    @Test
    public void subclassesCanSpecifySpaBasePathProgrammatically() {

        // set the resource path to point to the test app
        actionConfigParams.put("resource-path", "com/k15t/spark/confluence/testspanometa/");

        ConfluenceSpaceAppAction actionOverridingSpaPath = new ConfluenceSpaceAppAction() {

            @Override
            protected String getSpaBaseUrl() {
                return "overridden/test/base/path/";
            }

        };

        String result = actionOverridingSpaPath.index();

        Assert.assertEquals(Action.INPUT, result);

        String genBody = actionOverridingSpaPath.getBodyAsHtml();

        Document genBodyDoc = Jsoup.parse("<html><head></head><body>" + genBody + "</body></html>");

        // check that the marker element from the test index-file's body was included in the result
        Elements theMarkerElement = genBodyDoc.select("#marked_element");
        Assert.assertEquals(1, theMarkerElement.size());

        // test that the scripts and styles with relative paths were fixed to work in Confluence context
        Assert.assertEquals("/test/app/overridden/test/base/path/test-script.js",
                genBodyDoc.body().select("script").get(0).attr("src"));
        Assert.assertEquals("/test/app/overridden/test/base/path/test-styles.css",
                genBodyDoc.body().select("link").get(0).attr("href"));


    }


}
