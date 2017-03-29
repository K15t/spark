package com.k15t.spark.atlassian;

import com.atlassian.templaterenderer.TemplateRenderer;
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

    private static AtlassianIframeContentServlet testInstance;

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

        testInstance = Mockito.spy(new AtlassianIframeContentServlet() {
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
    public void servesIframeContent() throws Exception {

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

        // script element still there, plus iframeContent window scripts added
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
