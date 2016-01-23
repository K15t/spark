package com.k15t.spark.base.util;

import com.k15t.spark.base.MessageBundleProvider;
import com.k15t.spark.base.RequestProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode i18Properties = mapper.createObjectNode();
        for (String key : rb.keySet()) {
            i18Properties.put(key, convertValue(rb.getString(key)));
        }

        return i18Properties.toString();
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

}
