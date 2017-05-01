package com.k15t.spark.base;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RequestPropertiesTest {

    @Test
    public void getUri() throws Exception {
        assertUriEquals("https://dummy:8443/test", requestMock("https://dummy:8443/test", null));
        assertUriEquals("https://dummy:8443/test?abc=def", requestMock("https://dummy:8443/test", "abc=def"));
        assertUriEquals("https://dummy:8443/test/123", requestMock("https://dummy:8443/test/123", null));
        assertUriEquals("https://dummy:8443/test/123?abc=def", requestMock("https://dummy:8443/test/123", "abc=def"));
        assertUriEquals("https://dummy:8443", requestMock("https://dummy:8443", null));
        assertUriEquals("https://dummy:8443?abc=def", requestMock("https://dummy:8443", "abc=def"));
        assertUriEquals("https://dummy:8443/", requestMock("https://dummy:8443/", null));
        assertUriEquals("https://dummy:8443/?abc=def", requestMock("https://dummy:8443/", "abc=def"));
    }


    private void assertUriEquals(String expected, HttpServletRequest request) {
        RequestProperties rp = new RequestProperties(null, request);
        assertEquals(URI.create(expected), rp.getUri());
    }


    private HttpServletRequest requestMock(String requestUrl, String query) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(requestUrl != null ? new StringBuffer(requestUrl) : null);
        when(request.getQueryString()).thenReturn(query);
        return request;
    }


    @Test
    public void getUriWithBaseUrl() throws Exception {
        assertUriEquals("/myapp/plugins/servlet/dummy/test", "/myapp", "/plugins/servlet/dummy", "/test", null);
        assertUriEquals("/myapp/plugins/servlet/dummy/test/", "/myapp", "/plugins/servlet/dummy", "/test/", null);
        assertUriEquals("/myapp/plugins/servlet/dummy/test/", "/myapp/", "/plugins/servlet/dummy/", "/test/", null);

        assertUriEquals("/myapp/plugins/servlet/dummy/test?abc=def", "/myapp", "/plugins/servlet/dummy", "/test", "abc=def");
        assertUriEquals("/myapp/plugins/servlet/dummy/test/?abc=def", "/myapp", "/plugins/servlet/dummy", "/test/", "abc=def");
        assertUriEquals("/myapp/plugins/servlet/dummy/test/?abc=def", "/myapp/", "/plugins/servlet/dummy/", "/test/", "abc=def");

        assertUriEquals("/myapp/plugins/servlet/dummy", "/myapp", "/plugins/servlet/dummy", "", null);
        assertUriEquals("/myapp/plugins/servlet/dummy/", "/myapp", "/plugins/servlet/dummy", "/", null);
        assertUriEquals("/myapp/plugins/servlet/dummy/", "/myapp/", "/plugins/servlet/dummy/", "/", null);

        assertUriEquals("/myapp/plugins/servlet/dummy?abc=def", "/myapp", "/plugins/servlet/dummy", "", "abc=def");
        assertUriEquals("/myapp/plugins/servlet/dummy/?abc=def", "/myapp", "/plugins/servlet/dummy", "/", "abc=def");
        assertUriEquals("/myapp/plugins/servlet/dummy/?abc=def", "/myapp/", "/plugins/servlet/dummy/", "/", "abc=def");

        assertUriEquals("/plugins/servlet/dummy/test", "/", "/plugins/servlet/dummy", "/test", null);
        assertUriEquals("/plugins/servlet/dummy/test/", "/", "/plugins/servlet/dummy", "/test/", null);
        assertUriEquals("/plugins/servlet/dummy/test/", "/", "/plugins/servlet/dummy/", "/test/", null);

        assertUriEquals("/plugins/servlet/dummy/test?abc=def", "/", "/plugins/servlet/dummy", "/test", "abc=def");
        assertUriEquals("/plugins/servlet/dummy/test/?abc=def", "/", "/plugins/servlet/dummy", "/test/", "abc=def");
        assertUriEquals("/plugins/servlet/dummy/test/?abc=def", "/", "/plugins/servlet/dummy/", "/test/", "abc=def");

        assertUriEquals("/plugins/servlet/dummy", "/", "/plugins/servlet/dummy", "", null);
        assertUriEquals("/plugins/servlet/dummy/", "/", "/plugins/servlet/dummy", "/", null);
        assertUriEquals("/plugins/servlet/dummy/", "/", "/plugins/servlet/dummy/", "/", null);

        assertUriEquals("/plugins/servlet/dummy?abc=def", "/", "/plugins/servlet/dummy", "", "abc=def");
        assertUriEquals("/plugins/servlet/dummy/?abc=def", "/", "/plugins/servlet/dummy", "/", "abc=def");
        assertUriEquals("/plugins/servlet/dummy/?abc=def", "/", "/plugins/servlet/dummy/", "/", "abc=def");

        assertUriEquals("/plugins/servlet/dummy/test", "", "/plugins/servlet/dummy", "/test", null);
        assertUriEquals("/plugins/servlet/dummy/test/", "", "/plugins/servlet/dummy", "/test/", null);
        assertUriEquals("/plugins/servlet/dummy/test/", "", "/plugins/servlet/dummy/", "/test/", null);

        assertUriEquals("/plugins/servlet/dummy/test?abc=def", "", "/plugins/servlet/dummy", "/test", "abc=def");
        assertUriEquals("/plugins/servlet/dummy/test/?abc=def", "", "/plugins/servlet/dummy", "/test/", "abc=def");
        assertUriEquals("/plugins/servlet/dummy/test/?abc=def", "", "/plugins/servlet/dummy/", "/test/", "abc=def");

        assertUriEquals("/plugins/servlet/dummy", "", "/plugins/servlet/dummy", "", null);
        assertUriEquals("/plugins/servlet/dummy/", "", "/plugins/servlet/dummy", "/", null);
        assertUriEquals("/plugins/servlet/dummy/", "", "/plugins/servlet/dummy/", "/", null);

        assertUriEquals("/plugins/servlet/dummy?abc=def", "", "/plugins/servlet/dummy", "", "abc=def");
        assertUriEquals("/plugins/servlet/dummy/?abc=def", "", "/plugins/servlet/dummy", "/", "abc=def");
        assertUriEquals("/plugins/servlet/dummy/?abc=def", "", "/plugins/servlet/dummy/", "/", "abc=def");

        assertUriEquals("/myapp/plugins/servlet/dummy/test?abc=def", "myapp", "plugins/servlet/dummy", "test", "abc=def");
        assertUriEquals("/myapp/plugins/servlet/dummy/test/?abc=def", "myapp", "plugins/servlet/dummy", "test/", "abc=def");
        assertUriEquals("/myapp/plugins/servlet/dummy/test?abc=def", "myapp", "plugins/servlet/dummy", "/test", "abc=def");
        assertUriEquals("/myapp/plugins/servlet/dummy/test/?abc=def", "myapp/", "plugins/servlet/dummy", "/test/", "abc=def");
    }


    private void assertUriEquals(String expected, String relativeBaseUrl, String servletPath, String pathInServlet, String query) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn(servletPath);
        when(request.getPathInfo()).thenReturn(pathInServlet);
        when(request.getQueryString()).thenReturn(query);
        RequestProperties rp = new RequestProperties(null, request);
        assertEquals(URI.create(expected), rp.getUri(relativeBaseUrl));
    }

}