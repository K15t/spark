/*
 * Mock some parts of the AJS etc that are needed for running the SPARK test cases
 *
 * Could be done also in the test cases but some default mocks are added here for convenience
 */

var AJS = AJS || {}; // add a AJS mock as global and needed functions
var $ = $ || {};

var require = function(module) {
    if (module === 'ajs') {
        return AJS;
    } else if (module === 'jquery') {
        return $;
    } else {
        throw new Error('unimplement module: ' + module);
    }
};

var SPARK = { 'mockupOldVersion': true };

(function() {

    var testController = {};

    AJS.testControl = testController;

    testController.oldSparkMockupVersion = SPARK;

    AJS.toInit = function(toInit) {
        throw "AJS.toInit should not be needed";
    };

    testController.contextPath = "/test/context/path";
    AJS.contextPath = function() {
        return testController.contextPath;
    };

})();
