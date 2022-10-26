import 'iframe-resizer/js/iframeResizer.contentWindow';

window.SPARK = window.SPARK || {};

(function(sparkInstance, windowEl) {

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
    sparkInstance.setContainerWidth = parentSpark.setContainerWidth;

})(window.SPARK, window);
