import iframeResizer from 'iframe-resizer/js/iframeResizer';

import './spark-common.css';
import soyTemplates from './spark-common.soy';

'use strict';

function AppLoader() {

    var startedApps = {};

    var startedAppDialog;

    var defaultDialogOptions = {
        width: '1000px',
        height: '500px',
        label: {
            submit: 'Save',
            close: 'Close'
        }
    };

    /**
     * Open a dialog and bootstrap the application within a encapsulated iframe.
     *
     * @param title Title of the dialog
     * @param angularAppName Name of the angular application to bootstrap
     * @param appPath application path which will be used to load the necessary angular resources
     * @param callbackStarted Callback which will be called after the angular application was successfully started
     * @param createOptions see defaultDialogOptions
     */
    this.loadAppInDialog = function(title, angularAppName, appPath, createOptions, startedCallback) {

        if (startedAppDialog) {
            startedAppDialog.$el.remove();
            startedAppDialog = undefined;
        }

        createOptions = $.extend(defaultDialogOptions, createOptions);
        var elementIdSparkAppContainer = angularAppName + '-spark-dialog-app-container';

        var dialog = createDialog(elementIdSparkAppContainer, soyTemplates.appBootstrapContainerDialog2WithiFrame({
            id: elementIdSparkAppContainer,
            title: title,
            src: location.protocol + '//' + location.host + appPath,
            createOptions: createOptions
        }), createOptions.width, createOptions.height).content;

        var closeDialogButton = AJS.$('#closeDialogButton' + elementIdSparkAppContainer, dialog.$el);
        var submitDialogButton = AJS.$('#submitDialogButton' + elementIdSparkAppContainer, dialog.$el);
        var iFrameContent = AJS.$('#' + elementIdSparkAppContainer + '-iframe');

        closeDialogButton.click(function(e) {
            dialog.close();
        });

        dialog['close'] = function() {
            dialog.hide();
            iFrameContent.remove();
        };

        dialog['getButton'] = function(type) {
            if (type === 'submit') {
                return submitDialogButton;
            } else {
                return closeDialogButton;
            }
        };

        startedAppDialog = dialog;

        iframeResizer([{
            log: true,
            autoResize: true
        }], iFrameContent[0]);

        dialog.show();

        if (startedCallback) {
            startedCallback(dialog, iFrameContent);
        }
    };

    /**
     * Bootstraps an Angular application.
     *
     * @param element Dom element under which the angular application should be attached and bootstrapped
     * @param angularAppName Name of the angular application to bootstrap
     * @param appPath application path which will be used to load the necessary angular resources
     * @param callbackStarted Callback which will be called after the angular application was successfully started
     * @param createOptions Advanced configuration for setting up the the dialog. Currently supported are:
     *        openInIframe true in case to open the app in a iframe
     *        width width of the dialog or iframe
     *        height height of the dialog or iframe
     */
    this.loadApp = function(element, angularAppName, appPath, createOptions, callbackStarted) {

        // append trailing slash if not there.
        var fullAppPath = contextPath + (/\/$/.test(appPath) || /\.html$/.test(appPath) ? appPath : appPath + '/');

        var elementIdSparkAppContainer = angularAppName + '-spark-app-container';
        var appContainerAlreadyCreated = $('#' + elementIdSparkAppContainer).length > 0;

        if (appContainerAlreadyCreated) {
            $('#' + elementIdSparkAppContainer).remove();
        }

        if (createOptions !== undefined && createOptions.openInIframe) {

            $(element).append(soyTemplates.appBootstrapContaineriFrame({
                id: elementIdSparkAppContainer,
                src: location.protocol + '//' + location.host + fullAppPath,
                createOptions: $.extend(defaultDialogOptions, createOptions)
            }).content);

            iframeResizer([{
                'autoResize': true,
                'heightCalculationMethod': 'max'
            }], $(element).find('iframe')[0]);

            return;
        }

        $(element).append(soyTemplates.appBootstrapContainer({
            id: elementIdSparkAppContainer
        }).content);

        // We have to use an additional element (div#spark-dialog-app-wrapper),
        // because body doesn't work, because it is using the browsers .innerHtml
        // property, which doesn't work consistently across browsers and for
        // example in Chrome includes elements from head, too. The additional
        // element is inserted automatically by the AppServlet.
        // More info: https://api.jquery.com/load/#loading-page-fragments
        $('#' + elementIdSparkAppContainer, element).load(
            fullAppPath + ' div#spark-dialog-app-wrapper > *', function complete(response, status, xhr) {

                if (status == "error") {
                    var dialog = createErrorDialog(angularAppName);
                    var absoluteAppPath = location.protocol + '//' + location.host + fullAppPath;
                    dialog.$titleEl.html('Error');
                    dialog.$contentEl.html(
                        '<h2>Could not load dialog app from \'' + fullAppPath + '\'</h2>' +
                        '<p>Have you created a servlet module for the DialogAppServlet in <code>atlassian-plugin.xml</code>?</p>' +
                        '<p>It should be available at <a href="' + fullAppPath + '">' + absoluteAppPath + '</a></p>'
                    );
                    dialog.show();

                    AJS.$('#closeErrorDialogButton', dialog.$el).click(function(e) {
                        e.preventDefault();
                        dialog.hide();
                    });

                } else {

                    var angular = (window.angular = {});

                    if (startedApps[angularAppName]) {
                        delete startedApps[angularAppName];
                    }

                    // https://github.com/rgrove/lazyload
                    LazyLoad.js(getExtractScriptsFromElement(element), function() {
                        angular = window.angular;
                        startedApps[angularAppName] = angular.bootstrap($('#' + angularAppName, element), [angularAppName]);
                        if (callbackStarted) {
                            callbackStarted(angular);
                        }
                    });

                }
            });
    };

    /**
     * Gets the bootstrapped angular application
     *
     * @param angularAppName Name of the angular application
     * @returns {*} Angular application or undefined if there is no application registered with that name
     */
    this.getApp = function(angularAppName) {
        return startedApps[angularAppName];
    };


    this.getAppDialog = function() {
        return startedAppDialog;
    };


    var createErrorDialog = function(id) {
        var dialog;

        if (AJS.dialog2) {
            dialog = createDialog(id, soyTemplates.errorDialog2({
                id: id,
                title: 'An error happened ...'
            }).content);
        } else {
            dialog = createDialog(id, soyTemplates.errorDialog({
                title: 'An error happened ...'
            }).content, 800, 500);
        }

        $('.aui-blanket').addClass('spark-loading');

        return dialog;
    };


    var createDialog = function(id, dialogMarkup, cssClass, width, height) {

        var dialog;

        if (AJS.dialog2) {
            $('body').append(dialogMarkup);
            dialog = AJS.dialog2('#' + id);
            dialog.$appEl = dialog.$el;
            dialog.$contentEl = $('.spark-app-content', dialog.$appEl);
        } else {
            dialog = new AJS.Dialog({
                width: width,
                height: height,
                id: id
            });
            dialog.$appEl = dialog.popup.element;
            dialog.$appEl.html(dialogMarkup);
            dialog.$contentEl = $('.spark-app-content', dialog.$appEl);
            dialog.$contentEl.height(dialog.$appEl.height() - 105);
        }

        return dialog;
    };


    var getExtractScriptsFromElement = function($contentEl) {

        var scripts = [];

        $('meta[name=script]', $contentEl).each(function() {
            scripts.push($(this).attr('content'));
            $(this).remove();
        });

        return scripts;
    };
}

