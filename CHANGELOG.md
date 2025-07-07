# Spark 4.0.0
* Java 11 compile target
* Updated docs and code snippets for `staticParams` interceptor
    * Confluence 8.0 introduced `staticParams` as a replacement for `static-params`, the latter of which has been removed in Confluence 9.0. 
* Updated host product dependencies to Confluence 8.0.0 and Jira 8.13.0

# Spark 3.1.0
* Introduced support for dark mode, by running `SPARK.initializeTheming()` in the parent window.

# Spark 3.0.0

## Main changes
* Java 8 instead of 7
* Removed and updated dependencies
* Removed use of velocity for creating iframe wrapper HTML snippets
* Compatibility with xwork -> struts2 upgrade in Confluence 8
    * Spark 3 will be compatible with Confluence versions from 7.0.1 up to Confluence 8.x (latest tested milestone 8.0.0-m67)
* Removed classes that were intended for non-iframe SPAs.
* The dropped `MessageBundleProvider` feature should be replaced by bundling i18n data in the JS bundle at build time.
* Removed deprecated APIs
* Renamed `AppServlet.prepareIndexHtml` to `customizeHtml` because it can now be invoked for HTML files with other names.
    * `AppServlet.shouldCustomizeHtml` can be overridden in subclasses to control invocation of `customizeHtml`.
    * For backwards compatibility the default implementation of `shouldCustomizeHtml` returns true when requesting `index.html`. 
* Removed `AppServlet.renderVelocity` and `AtlassianAppServlet.getVelocityContext`
    * If still required subclasses can implement it themselves by overriding `customizeHtml` (and `shouldCustomizeHtml`) accordingly.
    * Also removed dependency on `atlassian-template-renderer-api` maven dependency.

## Xwork action declaration changes
Spark 2.x used Xwork specific APIs to retrieve the default action configuration (SPA base url, web resources, selected web item). For compatibility with Confluence 8 this approach could no longer be used.
Instead the actions require the use of the xwork/struts 'static parameters interceptor' which will use setter-based injection to inject the configuration parameter values defined in `atlassian-plugin.xml` into the action instance at runtime.

This requires changes in `atlassian-plugin.xml`:
1. The interceptor needs to be declared in **each** action's config block rather than by using a single `<default-interceptor-ref>` element on package level. That's because
    1. The required interceptor is not part of any of the default interceptor stacks
    2. Only a single `<default-interceptor-ref>` element can be used per `<package>`
    3. Once an action declares an `<interceptor-ref>` tag, the `<default-interceptor-ref>` element on `<package>` level is ignored
2. The parameter names need to be changed to conform to bean naming conventions for Xwork being able to find the corresponding setters:
    1. `spark-spa-base-url` -> `SparkSpaBaseUrl`
    2. `spark-selected-web-item-key` -> `SparkSelectedWebItemKey`
    3.  `spark-required-web-resource-keys` -> `SparkRequiredWebResourceKeys`

Example with spark 2:
```xml
<package name="some-space-tools-actions" extends="default" namespace="/spaces/...">
    <default-interceptor-ref name="defaultStack"/>
    <action name="space-tools" class="some.SpaceToolsTabAction" method="index">
        <result name="input" type="velocity">/some/space-app.vm</result>
        <result name="notpermitted" type="redirect">/pages/pagenotpermitted.action</result>
        <param name="spark-spa-base-url">/plugins/servlet/some/ui/</param>
        <param name="spark-selected-web-item-key">some-space-tools-tab-web-item</param>
        <param name="spark-required-web-resource-keys">...:spark-web-resource</param>
    </action>
</package>
```

Example with spark 3:
```xml
<package name="some-space-tools-actions" extends="default" namespace="/spaces/...">
    <action name="space-tools" class="some.SpaceToolsTabAction" method="index">
        <interceptor-ref name="defaultStack"/>
        <interceptor-ref name="static-params"/>
        <result name="input" type="velocity">/some/space-app.vm</result>
        <result name="notpermitted" type="redirect">/pages/pagenotpermitted.action</result>
        <param name="SparkSpaBaseUrl">/plugins/servlet/some/ui/</param>
        <param name="SparkSelectedWebItemKey">some-space-tools-tab-web-item</param>
        <param name="SparkRequiredWebResourceKeys">...:spark-web-resource</param>
    </action>
</package>
```

## Servlet declaration changes
Due to way the `AtlassianAppServlet` now detects local paths the `<url-pattern>` in `atlassian-plugin.xml` requires a more strict form.

It's no longer allowed to have path segments with mixed prefix and asterisk such as
```xml
<url-pattern>/something*</url-pattern>
```
Instead the asterisk must be in a separate path segment:
```xml
<url-pattern>/something/*</url-pattern>
```
If the servlet must be available at `.../something` without trailing slash, an additional `<url-pattern>` element can be defined:
```xml
<url-pattern>/something/*</url-pattern>
<url-pattern>/something</url-pattern>
```

The strict form allows use of standard servlet APIs to detect local paths inside the SPA instead of reflection-based access to values from `atlassian-plugin.xml` / redefining additional servlet init parameters.
The mixed form causes tomcat to provide unexpected values when calling `HttpServletRequest.getServletPath()` / `HttpServletRequest.getPathInfo()`.
