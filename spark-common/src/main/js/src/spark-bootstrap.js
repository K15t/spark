import iframeResizer from 'iframe-resizer/js/iframeResizer';

import css from './spark-common.scss';
import sparkTemplates from './spark-common-templates';

'use strict';

var getFullAppPath = function(appPath) {
    // append trailing slash if not there (before the query string if present)
    if (appPath != null) {
        var appPathParts = appPath.split('?');

        var appBasePath = appPathParts[0];

        appBasePath = (/\/$/.test(appBasePath) || /\.html$/.test(appBasePath) ? appBasePath : appBasePath + '/');

        return (AJS.contextPath() + appBasePath + (appPathParts.length > 1 ? '?' + appPathParts[1] : ''));
    }
    return appPath;
};

var getIFrameSourceQuery = function(queryString) {
    if (!queryString) {
        return '';
    }

    var queryStrToAppend = queryString;
    if (queryStrToAppend.indexOf('?') === 0 || queryStrToAppend.indexOf('&') === 0) {
        queryStrToAppend = queryStrToAppend.substr(1);
    }
    return '?' + queryStrToAppend;
};

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

        createOptions = AJS.$.extend(defaultDialogOptions, createOptions);
        var elementIdSparkAppContainer = angularAppName + '-spark-dialog-app-container';

        var dialog = createDialog(elementIdSparkAppContainer, sparkTemplates.appBootstrapContainerDialog2WithiFrame({
            id: elementIdSparkAppContainer,
            title: title,
            src: location.protocol + '//' + location.host + appPath,
            createOptions: createOptions,
            className: css.className
        }), createOptions.width, createOptions.height);

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
     * @param createOptions Advanced configuration for setting up the the dialog. Currently supported are:
     *        width width of the dialog or iframe
     *        height height of the dialog or iframe
     */
    this.loadApp = function(element, angularAppName, appPath, createOptions) {

        var fullAppPath = getFullAppPath(appPath);

        var elementIdSparkAppContainer = angularAppName + '-spark-app-container';
        var appContainerAlreadyCreated = AJS.$('#' + elementIdSparkAppContainer).length > 0;

        if (appContainerAlreadyCreated) {
            AJS.$('#' + elementIdSparkAppContainer).remove();
        }

        AJS.$(element).append(sparkTemplates.appBootstrapContaineriFrame({
            id: elementIdSparkAppContainer,
            src: location.protocol + '//' + location.host + fullAppPath,
            createOptions: AJS.$.extend(defaultDialogOptions, createOptions),
            className: css.className
        }));

        iframeResizer([{
            'autoResize': true,
            'heightCalculationMethod': 'max'
        }], AJS.$(element).find('iframe')[0]);
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
            dialog = createDialog(id, sparkTemplates.errorDialog2({
                id: id,
                title: 'An error happened ...',
                className: css.className
            }));
        } else {
            dialog = createDialog(id, sparkTemplates.errorDialog({
                title: 'An error happened ...',
                className: css.className
            }), 800, 500);
        }

        AJS.$('.aui-blanket').addClass('spark-loading');

        return dialog;
    };


    var createDialog = function(id, dialogMarkup, cssClass, width, height) {

        var dialog;

        if (AJS.dialog2) {
            AJS.$('body').append(dialogMarkup);
            dialog = AJS.dialog2('#' + id);
            dialog.$appEl = dialog.$el;
            dialog.$contentEl = AJS.$('.spark-app-content', dialog.$appEl);
        } else {
            dialog = new AJS.Dialog({
                width: width,
                height: height,
                id: id
            });
            dialog.$appEl = dialog.popup.element;
            dialog.$appEl.html(dialogMarkup);
            dialog.$contentEl = AJS.$('.spark-app-content', dialog.$appEl);
            dialog.$contentEl.height(dialog.$appEl.height() - 105);
        }

        return dialog;
    };
}

