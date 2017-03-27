var SPARK = SPARK || {};

(function(sparkInstance, windowEl) {

    // windowEl.frameElement would be null if domains are not same but
    // same domain is prereq for SPARK usage
    var parentSpark = windowEl.frameElement.SPARK;

    var getContextData = function() {
        return parentSpark.contextData;
    };

    var getDialogControls = function() {
        // TODO rename dialogControls to dialogControls..?
        return parentSpark.dialogControls;
    };

    sparkInstance.getContextData = getContextData;
    sparkInstance.getDialogControls = getDialogControls;

})(SPARK, window);
