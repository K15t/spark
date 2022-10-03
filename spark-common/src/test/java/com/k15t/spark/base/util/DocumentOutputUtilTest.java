package com.k15t.spark.base.util;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class DocumentOutputUtilTest {

    @Test
    public void escapeJavascriptForScriptTag_TestEscaping() {
        assertEquals("\\x3C!--", DocumentOutputUtil.escapeJavascriptForScriptTag("<!--"));

        assertEquals("\\x3Cscript", DocumentOutputUtil.escapeJavascriptForScriptTag("<script"));
        assertEquals("\\x3Cscript", DocumentOutputUtil.escapeJavascriptForScriptTag("<SCRIPT"));
        assertEquals("\\x3Cscript", DocumentOutputUtil.escapeJavascriptForScriptTag("<sCrIpT"));

        assertEquals("\\x3C/script", DocumentOutputUtil.escapeJavascriptForScriptTag("</script"));
        assertEquals("\\x3C/script", DocumentOutputUtil.escapeJavascriptForScriptTag("</SCRIPT"));
        assertEquals("\\x3C/script", DocumentOutputUtil.escapeJavascriptForScriptTag("</sCrIpT"));
    }


    @Test
    public void getIframeContentWindowJs_TestEscaping() {
        String js = DocumentOutputUtil.getIframeContentWindowJs();

        assertTrue(js.contains("/*\\x3C!-- test placeholder of spark-dist.contentWindow.js -->*/"));
    }


    @Test
    public void renderSparkIframeBody_TestEscaping() {
        String body = DocumentOutputUtil.renderSparkIframeBody("/path", "?one=test&two=2", "abc\"><img src=x onerror=prompt(1)>", "id-");

        assertTrue(Pattern.compile("id-[0-9]+-wrapper").matcher(body).find());
        assertTrue(body.contains("data-iframe-src=\"/path?one=test&amp;two=2\""));
        assertTrue(body.contains("data-iframe-context=\"abc&quot;&gt;&lt;img src=x onerror=prompt(1)&gt;\""));
        assertTrue(body.contains("/*\\x3C!-- test mockup of spark-dist.js -->*/"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void renderSparkIframeBody_InvalidPrefix() {
        DocumentOutputUtil.renderSparkIframeBody("/", null, null, "abc\"><img src=x onerror=prompt(1)>");
    }

}
