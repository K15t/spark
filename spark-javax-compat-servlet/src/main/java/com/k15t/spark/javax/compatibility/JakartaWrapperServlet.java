package com.k15t.spark.javax.compatibility;

import io.atlassian.util.adapter.jakarta.JakartaAdapters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;

import java.io.IOException;


/**
 * <p>A reusable Javax HttpServlet implementation that delegates request handling to a specified Jakarta HttpServlet class.</p>
 * The typical use case is to use this in Atlassian apps compatible with platform versions 7 and 8 by declaring it alongside the normal
 * servlet module as seen below.
 * <ul>
 *     <li>{@code <restrict>} elements ensure that only one of the module declarations is used depending on the host product version.</li>
 *     <li>
 *         The {@link JakartaWrapperServlet} receives the same init parameters as the original servlet and its fully qualified classname as
 *         an additional parameter.
 *     </li>
 * </ul>
 * <pre>{@code
 *     <servlet name="UI Servlet" key="ui-servlet" class="com.k15t.example.UiServlet">    <!-- Implements the Jakarta servlet API -->
 *         <restrict application="confluence" version="(9.99.99,)"/>
 *         <url-pattern>/example/ui/*</url-pattern>
 *         <init-param>
 *             <param-name>resource-path</param-name>
 *             <param-value>/com/k15t/example/ui/static</param-value>
 *         </init-param>
 *     </servlet>
 *     <servlet name="UI Servlet" key="ui-servlet" class="com.k15t.spark.javax.compatibility.JakartaWrapperServlet">
 *         <restrict application="confluence" version="(,9.99.99]"/>
 *         <url-pattern>/example/ui/*</url-pattern>
 *         <init-param>
 *             <param-name>resource-path</param-name>
 *             <param-value>/com/k15t/example/ui/static</param-value>
 *         </init-param>
 *         <init-param>
 *             <param-name>jakarta-servlet-class</param-name>
 *             <param-value>com.k15t.example.UiServlet</param-value>    <!-- The same servlet class as in the above module -->
 *         </init-param>
 *     </servlet>
 * }</pre>
 * <p>Please note: This servlet is not specific to spark and can be used for all kind of delegate servlets.</p>
 */
public class JakartaWrapperServlet extends javax.servlet.http.HttpServlet {

    @Autowired AutowireCapableBeanFactory beanFactory;

    private HttpServlet delegate;


    @Override
    public void init(ServletConfig config) throws javax.servlet.ServletException {
        super.init(config);

        String delegateClassName = config.getInitParameter("jakarta-servlet-class");
        Class<?> delegateClass;
        try {
            delegateClass = JakartaWrapperServlet.class.getClassLoader().loadClass(delegateClassName);
        } catch (ClassNotFoundException e) {
            throw new javax.servlet.ServletException("Could not load class: " + delegateClassName, e);
        }

        if (!HttpServlet.class.isAssignableFrom(delegateClass)) {
            throw new javax.servlet.ServletException("Class " + delegateClassName + " is not a jakarta HttpServlet subclass.");
        }

        //noinspection unchecked
        delegate = beanFactory.createBean((Class<HttpServlet>) delegateClass);

        try {
            delegate.init(JakartaAdapters.asJakarta(config));
        } catch (ServletException e) {
            throw new javax.servlet.ServletException(e);
        }
    }


    @Override
    protected void service(javax.servlet.http.HttpServletRequest javaxRequest, javax.servlet.http.HttpServletResponse javaxResponse)
            throws javax.servlet.ServletException, IOException {

        HttpServletRequest request = JakartaAdapters.asJakarta(javaxRequest);
        HttpServletResponse response = JakartaAdapters.asJakarta(javaxResponse);
        try {
            delegate.service(request, response);
        } catch (ServletException e) {
            throw new javax.servlet.ServletException(e);
        }
    }


    @Override
    public void destroy() {
        delegate.destroy();
    }


    @Override
    public String getServletInfo() {
        return delegate.getServletInfo();
    }

}
