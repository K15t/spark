/*
 * Add a mock implementation of the parts of AJS that are needed for the
 *
 * AJS.toInit() function needs to be available as a global when the spark scripts are run because the scripts
 * try to register to the AJS init queue.
 *
 * Other AJS (etc.) dependencies are used only once the SPARK functions are actually called, so they can
 * also be mocked by the test case code (and should be in most cases). For convenience some default implementations
 * are provided here so that a test case don't need to do lots of mocking of functionality that is irrelevant
 * to verify for given test (but required to be defined).
 */

var AJS = AJS || {}; // add a AJS mock as global and needed functions
var $ = $ || {};

AJS.$ = $;

(function() {

    var testController = {};

    AJS.testControl = testController;

    testController.toInitList = [];

    AJS.toInit = function(toInit) {
        testController.toInitList.push(toInit);

        return this;
    };

    testController.contextPath = "test/context/path";
    AJS.contextPath = function() {
        return testController.contextPath;
    };

    var initRun = false;
    testController.initOnce = function() {

        if (!initRun) {
            initRun = true;
            var i = 0;
            for (i = 0; i < testController.toInitList.length; i++) {
                testController.toInitList[i].call();
            }
        }

    };

})();