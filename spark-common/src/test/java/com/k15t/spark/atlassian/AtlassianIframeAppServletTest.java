package com.k15t.spark.atlassian;

import com.atlassian.templaterenderer.TemplateRenderer;
import com.k15t.spark.base.Keys;
import com.k15t.spark.base.RequestProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import java.net.URI;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;


public class AtlassianIframeAppServletTest {

    private static RequestProperties props;

    private static HttpServletRequest request;

    private static TemplateRenderer templateRenderer;

    private static AtlassianIframeAppServlet testInstance;

    private static ServletConfig servletConfig;

    private static String renderResult;

    @Before
    public void setup() throws Exception {
        props = Mockito.mock(RequestProperties.class);
        request = Mockito.mock(HttpServletRequest.class);
        templateRenderer = Mockito.mock(TemplateRenderer.class);

        Mockito.when(props.getUri()).thenReturn(new URI("test"));
        Mockito.when(props.getRequest()).thenReturn(request);

        // instantiate as spy to be able to mock the Servlet.getServletConfig() method that is
        // used for getting the init parameters

        testInstance = Mockito.spy(new AtlassianIframeAppServlet() {
            @Override
            protected boolean isDevMode() {
                return true;
            }
        });
        servletConfig = Mockito.mock(ServletConfig.class);
        Mockito.when(testInstance.getServletConfig()).thenReturn(servletConfig);

        templateRenderer = Mockito.mock(TemplateRenderer.class);
        // stubbing it this way avoids AtlassianAppServlet.getTemplateRenderer being called once
        // which would fail; this is considered bad style in most cases but needed / works here
        Mockito.doReturn(templateRenderer).when(testInstance).getTemplateRenderer();

        renderResult = "test render result";
        Mockito.when(templateRenderer.renderFragment(anyString(), anyMap())).thenReturn(renderResult);

    }

    private static void assertMetaTagCorrect(Element metaEl, String name, String content) {
        Assert.assertEquals(name, metaEl.attr("name"));
        Assert.assertEquals(content, metaEl.attr("content"));
    }

    @Test
    public void testParsingMetaParamsFromInit() throws Exception {

        Mockito.when(servletConfig.getInitParameter(Keys.META_DECORATORS_FOR_ADMIN_IFRAME_WRAPPER)).
                thenReturn("decorator:atl.admin;"
                        + "admin.active.section:system.admin/configuration;"
                        + "admin.active.tab:testing-webitem");

        Document testDoc = Jsoup.parse(testInstance.
                prepareIndexHtml("<html><head></head><body></body></html>", props));

        Elements addedMeta = testDoc.head().select("meta");

        Assert.assertEquals(3, addedMeta.size());

        assertMetaTagCorrect(addedMeta.get(0), "decorator", "atl.admin");
        assertMetaTagCorrect(addedMeta.get(1), "admin.active.section", "system.admin/configuration");
        assertMetaTagCorrect(addedMeta.get(2), "admin.active.tab", "testing-webitem");

        // other test case
        Mockito.when(servletConfig.getInitParameter(Keys.META_DECORATORS_FOR_ADMIN_IFRAME_WRAPPER)).
                thenReturn("test-that-should-be-ignored(with warning);valid:value;other ignaoeurd;m/o.r\\e:'v'a'l'i'd");

        Document testDoc2 = Jsoup.parse(testInstance.
                prepareIndexHtml("<html><head></head><body></body></html>", props));

        Elements addedMeta2 = testDoc2.head().select("meta");

        Assert.assertEquals(2, addedMeta2.size());

        assertMetaTagCorrect(addedMeta2.get(0), "valid", "value");
        assertMetaTagCorrect(addedMeta2.get(1), "m/o.r\\e", "'v'a'l'i'd");

        // one more with some white space
        Mockito.when(servletConfig.getInitParameter(Keys.META_DECORATORS_FOR_ADMIN_IFRAME_WRAPPER)).
                thenReturn("decorator\n        : value\n;\n\n testing-parameter \t : testing-value\n\n\n");

        Document testDoc3 = Jsoup.parse(testInstance.
                prepareIndexHtml("<html><head></head><body></body></html>", props));

        Elements addedMeta3 = testDoc3.head().select("meta");

        Assert.assertEquals(2, addedMeta3.size());

        assertMetaTagCorrect(addedMeta3.get(0), "decorator", "value");
        assertMetaTagCorrect(addedMeta3.get(1), "testing-parameter", "testing-value");


    }

    private static void assertContentElementCorrect(Element contentEl, String name, String content) {
        Assert.assertEquals(name, contentEl.attr("tag"));
        Assert.assertEquals(content, contentEl.text());
    }

    @Test
    public void testParsingContentDecoratorsFromInit() throws Exception {

        Mockito.when(servletConfig.getInitParameter(Keys.BODY_DECORATORS_FOR_ADMIN_IFRAME_WRAPPER)).
                thenReturn("selectedWebItem:test-webitem");

        Document testDoc = Jsoup.parse(testInstance.
                prepareIndexHtml("<html><head></head><body></body></html>", props));

        Elements addedContentEls = testDoc.body().select("content");

        Assert.assertEquals(1, addedContentEls.size());

        assertContentElementCorrect(addedContentEls.get(0), "selectedWebItem", "test-webitem");

        // other test with some white space
        Mockito.when(servletConfig.getInitParameter(Keys.BODY_DECORATORS_FOR_ADMIN_IFRAME_WRAPPER)).
                thenReturn("testing key\n        : value\n;illegal-param;"
                        + "\n\n testing-parameter \t : testing value that should have spaces\n\n\n");

        Document testDoc2 = Jsoup.parse(testInstance.
                prepareIndexHtml("<html><head></head><body></body></html>", props));

        Elements addedContentEls2 = testDoc2.body().select("content");

        Assert.assertEquals(2, addedContentEls2.size());

        assertContentElementCorrect(addedContentEls2.get(0), "testing key", "value");
        assertContentElementCorrect(addedContentEls2.get(1), "testing-parameter",
                "testing value that should have spaces");

    }

    @Test
    public void testSettingParamSpecsProgrammatically() throws Exception {

        Mockito.when(servletConfig.getInitParameter(Keys.BODY_DECORATORS_FOR_ADMIN_IFRAME_WRAPPER)).
                thenReturn("should-not:be-this");
        Mockito.when(servletConfig.getInitParameter(Keys.META_DECORATORS_FOR_ADMIN_IFRAME_WRAPPER)).
                thenReturn("meta-should-not:be-this");

        Mockito.when(testInstance.getMetaDecoratorSpec()).
                thenReturn("test-name:test-value;   meta 2 : value 2");
        Mockito.when(testInstance.getBodyContentDecoratorSpec()).
                thenReturn("body-content:body-value; other body content element : other value ");

        Document testDoc = Jsoup.parse(testInstance.
                prepareIndexHtml("<html><head></head><body></body></html>", props));

        Elements addedMetaTags = testDoc.head().select("meta");
        Elements addedContentEls = testDoc.body().select("content");

        Assert.assertEquals(2, addedMetaTags.size());

        assertMetaTagCorrect(addedMetaTags.get(0), "test-name", "test-value");
        assertMetaTagCorrect(addedMetaTags.get(1), "meta 2", "value 2");

        Assert.assertEquals(2, addedContentEls.size());

        assertContentElementCorrect(addedContentEls.get(0), "body-content", "body-value");
        assertContentElementCorrect(addedContentEls.get(1), "other body content element", "other value");

    }


}