var initIframeAppLoader = function(iframeResizer) {

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

    var closeSparkApp = function(appContainerId, data, callback) {
        var iFrame = AJS.$('#' + appContainerId + '-iframe-container').find('iframe').get()[0];

        // First close the iFrame
        if (iFrame.iFrameResizer) {
            iFrame.iFrameResizer.close();
        }

        // Then remove the iFrame container
        AJS.$('#' + appContainerId).remove();
        if (callback) {
            callback(data);
        }
    };

    function getMaxHeight() {
        return window.innerHeight;
    }

    var createIframe = function(appId, appPath, options) {

        // Parameter validations
        if (!appId) {
            throw new Error('Parameter missing - \'appId\'');
        } else if (!appPath) {
            throw new Error('Parameter missing - \'appPath\'');
        } else if (!options) {
            throw new Error('Parameter missing - \'options\'');
        }

        var fullAppPath = getFullAppPath(appPath);
        var iFrameId = appId + '-iframe';

        var iframeSrcQuery = getIFrameSourceQuery(options.queryString);

        var iFrameElement = AJS.$(sparkTemplates.bootstrappedIFrame({
            'id': iFrameId,
            'src': location.protocol + '//' + location.host + fullAppPath + iframeSrcQuery,
            className: css.className
        }));

        var iFrameDomEl = iFrameElement.get()[0];

        // add contextdata to a path from which the SPARK counterpart injected into
        // the iframe's content can find it
        // the data is added as an extra field of the iframe DOM element, and it can
        // be accessed from the content document by window.frameElement (works
        // as long as the iframe and the host have same domain)
        // client code in the iframe should always use SPARK.getContextData() etc to access
        // this data (and not rely on the current internal implementation)

        var iFrameSparkContext = {};
        iFrameDomEl.SPARK = iFrameSparkContext;

        iFrameSparkContext.contextData = options.contextData;

        iFrameSparkContext.setIFrameContainerWidth = function(width) {
            // Width of the iFrame is set automatically based on its content
            // The idea is to resize the iFrame's parent container
            AJS.$(iFrameElement).parent().width(width);
        };

        if (options.customContext) {
            iFrameSparkContext.customContext = options.customContext;
        }

        // Setup iFrame Resizer
        var resizerSettings = {
            'autoResize': true,
            'heightCalculationMethod': 'max',
            'maxHeight': getMaxHeight()
        };

        if (options.iFrameResizerSettings) {
            resizerSettings = AJS.$.extend(resizerSettings, options.iFrameResizerSettings);
        }

        iframeResizer(resizerSettings, iFrameDomEl);

        return { iFrameElement, iFrameSparkContext };

    }

    var openFullscreenIframeDialog = function(appName, appPath, options) {

        // Parameter validations
        if (!appName) {
            throw new Error('Parameter missing - \'appName\'');
        } else if (!appPath) {
            throw new Error('Parameter missing - \'appPath\'');
        }

        var bodyEl = AJS.$('body');

        // Remove scrollers from content below the iFrame dialog
        bodyEl.addClass('spark-no-scroll');

        var appContainerId = appName + '-spark-app-container';

        // Remove the app container if it still exists on the page
        // Ideally, closing the dialog should remove the container
        AJS.$('#' + appContainerId).remove();

        var dialogSettings = AJS.$.extend({ 'addChrome': false }, options);

        var sparkAppContainerElement = AJS.$(sparkTemplates.appFullscreenContaineriFrame({
            id: appContainerId,
            createOptions: dialogSettings,
            className: css.className
        }));

        var closeFullScreenDialog = function(resultData) {
            // This is specific to full screen dialog
            bodyEl.removeClass('spark-no-scroll');
            closeSparkApp(appContainerId, resultData, dialogSettings.onClose);
        };

        // Add an easy way for the contained iFrame to access the dialog chrome (if added)
        var dialogChrome = null;
        if (dialogSettings.addChrome) {
            dialogChrome = {
                'cancelBtn': sparkAppContainerElement.find('#' + appContainerId + '-chrome-cancel').get()[0],
                'confirmBtn': sparkAppContainerElement.find('#' + appContainerId + '-chrome-submit').get()[0]
            };

            // Handle closing of dialog internally if parameter is provided
            if (dialogSettings.setDefaultCloseBehaviour) {
                dialogChrome.cancelBtn.addEventListener('click', closeFullScreenDialog);
                dialogChrome.confirmBtn.addEventListener('click', closeFullScreenDialog);
            }
        }

        // Setup the iFrame

        var iFrameResizerSettings = {
            'scrolling': 'auto',
            resizedCallback: function(data) {
                // Need to re-set maxHeight when window is resized
                // This is called when the iFrame is resized
                data.iframe.style.maxHeight = options.addChrome ? getMaxHeight() - 51 + 'px' : getMaxHeight() + 'px';
            }
        };

        var iFrameSettings = AJS.$.extend({ 'iFrameResizerSettings': iFrameResizerSettings }, options);

        var { iFrameElement, iFrameSparkContext } = createIframe(appContainerId, appPath, iFrameSettings);

        // Add the iFrame to the spark full screen dialog app container
        sparkAppContainerElement.find('#' + appContainerId + '-iframe-container').append(iFrameElement);

        // Add dialog specific context data
        iFrameSparkContext.dialogControls = {
            'closeDialog': closeFullScreenDialog,
            'dialogChrome': dialogChrome
        };


        // Add the spark full screen dialog app to the body
        sparkAppContainerElement.appendTo(bodyEl);

        // TODO Return the iFrameDomElement for consistency and ease of use
        return appContainerId;
    };

    /**
     *
     * @param appName Name of the application to bootstrap
     * @param appPath application path which will be used to load the necessary angular resources
     * @param dialogOptions see defaultInlineOptions (for more details - https://docs.atlassian.com/aui/7.6.3/docs/inline-dialog.html)
     * @param startedCallback Callback which will be called after the application was successfully started
     */

    var openInlineIframeDialog = function(appName, appPath, options) {

        // Parameter validations
        if (!appName) {
            throw new Error('Parameter missing - \'appName\'');
        } else if (!appPath) {
            throw new Error('Parameter missing - \'appPath\'');
        }

        var bodyEl = AJS.$('body');

        //Setup the Dialog
        var appContainerId = appName + '-spark-app-container';

        var dialogSettings = AJS.$.extend({
            width: '540px',
            triggerText: 'Inline trigger',
            alignment: 'bottom left'
        }, options);

        // Remove the app container if it still exists on the page
        // Ideally, closing the dialog should remove the container
        AJS.$('#' + appContainerId).remove();

        // Init the byline dialog trigger element and bind it to the body later
        var triggerEl = sparkTemplates.inlineDialogTrigger({
            targetId: appContainerId,
            text: dialogSettings.triggerText
        });

        // Init the spark inline dialog app container, and bind it to the body later
        var dialogEl = AJS.$(sparkTemplates.inlineDialogAppContainer({
            id: appContainerId,
            createOptions: dialogSettings,
            className: css.className
        }));

        // Setup the iFrame
        var { iFrameElement } = createIframe(appContainerId, appPath, options);

        // Add the iFrame to the Dialog container
        dialogEl.find('#' + appContainerId + '-iframe-container').append(iFrameElement);

        AJS.$(bodyEl).append(dialogEl);

        return {
            triggerEl,
            iFrameDomEl: AJS.$(iFrameElement).get()[0]
        };
    };

    return {
        'openFullscreenIframeDialog': openFullscreenIframeDialog,
        'openInlineIframeDialog': openInlineIframeDialog,
        'createIframe': createIframe
    };

};

export default {
    appLoader: new AppLoader(),
    iframeAppLoader: initIframeAppLoader(iframeResizer),
    // the init is exposed here too so that tests can initialize with mocked iFrameResizer
    initIframeAppLoader: initIframeAppLoader
};
