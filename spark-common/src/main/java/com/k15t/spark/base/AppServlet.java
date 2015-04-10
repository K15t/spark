package com.k15t.spark.base;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <p>Serves resources.</p>
 * <p>As an example, this servlet is supposed to be listening to the URL pattern <code>https://example.com/servlet/**</code>.
 * Inside the classpath, there is a directory <code>/webapp</code> that contains a web application. The file
 * <code>/scripts/test.js</code> from this directory can be accessed using the following two URLs:
 * <code>https://example.com/servlet/scripts/test.js</code> and
 * <code>https://example.com/servlet/_/&lt;deploymentNumber&gt;/&lt;localeKey&gt;/scripts/test.js</code>.</p>
 * <p>The following terminology will be used in this code:</p>
 * <ul>
 * <li><code>resourcePath</code> is <code>&quot;/webapp/&quot;</code></li>
 * <li><code>cacheKey</code> would be <code>&quot;_/&lt;deploymentNumber&gt;/&lt;localeKey&gt;/&quot;</code> in the
 * second URL</li>
 * <li><code>localPath</code> is <code>&quot;scripts/test.js</code></li>
 * <li><code>localUrlPart</code> is <code>&quot;/scripts/test.js</code> in the first example,
 * <code>/_/&lt;deploymentNumber&gt;/&lt;localeKey&gt;/scripts/test.js</code> in the second.</li>
 * </ul>
 */

abstract public class AppServlet extends HttpServlet {

    protected final Set<String> VELOCITY_TYPES = Collections.unmodifiableSet(new HashSet<String>() {{
        add("text/html");
        add("text/css");
    }});

    protected final Set<String> JAVASCRIPT_I18N_TYPES = Collections.unmodifiableSet(new HashSet<String>() {{
        add("application/javascript");
    }});

    private String resourcePath;

    // Copied from auiplugin
    protected static final Pattern JAVASCRIPT_I18N_PATTERN = Pattern.compile("AJS\\.I18n\\.getText\\(\\s*(['\"])([\\w.-]+)\\1\\s*([\\),])");


    @Override
    public void init() throws ServletException {
        resourcePath = getServletConfig().getInitParameter(Keys.RESOURCE_PATH);
        if (resourcePath == null) {
            throw new ServletException(Keys.RESOURCE_PATH + " parameter is not defined");
        }

        if (!"/".equals(resourcePath.substring(resourcePath.length() - 1))) {
            resourcePath = resourcePath + "/";
        }
    }


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
            ServletException {

        RequestProperties props = getRequestProperties(request);

        if (!verifyPermissions(props, response)) {
            return;
        }

        if (props.shouldCache()) {
            response.setHeader("Cache-Control", "public");
        } else {
            response.setHeader("Cache-Control", "no-cache,must-revalidate");
        }

        response.setContentType(props.getContentType());

        if (!sendOutput(props, response)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }


    protected RequestProperties getRequestProperties(HttpServletRequest request) {
        return new RequestProperties(this, request);
    }


    protected boolean sendOutput(RequestProperties props, HttpServletResponse response) throws IOException {
        InputStream resource = getPluginResource(props.getLocalPath());
        if (resource == null) {
            return false;
        }

        // this is a bit of a hack to set the contextPath as global variable in JavaScript
        /*if ("index.html".equals(path)) {
            String rendered = renderer.renderFragment(IOUtils.toString(in, "UTF-8"),
                ImmutableMap.<String, Object>of("contextPath", contextPath));
            in = new ByteArrayInputStream(rendered.getBytes(Charset.forName("UTF-8")));
        }*/

        String shortType = StringUtils.substringBefore(props.getContentType(), ";");
        if (VELOCITY_TYPES.contains(shortType)) {
            renderVelocity(props, response, resource);
        } else if (JAVASCRIPT_I18N_TYPES.contains(shortType)) {
            renderJavaScriptI18n(props, response, resource);
        } else {
            IOUtils.copy(resource, response.getOutputStream());
        }

        return true;
    }


    abstract protected void renderVelocity(RequestProperties props, HttpServletResponse response, InputStream template) throws IOException;


    protected boolean verifyPermissions(RequestProperties props, HttpServletResponse response) throws IOException {
        return true;
    }


    protected String getPluginResourcePath(String localPath) {
        if (StringUtils.startsWith(localPath, "auing/")) {
            return "/com/k15t/auing/common/" + StringUtils.removeStart(localPath, "auing/");
        } else {
            return resourcePath + localPath;
        }
    }


    protected InputStream getPluginResource(String localPath) throws IOException {
        if (isDevMode()) {
            InputStream dev = loadFromDevelopmentDirectory(getPluginResourcePath(localPath));
            if (dev != null) {
                return dev;
            }
        }

        return getClass().getClassLoader().getResourceAsStream(getPluginResourcePath(localPath));
    }


    /**
     * @return true if the host application is running in development mode.
     */
    protected abstract boolean isDevMode();


    protected InputStream loadFromDevelopmentDirectory(String localPath) throws IOException {
        InputStream fileIn = null;
        String resourceDirectoryPaths = System.getProperty(Keys.SPARK_RESOURCE_DIRECTORIES);

        if (resourceDirectoryPaths == null) {
            return null;
        }

        for (String resourceDirectoryPath : StringUtils.split(resourceDirectoryPaths, ",")) {
            resourceDirectoryPath = resourceDirectoryPath.trim();
            resourceDirectoryPath = StringUtils.removeEnd(resourceDirectoryPath, "/") + "/";
            File resourceDirectory = new File(resourceDirectoryPath);

            if (resourceDirectory.isDirectory()) {
                File resource = new File(resourceDirectoryPath + localPath);
                if (resource.canRead()) {
                    fileIn = FileUtils.openInputStream(resource);
                    break;
                }
            }
        }

        return fileIn;
    }


    protected void renderJavaScriptI18n(RequestProperties props, HttpServletResponse response, InputStream template) throws IOException {
        String javaScript = IOUtils.toString(template);

        // Copied from auiplugin

        Matcher matcher = JAVASCRIPT_I18N_PATTERN.matcher(javaScript);

        PrintWriter out = response.getWriter();

        int index = 0;
        while (matcher.find()) {
            out.write(javaScript.substring(index, matcher.start()));
            index = matcher.end();

            String key = matcher.group(2);
            boolean format = ",".equals(matcher.group(3));

            if (format) {
                out.write("AJS.format(\"" + StringEscapeUtils.escapeJavaScript(getText(key)) + "\",");
            } else {
                out.write("\"" + StringEscapeUtils.escapeJavaScript(getText(key)) + "\"");
            }
        }
        out.write(javaScript.substring(index, javaScript.length()));
        out.close();
    }


    abstract protected String getText(String key);


}
