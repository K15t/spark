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
        if (document.querySelector('style[data-theme]:not([data-custom-theme])')) {
            return;
        }

        // look for data-theme style tags on parent document and copy those in

        if (parentWindow && parentWindow.document) {
            // copy theming CSS from outer window
            parentWindow.document.querySelectorAll('style[data-theme]:not([data-custom-theme])')
                .forEach((themeStyle) => {
                    var theme = themeStyle.dataset.theme;

                    var iframeThemeStyle = document.createElement('style');
                    iframeThemeStyle.dataset.theme = theme;
                    iframeThemeStyle.textContent = themeStyle.textContent;

                    document.head.appendChild(iframeThemeStyle);
                })
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

    function initializeTheming() {
        copyThemeTokens();
        copyThemeAttributes();

        // if the user switches themes Confluence will send a message with the "theme.change" type in the payload
        windowEl.addEventListener("message", handleThemeChange);
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
