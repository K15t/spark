import 'simple-xdm/plugin';

window.SPARK = window.SPARK || {};

(function(sparkInstance, windowEl) {

    // windowEl.frameElement would be null if domains are not same but
    // same domain is prereq for SPARK usage
    var parentSpark = windowEl.frameElement.SPARK;

    var getContextData = function() {
        return parentSpark.contextData;
    };

    var getDialogControls = function() {
        return parentSpark.dialogControls;
    };

    sparkInstance.getContextData = getContextData;
    sparkInstance.getDialogControls = getDialogControls;

})(window.SPARK, window);
