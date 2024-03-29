package com.k15t.spark.atlassian;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.user.UserManager;
import com.k15t.spark.base.RequestProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.util.Date;


public class AtlassianIframeAppServletTest {

    private static RequestProperties props;

    private static HttpServletRequest request;
    private static HttpServletResponse response;

    private static AtlassianIframeContentServlet testInstance;

    private static ServletConfig servletConfig;
    private LoginUriProvider loginUriProvider;
    private UserManager userManager;
    private LocaleResolver localeResolver;
    private ApplicationProperties applicationProperties;
    private ApplicationContext applicationContext;


    @Before
    public void setup() throws Exception {
        props = Mockito.mock(RequestProperties.class);
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);

        Mockito.when(props.getUri()).thenReturn(new URI("test"));
        Mockito.when(props.getRequest()).thenReturn(request);

        // instantiate as spy to be able to mock the Servlet.getServletConfig() method that is
        // used for getting the init parameters

        loginUriProvider = Mockito.mock(LoginUriProvider.class);
        userManager = Mockito.mock(UserManager.class);
        localeResolver = Mockito.mock(LocaleResolver.class);
        applicationProperties = Mockito.mock(ApplicationProperties.class);
        applicationContext = Mockito.mock(ApplicationContext.class);
        Mockito.when(applicationContext.getStartupDate()).thenReturn(new Date().getTime() - 1000);

        testInstance = Mockito.spy(
                new AtlassianIframeContentServlet(loginUriProvider, userManager, localeResolver, applicationProperties,
                        applicationContext) {
                    @Override
                    protected boolean isDevMode() {
                        return true;
                    }
                });
        servletConfig = Mockito.mock(ServletConfig.class);
        Mockito.when(testInstance.getServletConfig()).thenReturn(servletConfig);
    }


    @Test
    public void servesIframeContent() throws Exception {

        String prepIndexRes = testInstance.customizeHtml(
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
        Assert.assertEquals("/*\\x3C!-- test placeholder of spark-dist.contentWindow.js -->*/",
                scriptEl.select(":not([src])").html());

        // p element in the body still there
        Elements bodyEl = resDoc.select("#body-el");
        Assert.assertEquals(1, bodyEl.size());
        Assert.assertEquals("body-el", bodyEl.attr("id"));
        Assert.assertEquals("test", bodyEl.html());

    }


    @Test
    public void callsCustomizeIframeContentHook() throws Exception {

        String testHtml = "<html><head><script src='test.js'/></head><body><p id='body-el'>test</p></body></html>";

        AtlassianIframeContentServlet customizingInstance = Mockito.spy(
                new AtlassianIframeContentServlet(loginUriProvider, userManager, localeResolver, applicationProperties,
                        applicationContext) {
                    @Override
                    protected boolean isDevMode() {
                        return true;
                    }


                    @Override
                    protected void customizeIframeContentDocument(Document document, RequestProperties props) {
                        document.body().append("<p id='extra-footer'>Custom test footer</p>");
                    }
                });
        Mockito.when(customizingInstance.getServletConfig()).thenReturn(servletConfig);

        String resStr = customizingInstance.customizeHtml(testHtml, props);

        Document resDoc = Jsoup.parse(resStr);

        Assert.assertEquals(2, resDoc.head().children().size());
        Assert.assertEquals("script", resDoc.head().child(0).nodeName());
        Assert.assertEquals("script", resDoc.head().child(1).nodeName());
        Assert.assertEquals("test.js", resDoc.head().child(1).attr("src"));

        Assert.assertEquals(2, resDoc.body().children().size());
        Assert.assertEquals("body-el", resDoc.body().child(0).id());

        Assert.assertEquals("extra-footer", resDoc.body().child(1).id());
        Assert.assertEquals("Custom test footer", resDoc.body().child(1).html());

    }


    @Test
    public void requestHandlingIsStoppedOnPermissionProblem() throws Exception {

        AtlassianIframeContentServlet permissionVerifyingInstance =
                new AtlassianIframeContentServlet(loginUriProvider, userManager, localeResolver, applicationProperties,
                        applicationContext) {
                    @Override
                    protected boolean isDevMode() {
                        return true;
                    }


                    @Override
                    protected RequestProperties getRequestProperties(HttpServletRequest request) {
                        return new RequestProperties(this, request);
                    }


                    @Override
                    protected boolean verifyPermissions(RequestProperties props, HttpServletResponse resp) throws IOException {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return false;
                    }
                };

        permissionVerifyingInstance.doGet(request, response);

        // the request handling should have stopped after 'forbidden' permissions (no more data should be added to response)
        Mockito.verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        Mockito.verifyNoMoreInteractions(response);

    }


}
