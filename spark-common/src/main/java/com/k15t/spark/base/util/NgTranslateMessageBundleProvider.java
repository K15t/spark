package com.k15t.spark.base.util;

import com.k15t.spark.base.MessageBundleProvider;
import com.k15t.spark.base.RequestProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Bundle provider to provide the message bundle and all properties as JSON representation for ng-translate
 * including the required placeholder conversion ({{0}} => {{_0}}).
 */
public class NgTranslateMessageBundleProvider implements MessageBundleProvider {

    private static final Logger logger = LoggerFactory.getLogger(NgTranslateMessageBundleProvider.class);

    private String msgBundleResourcePath;


    public NgTranslateMessageBundleProvider(String msgBundleResourcePath) {
        this.msgBundleResourcePath = msgBundleResourcePath;
    }


    @Override
    public boolean isMessageBundle(RequestProperties props) {

        if (msgBundleResourcePath != null) {
            return props.getLocalPath() != null && (this.msgBundleResourcePath).endsWith(props.getLocalPath());
        }

        return false;
    }


    @Override
    public String loadBundle(RequestProperties props) {

        ResourceBundle rb;
        Locale locale = props.getLocale();
        if (locale != null) {
            logger.debug("Use local {} to load bundle from {}", locale, msgBundleResourcePath);
            rb = ResourceBundle.getBundle(msgBundleResourcePath, locale, new ResourceBundle.Control() {
                @Override
                public Locale getFallbackLocale(String baseName, Locale locale) {
                    return Locale.ROOT;
                }
            });
        } else {
            logger.debug("Use local {} to load bundle from {}", Locale.getDefault(), msgBundleResourcePath);
            rb = ResourceBundle.getBundle(msgBundleResourcePath);
        }

        StringBuilder json = new StringBuilder("{");
        for (Iterator<String> iterator = rb.keySet().iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            String value = convertValue(rb.getString(key));
            json.append('"').append(escapeJson(key)).append("\":\"").append(escapeJson(value)).append('"');
            if (iterator.hasNext()) {
                json.append(',');
            }
        }
        json.append("}");

        return json.toString();
    }


    @Override
    public String getContentType() {
        return "application/json";
    }


    /**
     * Converts the value to work with ng-translate. Overwrite this method to enforce a custom transformation.
     */
    protected String convertValue(String value) {
        return value.replaceAll("\\{(\\d)\\}", "{{_$1}}").replaceAll("''", "'");
    }


    /**
     * See https://www.ietf.org/rfc/rfc4627.txt section '2.5. Strings'
     * <pre>
     * All Unicode characters may be placed within the
     * quotation marks except for the characters that must be escaped:
     * quotation mark, reverse solidus, and the control characters (U+0000
     * through U+001F).
     * </pre>
     */
    static String escapeJson(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder(input.length());

        for (int i = 0; i < input.length(); i += 1) {
            char c = input.charAt(i);

            if (c == '\\') {
                result.append("\\\\");
            } else if (c == '\"') {
                result.append("\\\"");
            } else if (c == '\n') {
                result.append("\\n");
            } else if (c <= 0x1F) {
                String hex = Integer.toHexString(c);
                result.append("\\u");
                result.append(StringUtils.leftPad(hex, 4, '0'));
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

}
