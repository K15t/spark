package com.k15t.spark.base;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


public class RequestProperties {

    protected static final Pattern CACHE_KEY_PATTERN = Pattern.compile("^/_/[^/]+/[^/]+/");

    private Boolean loadAsIframe = false;

    protected final AppServlet appServlet;
    protected final HttpServletRequest request;

    protected String urlLocalPart;
    protected String localPath;
    protected Boolean hasCacheKey;
    protected Boolean shouldCache;
    protected Locale locale;
    protected URI uri;


    public RequestProperties(AppServlet appServlet, HttpServletRequest request) {
        this.appServlet = appServlet;
        this.request = request;
    }


    public HttpServletRequest getRequest() {
        return request;
    }


    protected String getUrlLocalPart() {
        if (urlLocalPart == null) {
            urlLocalPart = request.getPathInfo();
        }

        if (urlLocalPart == null) {
            urlLocalPart = "";
        }

        return urlLocalPart;
    }


    public String getLocalPath() {
        if (localPath == null) {
            localPath = StringUtils.removeStart(CACHE_KEY_PATTERN.matcher(getUrlLocalPart()).replaceFirst(""), "/");
            if (localPath.equals("")) {
                localPath = "index.html";
            }
        }

        return localPath;
    }


    protected boolean hasCacheKey() {
        if (hasCacheKey == null) {
            hasCacheKey = !(getUrlLocalPart().length() == getLocalPath().length() + 1); // localPath does not contain leading slash
        }

        return hasCacheKey;
    }


    public String getContentType() {
        // Implementation copied from com.k15t.scroll.viewport.theme.ContentType#forPath

        String path = getLocalPath();

        if (path.endsWith(".css")) {
            return "text/css";
        } else if (path.endsWith(".js")) {
            return "text/javascript";
        } else if (path.endsWith(".html") || path.endsWith(".htm")) {
            return "text/html;charset=utf-8";
        } else if (path.endsWith(".png")) {
            return "image/png";
        } else if (path.endsWith(".gif")) {
            return "image/gif";
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith("jpe")) {
            return "image/jpeg";
        } else if (path.endsWith("svg") || path.endsWith("svgz")) {
            return "image/svg+xml";
        } else if (path.endsWith(".tiff") || path.endsWith(".tif")) {
            return "image/tiff";
        } else if (path.endsWith(".bmp") || path.endsWith(".dib")) {
            return "image/bmp";
        } else if (path.endsWith(".woff")) {
            return "application/font-woff";
        } else if (path.endsWith(".woff2")) {
            return "application/font-woff2";
        } else if (path.endsWith(".eot")) {
            return "application/vnd.ms-fontobject";
        } else if (path.endsWith(".ico")) {
            return "image/ico";
        } else if (path.endsWith(".art")) {
            return "image/x-jg";
        } else if (path.endsWith(".ief")) {
            return "image/ief";
        } else if (path.endsWith("pic")) {
            return "image/x-pict";
        } else {
            return "application/octet-stream";
        }
    }


    public boolean shouldCache() {
        if (shouldCache == null) {
            shouldCache = !appServlet.isDevMode() && hasCacheKey()
                    && !("text/html".equals(StringUtils.substringBefore(getContentType(), ";")));
        }

        return shouldCache;
    }


    /**
     * Gets the request uri including query parameters.
     */
    public URI getUri() {
        return getUri(null);
    }


    /**
     * Gets the request uri including query parameters. If the baseUrl is null the base url of the http servlet request will be used.
     */
    protected URI getUri(String baseUrl) {
        if (uri == null) {
            StringBuffer builder;
            if (baseUrl == null) {
                builder = request.getRequestURL();
            } else {
                List<String> segments = new ArrayList<>();
                segments.addAll(Arrays.asList(StringUtils.split(StringUtils.defaultString(baseUrl), '/')));
                segments.addAll(Arrays.asList(StringUtils.split(StringUtils.defaultString(getRequest().getServletPath()), '/')));
                segments.addAll(Arrays.asList(StringUtils.split(StringUtils.defaultString(getRequest().getPathInfo()), '/')));

                builder = new StringBuffer();
                for (String segment : segments) {
                    builder.append("/").append(segment);
                }

                // Add trailing slash if required.
                if (StringUtils.endsWith(getRequest().getPathInfo(), "/")) {
                    builder.append("/");
                }
            }
            if (request.getQueryString() != null) {
                builder.append("?");
                builder.append(request.getQueryString());
            }
            uri = URI.create(builder.toString());
        }

        return uri;
    }


    public Locale getLocale() {
        if (this.locale == null) {
            this.locale = request.getLocale();
        }
        return locale;
    }


    public boolean shouldLoadAsIframe() {
        if (this.loadAsIframe == null) {
            this.loadAsIframe = Boolean.valueOf(request.getParameter("loadAsIframe"));
        }
        return loadAsIframe;
    }


    public boolean isRequestedWithAjax() {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

}
