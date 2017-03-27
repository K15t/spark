package com.k15t.spark.base.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;


public class DocumentOutputUtilTest {

    private static final String iframeIdContextKey = "iframeId";
    private static final String iframeSrcContextKey = "iframeSrc";
    private static final String iframeResizerJsContextKey = "iframeResizerJs";
    private static final String iframeInjContextVelocityKey = "escapedIframeContext";
    private static final String iframeInitCallbackVelocityKey = "iframeInitCallback";


    @Test
    public void generateAdminIframeTemplateContext() throws IOException {

        // simple case

        Map<String, Object> res =
                DocumentOutputUtil.generateAdminIframeTemplateContext("/test/base/url/",
                        "iframe-id", "{\"test-key\": \"test-value\"}",
                        null, "");

        Assert.assertEquals("iframe-id", res.get(iframeIdContextKey));

        Assert.assertEquals("/test/base/url/?iframe_content=true", res.get(iframeSrcContextKey));

        Assert.assertEquals("/* test mockup of iframeResizer.min.js */", res.get(iframeResizerJsContextKey));

        Assert.assertEquals("{\\\"test-key\\\": \\\"test-value\\\"}", res.get(iframeInjContextVelocityKey));

        Assert.assertEquals(null, res.get(iframeInitCallbackVelocityKey));

        // another try, main difference: invalid initCallback function name (should not be added to context)

        res = DocumentOutputUtil.generateAdminIframeTemplateContext("/test2/",
                "id_of_second_iframe", "context information string '+!&",
                "angular.initialized", "?space=32");

        Assert.assertEquals("id_of_second_iframe", res.get(iframeIdContextKey));

        Assert.assertEquals("/test2/?space=32&iframe_content=true", res.get(iframeSrcContextKey));

        Assert.assertEquals("/* test mockup of iframeResizer.min.js */", res.get(iframeResizerJsContextKey));

        Assert.assertEquals("context information string '+!&", res.get(iframeInjContextVelocityKey));

        Assert.assertEquals(null, res.get(iframeInitCallbackVelocityKey));

        // one more time, this time also the init callback should be added as expected

        res = DocumentOutputUtil.generateAdminIframeTemplateContext("/test/3/", "id3",
                "{'context': {'pages': ['test1', 'test2'], 'result': {'success': false, 'code': 404}}}",
                "sparkInitialized", "test_value=false&admin=true");

        Assert.assertEquals("id3", res.get(iframeIdContextKey));

        Assert.assertEquals("/test/3/?iframe_content=true&test_value=false&admin=true", res.get(iframeSrcContextKey));

        Assert.assertEquals("/* test mockup of iframeResizer.min.js */", res.get(iframeResizerJsContextKey));

        Assert.assertEquals(
                "{'context': {'pages': ['test1', 'test2'], 'result': {'success': false, 'code': 404}}}",
                res.get(iframeInjContextVelocityKey));

        Assert.assertEquals("sparkInitialized", res.get(iframeInitCallbackVelocityKey));

    }


    @Test
    public void getIframeContentWindowJs() throws IOException {

        String iframeContWinJs = DocumentOutputUtil.getIframeResizeContentWindowJs();

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

        Map<String, Integer> refs = checkVelocityFragmentReferences(initScripElCont,
                Arrays.asList(iframeInjContextVelocityKey, iframeInitCallbackVelocityKey, iframeIdContextKey), true);

        Assert.assertTrue(refs.get(iframeInjContextVelocityKey) > 0);
        //Assert.assertTrue(refs.get(iframeInitCallbackVelocityKey) > 0);
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


    /**
     * Extracts the variable references from velocity template fragment ($variable or ${variable}) and
     * counts how many times each of the variable names in the expRefs list is present in the fragment.
     *
     * @param velocityFragment fragment from which to extract variables
     * @param expRefs list of variable names to count (that are expected to be used in the fragment)
     * @param failOnUnExpectedRef if true, calls Assert.fail() when encountering a variable name not in the 'expRefs' list
     * @return map from variable name to int, from variable names in 'expRefs' list to time used in the fragment
     */
    private static Map<String, Integer> checkVelocityFragmentReferences(
            String velocityFragment, List<String> expRefs, boolean failOnUnExpectedRef) {

        Map<String, Integer> refCount = new HashMap<>();
        for (String expRef : expRefs) {
            refCount.put(expRef, 0);
        }

        Pattern velocityLongKeyPattern = Pattern.compile("\\$\\{(\\w+)\\}");
        Matcher velocityLongKeys = velocityLongKeyPattern.matcher(velocityFragment);

        Pattern velocityShortKeyPattern = Pattern.compile("\\$(\\w+)");
        Matcher velocityShortKeys = velocityShortKeyPattern.matcher(velocityFragment);

        List<String> foundRefs = new LinkedList<>();

        while (velocityLongKeys.find()) {
            foundRefs.add(velocityLongKeys.group(1));
        }
        while (velocityShortKeys.find()) {
            foundRefs.add(velocityShortKeys.group(1));
        }

        for (String refKey : foundRefs) {

            Integer currCount = refCount.get(refKey);

            if (currCount == null) { // unexpected refKey
                if (failOnUnExpectedRef) {
                    Assert.fail("Found velocity variable referencing key <" + refKey + "> that was not " +
                            "in the list of expected keys (" + expRefs + ")");
                }
            } else {
                refCount.put(refKey, currCount + 1);
            }

        }

        return refCount;
    }

}
