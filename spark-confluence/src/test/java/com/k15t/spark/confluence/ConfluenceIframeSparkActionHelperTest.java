package com.k15t.spark.confluence;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.k15t.spark.confluence.ConfluenceIframeSparkActionHelper.splitWebResourceKeys;
import static org.junit.Assert.assertEquals;


public class ConfluenceIframeSparkActionHelperTest {

    @Test
    public void splitWebResourceKeys_Simple() {
        List<String> expected = Arrays.asList("com.k15t.test.sparkplugin:important-resource.js");
        assertEquals(expected, splitWebResourceKeys("com.k15t.test.sparkplugin:important-resource.js"));
        assertEquals(expected, splitWebResourceKeys("com.k15t.test.sparkplugin:important-resource.js,"));
        assertEquals(expected, splitWebResourceKeys("com.k15t.test.sparkplugin:important-resource.js ,"));
        assertEquals(expected, splitWebResourceKeys("com.k15t.test.sparkplugin:important-resource.js , ,,"));
    }


    @Test
    public void splitWebResourceKeys_Empty() {
        assertEquals(0, splitWebResourceKeys(null).size());
        assertEquals(0, splitWebResourceKeys("").size());
        assertEquals(0, splitWebResourceKeys(" ").size());
        assertEquals(0, splitWebResourceKeys(",").size());
        assertEquals(0, splitWebResourceKeys(" , ").size());
        assertEquals(0, splitWebResourceKeys(" , ,,").size());
    }


    @Test
    public void splitWebResourceKeys_MultipleCompleteModuleKeys() {
        List<String> expected = Arrays.asList("com.k15t.test.sparkplugin:important-resource.js",
                "com.k15t.test.sparkplugin:less-important-resource.js",
                "com.k15t.test.sparkplugin:some-styling.css");
        String keyString = "com.k15t.test.sparkplugin:important-resource.js, ,,com.k15t.test.sparkplugin:less-important-resource.js,"
                + "com.k15t.test.sparkplugin:some-styling.css";
        assertEquals(expected, splitWebResourceKeys(keyString));
    }

}
