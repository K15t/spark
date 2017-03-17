package com.k15t.spark.atlassian;

import com.atlassian.templaterenderer.TemplateRenderer;
import com.k15t.spark.base.Keys;
import com.k15t.spark.base.RequestProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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


    @Test
    public void servesAdminContentByDefault() throws Exception {

        Mockito.when(servletConfig.getInitParameter(Keys.SPARK_SELECTED_WEB_ITEM_KEY)).thenReturn("test-web-item-id");

        String prepIndexRes = testInstance.prepareIndexHtml(
                "<html><head><script src='test.js'/></head><body><p id='body-el'>test</p></body></html>",
                props);

        Document resDoc = Jsoup.parse(prepIndexRes);

        // adds 'atl.admin' decorator
        Elements addedMetaTags = resDoc.select("meta");
        Assert.assertEquals(1, addedMetaTags.size());

        Assert.assertEquals("decorator", addedMetaTags.attr("name"));
        Assert.assertEquals("atl.admin", addedMetaTags.attr("content"));

        // selected web item decorator added
        Elements selWebItemDec = resDoc.body().select("content");
        Assert.assertEquals(1, selWebItemDec.size());

        Assert.assertEquals("selectedWebItem", selWebItemDec.attr("tag"));
        Assert.assertEquals("test-web-item-id", selWebItemDec.text());

        // script element removed from head (wouldn't work there with admin decorator anyway)
        Elements scriptEl = resDoc.head().select("script");
        Assert.assertEquals(0, scriptEl.size());

        // body contains the expected iframe wrapping template
        // (and nothing else except for the selectedWebItem content decorator)
        resDoc.body().select("content").remove();
        Assert.assertEquals("test render result", resDoc.body().html());

    }


    @Test
    public void setSelectedWebItemProgrammatically() throws Exception {

        Mockito.when(servletConfig.getInitParameter(Keys.SPARK_SELECTED_WEB_ITEM_KEY)).thenReturn("wrong-web-item-id");

        Mockito.when(request.getParameter(Keys.SPARK_SELECTED_WEB_ITEM_KEY)).thenReturn("also-wrong-item-id");

        // this should be added instead
        Mockito.when(testInstance.getSelectedWebItemKey(props)).thenReturn("correct-web-key");

        String prepIndexRes = testInstance.prepareIndexHtml(
                "<html><head><script src='test.js'/></head><body><p id='body-el'>test</p></body></html>",
                props);

        Document resDoc = Jsoup.parse(prepIndexRes);

        // selected web item decorator added
        Elements selWebItemDec = resDoc.body().select("content");
        Assert.assertEquals(1, selWebItemDec.size());

        Assert.assertEquals("selectedWebItem", selWebItemDec.attr("tag"));
        Assert.assertEquals("correct-web-key", selWebItemDec.text());

    }


    @Test
    public void selectedWebItemCanBeLeftUnspecified() throws Exception {

        // test mostly just that not specifying the init param does not throw exceptions (even though empty
        // selectedWebItem-tag won't be able to select anything, but that is not critical)

        Mockito.when(servletConfig.getInitParameter(Keys.SPARK_SELECTED_WEB_ITEM_KEY)).thenReturn(null);

        String prepIndexRes = testInstance.prepareIndexHtml(
                "<html><head><script src='test.js'/></head><body><p id='body-el'>test</p></body></html>",
                props);

        Document resDoc = Jsoup.parse(prepIndexRes);

        // selected web item decorator added
        Elements selWebItemDec = resDoc.body().select("content");
        Assert.assertEquals(1, selWebItemDec.size());

        Assert.assertEquals("selectedWebItem", selWebItemDec.attr("tag"));
        Assert.assertEquals("", selWebItemDec.text());

    }


    @Test
    public void selectedWebItemCanBeSetByQueryParam() throws Exception {

        Mockito.when(servletConfig.getInitParameter(Keys.SPARK_SELECTED_WEB_ITEM_KEY)).thenReturn("wrong-web-item-id");

        // this should win the selected web item set in the init config
        Mockito.when(request.getParameter(Keys.SPARK_SELECTED_WEB_ITEM_KEY)).thenReturn("query-param-web-item-id");

        String prepIndexRes = testInstance.prepareIndexHtml(
                "<html><head><script src='test.js'/></head><body><p id='body-el'>test</p></body></html>",
                props);

        Document resDoc = Jsoup.parse(prepIndexRes);

        // selected web item decorator added
        Elements selWebItemDec = resDoc.body().select("content");
        Assert.assertEquals(1, selWebItemDec.size());

        Assert.assertEquals("selectedWebItem", selWebItemDec.attr("tag"));
        Assert.assertEquals("query-param-web-item-id", selWebItemDec.text());

    }


    @Test
    public void servesIframeContentOnQueryParameter() throws Exception {

        Mockito.when(servletConfig.getInitParameter(Keys.SPARK_SELECTED_WEB_ITEM_KEY)).thenReturn("test-web-item-id");

        Mockito.when(request.getParameter("iframe_content")).thenReturn("true");

        String prepIndexRes = testInstance.prepareIndexHtml(
                "<html><head><script src='test.js'/></head><body><p id='body-el'>test</p></body></html>",
                props);

        Document resDoc = Jsoup.parse(prepIndexRes);

        // no meta added
        Elements addedMetaTags = resDoc.select("meta");
        Assert.assertEquals(0, addedMetaTags.size());

        // no body decorators added
        Elements addedBodyDocs = resDoc.select("content");
        Assert.assertEquals(0, addedBodyDocs.size());

        // script element still there, plus iframeResizer.contentWindow.js added
        Elements scriptEl = resDoc.select("script");
        Assert.assertEquals(2, scriptEl.size());
        Assert.assertEquals("test.js", scriptEl.select("[src]").attr("src"));

        // iframeResizer.contentWindow should be added inline (no src attribute)
        Assert.assertEquals("/* test placeholder of iframeResizer.contentWindow.min.js */",
                scriptEl.select(":not([src])").html());

        // p element in the body still there
        Elements bodyEl = resDoc.select("#body-el");
        Assert.assertEquals(1, bodyEl.size());
        Assert.assertEquals("body-el", bodyEl.attr("id"));
        Assert.assertEquals("test", bodyEl.html());

    }


}