var initIframeAppLoader = function() {

    /**
     * Creates a fullscreen iframe that will load the js app in given path.
     *
     * Simulates (quite loosely) how fullscreen dialog with an iframe
     * in Atlassian Connect would work.
     *
     * A chrome bar with close/submit buttons can be added to the dialog by specifying
     * 'addChrome': true in the 'dialogOptions' object.
     *
     * A JS-object containing controls for interacting with the parent window (eg. closing
     * the iframe dialog) will be available in the iframe context using SPARK.getDialogControls()
     *
     * The SPARK.dialogControls will contain method for closing the dialog 'closeDialog',
     * and 'dialogChrome' object for controlling possible dialog toolbar. If there is
     * no toolbar 'dialogChrome' is null, otherwise it will contain references to the buttons
     * in the dialog chrome ('cancelBtn' and 'confirmBtn').
     *
     * It is possible to pass custom context data to the context of the loaded iframe
     * by setting an object to 'dialogOptions.contextData'. A reference to this object
     * will be available in the iframe's context using SPARK.getContextData()
     *
     * @param appName name of the app (used as prefix for eg. element ids)
     * @param appPath relative path from which the iframe content is to be loaded
     * @param dialogOptions optional extra parameters for dialog creation
     */
    var openFullscreenIframeDialog = function(appName, appPath, dialogOptions) {

        var bodyEl = $('body');

        // to remove scrollers from content below the iframe dialog
        bodyEl.addClass('spark-no-scroll');

        var fullAppPath = AJS.contextPath() + appPath;

        var elementIdSparkAppContainer = appName + '-spark-app-container';

        var dialogSettings = $.extend({ 'addChrome': false }, dialogOptions);

        // make sure that element with the id is not already there
        // (in normal operation it is removed on dialog close)
        $('#' + elementIdSparkAppContainer).remove();

        var iframeSrcQuery = '';
        if (dialogSettings.queryString) {
            var queryStrToAppend = dialogSettings.queryString;
            if (queryStrToAppend.indexOf('?') === 0 || queryStrToAppend.indexOf('&') === 0) {
                queryStrToAppend = queryStrToAppend.substr(1);
            }
            iframeSrcQuery += '?' + queryStrToAppend;
        }

        // init a fullscreen dialog wrapper and iframe (and add it to body later)
        var iframeWrapperElement = $(soyTemplates.appFullscreenContaineriFrame({
            'id': elementIdSparkAppContainer,
            'src': location.protocol + '//' + location.host + fullAppPath + iframeSrcQuery,
            'createOptions': dialogSettings
        }).content);

        // add an easy way for the contained iframe to access the dialog chrome (if added)
        var dialogChrome = null;
        if (dialogSettings.addChrome) {
            var cancelBtnDomEl = iframeWrapperElement.find('#' + elementIdSparkAppContainer + '-chrome-cancel').get()[0];
            var confirmBtnDomEl = iframeWrapperElement.find('#' + elementIdSparkAppContainer + '-chrome-submit').get()[0];
            dialogChrome = {
                'cancelBtn': cancelBtnDomEl,
                'confirmBtn': confirmBtnDomEl
            };
        }

        var iframeElement = iframeWrapperElement.find('iframe');
        var iframeDomEl = iframeElement.get()[0];

        var iframeCloser = function(resultData) {
            bodyEl.removeClass('spark-no-scroll');
            if (iframeDomEl.iFrameResizer) {
                iframeDomEl.iFrameResizer.close();
            }
            iframeWrapperElement.remove();
            if (dialogSettings.onClose) {
                dialogSettings.onClose(resultData);
            }
        };

        // add contextdata to a path from which the SPARK counterpart injected into
        // the iframe's content can find it
        // the data is added as an extra field of the iframe DOM element, and it can
        // be accessed from the content document by window.frameElement (works
        // as long as the iframe and the host have same domain)
        // client code in the iframe should always use SPARK.getContextData() etc to access
        // this data (and not rely on the current internal implementation)

        var sparkIframeContext = {};
        iframeDomEl.SPARK = sparkIframeContext;

        sparkIframeContext.dialogControls = {
            'closeDialog': iframeCloser,
            'dialogChrome': dialogChrome
        };

        sparkIframeContext.contextData = dialogSettings.contextData;

        iframeResizer([{
            'autoResize': true,
            'heightCalculationMethod': 'max'
        }], iframeDomEl);

        iframeWrapperElement.appendTo(bodyEl);

        return elementIdSparkAppContainer;

    };

    return {
        'openFullscreenIframeDialog': openFullscreenIframeDialog
    };

};


export default {
    appLoader: new AppLoader(),
    iframeAppLoader: initIframeAppLoader()
};
