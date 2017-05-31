package com.k15t.spark.base.util;

import org.junit.Test;

import static com.k15t.spark.base.util.UrlUtil.rebaseUrl;
import static org.junit.Assert.assertEquals;


public class UrlUtilTest {

    @Test
    public void rebaseUrlTest() throws Exception {
        assertEquals("https://dummy:123/wiki", rebaseUrl("https://dummy:123/wiki", ""));
        assertEquals("https://dummy:123/wiki/", rebaseUrl("https://dummy:123/wiki/", ""));
        assertEquals("https://dummy:123/wiki/abc", rebaseUrl("https://dummy:123/wiki", "abc"));
        assertEquals("https://dummy:123/wiki/abc", rebaseUrl("https://dummy:123/wiki/", "abc"));
        assertEquals("https://dummy:123/wiki/abc/def", rebaseUrl("https://dummy:123/wiki", "abc/def"));
        assertEquals("https://dummy:123/wiki/abc/def", rebaseUrl("https://dummy:123/wiki/", "abc/def"));

        assertEquals("https://dummy:123", rebaseUrl("https://dummy:123", ""));
        assertEquals("https://dummy:123/", rebaseUrl("https://dummy:123/", ""));
        assertEquals("https://dummy:123/abc", rebaseUrl("https://dummy:123", "abc"));
        assertEquals("https://dummy:123/abc", rebaseUrl("https://dummy:123/", "abc"));
        assertEquals("https://dummy:123/abc/def", rebaseUrl("https://dummy:123", "abc/def"));
        assertEquals("https://dummy:123/abc/def", rebaseUrl("https://dummy:123/", "abc/def"));
    }

}