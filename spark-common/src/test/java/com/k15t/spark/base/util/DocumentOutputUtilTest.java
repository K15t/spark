package com.k15t.spark.base.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;


public class DocumentOutputUtilTest {

    private static final String iframeIdContextKey = "iframeId";
    private static final String iframeSrcContextKey = "iframeSrc";
    private static final String iframeResizerJsContextKey = "iframeResizerJs";
    private static final String iframeInjContextVelocityKey = "escapedIframeContext";


    @Test
    public void generateAdminIframeTemplateContext() throws IOException {

        // simple case

        Map<String, Object> res =
                DocumentOutputUtil.generateAdminIframeTemplateContext("/test/base/url/",
                        "iframe-id", "{\"test-key\": \"test-value\"}", "");

        Assert.assertEquals("iframe-id", res.get(iframeIdContextKey));

        Assert.assertEquals("/test/base/url/", res.get(iframeSrcContextKey));

        Assert.assertEquals("/* test mockup of iframeResizer.min.js */", res.get(iframeResizerJsContextKey));

        Assert.assertEquals("{\\\"test-key\\\": \\\"test-value\\\"}", res.get(iframeInjContextVelocityKey));

        // another try, main difference: invalid initCallback function name (should not be added to context)

        res = DocumentOutputUtil.generateAdminIframeTemplateContext("/test2/",
                "id_of_second_iframe", "context information string '+!&", "?space=32");

        Assert.assertEquals("id_of_second_iframe", res.get(iframeIdContextKey));

        Assert.assertEquals("/test2/?space=32", res.get(iframeSrcContextKey));

        Assert.assertEquals("/* test mockup of iframeResizer.min.js */", res.get(iframeResizerJsContextKey));

        Assert.assertEquals("context information string '+!&", res.get(iframeInjContextVelocityKey));

        // one more time, this time also the init callback should be added as expected

        res = DocumentOutputUtil.generateAdminIframeTemplateContext("/test/3/", "id3",
                "{'context': {'pages': ['test1', 'test2'], 'result': {'success': false, 'code': 404}}}",
                "test_value=false&admin=true");

        Assert.assertEquals("id3", res.get(iframeIdContextKey));

        Assert.assertEquals("/test/3/?test_value=false&admin=true", res.get(iframeSrcContextKey));

        Assert.assertEquals("/* test mockup of iframeResizer.min.js */", res.get(iframeResizerJsContextKey));

        Assert.assertEquals(
                "{'context': {'pages': ['test1', 'test2'], 'result': {'success': false, 'code': 404}}}",
                res.get(iframeInjContextVelocityKey));

    }


    @Test
    public void getIframeContentWindowJs() throws IOException {

        String iframeContWinJs = DocumentOutputUtil.getIframeContentWindowJs();

        Assert.assertEquals("/* test placeholder of iframeResizer.contentWindow.min.js */", iframeContWinJs);

    }


    @Test
    public void getIframeAdminContentWrapperTemplate() throws IOException {

        String iframeTemplate = DocumentOutputUtil.getIframeAdminContentWrapperTemplate();

        Document document = Jsoup.parse(iframeTemplate, "testBase");

        // assert that there is iframeElement and that it uses the expeted context variables

        Elements iframeElement = document.select("iframe");

        Assert.assertEquals(1, iframeElement.size());

        assertTemplateReferencesVelocityVariable(iframeSrcContextKey, iframeElement.attr("src"));
        assertTemplateReferencesVelocityVariable(iframeIdContextKey, iframeElement.attr("id"));

        // assert that there are two script elements and that they use expected context variables

        Elements scriptElements = document.select("script");

        Assert.assertEquals(2, scriptElements.size());

        // first script element should only add the resizer-js as unescaped text to the (inline) script element
        Element resizeScriptEl = scriptElements.get(0);
        assertTemplateReferencesVelocityVariable(iframeResizerJsContextKey + "WithHtml", resizeScriptEl.html().trim());

        // second element should contain references for contextParam and initCallback
        // don't try to check the js here, just that the velocity variables exist
        Element initScriptEl = scriptElements.get(1);

        String initScripElCont = initScriptEl.html();

        Map<String, Integer> refs = SparkTestUtils.checkVelocityFragmentReferences(initScripElCont,
                Arrays.asList(iframeInjContextVelocityKey, iframeIdContextKey), true);

        Assert.assertTrue(refs.get(iframeInjContextVelocityKey) > 0);

        // should call iFrameResize on the iframe with correct id
        Assert.assertTrue(refs.get(iframeIdContextKey) > 0);

    }


    /**
     * Tests whether the value in template is a valid reference to a context key (eg. for key 'refKey'
     * value in template must be '$refKey' or '${refKey}'
     */
    private static void assertTemplateReferencesVelocityVariable(String variableKey, String templateValue) {
        Assert.assertThat(templateValue, anyOf(is("$" + variableKey), is("${" + variableKey + "}")));
    }

}
