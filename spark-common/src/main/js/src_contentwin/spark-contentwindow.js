import 'iframe-resizer/js/iframeResizer.contentWindow';

window.SPARK = window.SPARK || {};

(function(sparkInstance, windowEl) {
    var parentWindow = windowEl.parent;

    /*
     * Copy in the style elements from the parent pages head into this one.
     * This only makes the design tokens available in this frame but does not enable theming (i.e. dark mode) yet.
     */
    function copyThemeTokens() {
        // copy style tags into our app iframe if not already present
        if (document.querySelector('style[data-theme]:not([data-custom-theme]),link[data-theme]')) {
            return;
        }

        // look for data-theme style tags on parent document and copy those in
        if (parentWindow && parentWindow.document) {
            var inlineThemeTags = [...parentWindow.document.querySelectorAll('style[data-theme]:not([data-custom-theme])')];
            var inlineThemeTokens = inlineThemeTags
                .filter(tag => tag.textContent.trim().length > 0);

            if (inlineThemeTokens.length && inlineThemeTokens.length === inlineThemeTags.length) {
                // copy theming CSS from outer window
                inlineThemeTokens.forEach((themeStyle) => {
                    var theme = themeStyle.dataset.theme;

                    var iframeThemeStyle = document.createElement('style');
                    iframeThemeStyle.dataset.theme = theme;
                    iframeThemeStyle.textContent = themeStyle.textContent;

                    document.head.appendChild(iframeThemeStyle);
                })
            } else {
                var contextPathTag = parentWindow.document.querySelector('meta[name="ajs-context-path"]');
                if (contextPathTag) {
                    var contextPath = contextPathTag.content || '';

                    var stylesheet = document.createElement('link');
                    stylesheet.rel = 'stylesheet';
                    stylesheet.href = contextPath
                        + '/s/_/_/download/resources'
                        + '/com.atlassian.auiplugin:split_aui.page.design-tokens-base-themes-css'
                        + '/aui.page.design-tokens-base-themes-css.css';
                    stylesheet.dataset.theme = 'all';

                    document.head.appendChild(stylesheet);
                }

            }
        }
    }


    /*
     * Copy the data attributes from the parent pages HTML element to this one.
     * Setting those effectively enables theming (as long as the required token styles are available in this frame).
     * See https://developer.atlassian.com/server/framework/atlassian-theme/html/ for the meaning of those data attributes.
     */
    function copyThemeAttributes() {
        if (parentWindow && parentWindow.document) {
            var parentHtml = parentWindow.document.documentElement;

            if (parentHtml) {
                var parentData = parentHtml.dataset;
                setThemeAttributes(parentData.colorMode, parentData.theme);
            }
        }
    }

    function setThemeAttributes(colorMode, themeKeys) {
        var thisHtml = document.documentElement;
        thisHtml.dataset.colorMode = colorMode;
        thisHtml.dataset.theme = themeKeys;

        // also set color-scheme meta tag so it matches the outer documents scheme which will enable transparent background iframes
        var colorSchemeTag = document.querySelector('meta[name="color-scheme"]');
        if (!colorSchemeTag) {
            colorSchemeTag = document.createElement('meta');
            colorSchemeTag.name = 'color-scheme';
            document.head.appendChild(colorSchemeTag);
        }
        colorSchemeTag.content = colorMode;
    }

    function handleThemeChange(event) {
        try {
            const { type, colorMode, darkThemeKey, lightThemeKey } = JSON.parse(event.data);
            if (type === "theme.change") {
                // parent pages theme attributes are not yet updated, construct attributes from event payload instead
                var themeKeys = 'dark:' + darkThemeKey + ' light:' + lightThemeKey;
                // colorMode is uppercase and in the case of 'AUTO' needs to be resolved manually
                var colorModeResolved = (colorMode === 'AUTO') ? getPreferredTheme() : colorMode.toLowerCase();

                setThemeAttributes(colorModeResolved, themeKeys);
            }
        } catch (e) { /* ignore, usually due to event.data not being a parseable JSON string */ }
    }

    /*
     * Returns the users preferred color schema, falls back to light if that is not possible
     */
    function getPreferredTheme() {
        var isMatchMediaAvailable = 'matchMedia' in windowEl;
        if (isMatchMediaAvailable) {
            var darkModeMediaQuery = '(prefers-color-scheme: dark)';
            var darkModeMql = isMatchMediaAvailable && windowEl.matchMedia(darkModeMediaQuery);
            return darkModeMql.matches ? 'dark' : 'light';
        }
        return 'light';
    }

    /*
     * Check for availability of theming in the host product, to avoid errors when requesting tokens in old versions without theming.
     */
    function isThemingAvailable() {
        if (parentWindow && parentWindow.document && parentWindow.document.documentElement) {
            const data = parentWindow.document.documentElement.dataset;
            return !!(data.theme && data.colorMode);
        }
        return false;
    }

    function initializeTheming() {
        if (isThemingAvailable()) {
            copyThemeTokens();
            copyThemeAttributes();

            // if the user switches themes the host app will send a message with the "theme.change" type in the payload
            windowEl.addEventListener("message", handleThemeChange);
        }
    }

    // windowEl.frameElement would be null if domains are not same but
    // same domain is prereq for SPARK usage
    var parentSpark = windowEl.frameElement.SPARK || {};

    var getContextData = function() {
        return parentSpark.contextData;
    };

    var getDialogControls = function() {
        return parentSpark.dialogControls;
    };

    var getCustomContext = function() {
        return parentSpark.customContext;
    };


    sparkInstance.getContextData = getContextData;
    sparkInstance.getDialogControls = getDialogControls;
    sparkInstance.getCustomContext = getCustomContext;
    sparkInstance.initializeTheming = initializeTheming;
    sparkInstance.setContainerWidth = parentSpark.setContainerWidth;

})(window.SPARK, window);
