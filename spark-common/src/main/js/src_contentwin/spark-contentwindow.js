var SPARK = SPARK || {};

(function(sparkInstance, windowEl) {

    // windowEl.frameElement would be null if domains are not same but
    // same domain is prereq for SPARK usage
    var parentSpark = windowEl.frameElement.SPARK;

    var getData = function() {
        return parentSpark.extraData;
    };

    var getDialogControls = function() {
        // TODO rename iframeControls to dialogControls..?
        return parentSpark.iframeControls;
    };

    sparkInstance.getData = getData;
    sparkInstance.getDialogControls = getDialogControls;

})(SPARK, window);
