import iframeResizer from 'iframe-resizer/js/iframeResizer';

import $ from 'jquery';
import AJS from 'ajs';

import css from './spark-common.scss';
import sparkTemplates from './spark-common-templates';

'use strict';

// noinspection JSUnresolvedVariable
const cssClassName = css.className;

const getFullAppPath = function(appPath) {
    // append trailing slash if not there (before the query string if present)
    const appPathParts = appPath.split('?');

    let appBasePath = appPathParts[0];

    appBasePath = (/\/$/.test(appBasePath) || /\.html$/.test(appBasePath) ? appBasePath : appBasePath + '/');

    return AJS.contextPath() + appBasePath + (appPathParts.length > 1 ? '?' + appPathParts[1] : '');
};

const getIFrameSourceQuery = function(queryString) {
    if (!queryString) {
        return '';
    }

    let queryStrToAppend = queryString;
    if (queryStrToAppend.indexOf('?') === 0 || queryStrToAppend.indexOf('&') === 0) {
        queryStrToAppend = queryStrToAppend.substring(1);
    }
    return '?' + queryStrToAppend;
};

const getIframeSource = function(appPath, queryString) {
    const fullAppPath = getFullAppPath(appPath);
    const iframeQueryString = getIFrameSourceQuery(queryString);

    return location.protocol + '//' + location.host + fullAppPath + iframeQueryString;
};

function AppLoader() {

    let startedAppDialog;

    const defaultDialogOptions = {
        width: '1000px',
        height: '500px',
        label: {
            submit: 'Save',
            close: 'Close'
        }
    };

    // noinspection JSUnusedGlobalSymbols
    /**
     * Open a dialog and bootstrap the application within a encapsulated iframe.
     *
     * @param title Title of the dialog
     * @param angularAppName Name of the angular application to bootstrap
     * @param appPath application path which will be used to load the necessary angular resources
     * @param startedCallback Callback which will be called after the angular application was successfully started
     * @param createOptions see defaultDialogOptions
     */
    this.loadAppInDialog = function(title, angularAppName, appPath, createOptions, startedCallback) {

        if (startedAppDialog) {
            startedAppDialog.$el.remove();
            startedAppDialog = undefined;
        }

        createOptions = $.extend(defaultDialogOptions, createOptions);
        const elementIdSparkAppContainer = angularAppName + '-spark-dialog-app-container';

        const dialog = createDialog(elementIdSparkAppContainer, sparkTemplates.appBootstrapContainerDialog2WithiFrame({
            id: elementIdSparkAppContainer,
            title: title,
            src: location.protocol + '//' + location.host + appPath,
            createOptions: createOptions,
            className: cssClassName
        }));

        const closeDialogButton = $('#closeDialogButton' + elementIdSparkAppContainer, dialog.$el);
        const submitDialogButton = $('#submitDialogButton' + elementIdSparkAppContainer, dialog.$el);
        const iFrameContent = $('#' + elementIdSparkAppContainer + '-iframe');

        closeDialogButton.click(function() {
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
     * Bootstraps an application.
     *
     * @param element Dom element under which the angular application should be attached and bootstrapped
     * @param appName Name of the application to bootstrap
     * @param appPath application path which will be used to load the necessary angular resources
     * @param createOptions Advanced configuration for setting up the the dialog. Currently supported are:
     *        width width of the dialog or iframe
     *        height height of the dialog or iframe
     */
    this.loadApp = function(element, appName, appPath, createOptions) {

        const fullAppPath = getFullAppPath(appPath);

        const elementIdSparkAppContainer = appName + '-spark-app-container';
        const appContainer = $('#' + elementIdSparkAppContainer);

        if (appContainer.length > 0) {
            appContainer.remove();
        }

        $(element).append(sparkTemplates.appBootstrapContaineriFrame({
            id: elementIdSparkAppContainer,
            src: location.protocol + '//' + location.host + fullAppPath,
            createOptions: $.extend(defaultDialogOptions, createOptions),
            className: cssClassName
        }));

        iframeResizer([{
            'autoResize': true,
            'heightCalculationMethod': 'max'
        }], $(element).find('iframe')[0]);
    };


    // noinspection JSUnusedGlobalSymbols
    this.getAppDialog = function() {
        return startedAppDialog;
    };


    const createDialog = function(id, dialogMarkup) {
        $('body').append(dialogMarkup);
        const dialog = AJS.dialog2('#' + id);
        dialog.$appEl = dialog.$el;
        dialog.$contentEl = $('.spark-app-content', dialog.$appEl);

        return dialog;
    };
}

const initIframeAppLoader = function(iframeResizer) {

    // helper function for generic actions to take when closing a spark app view
    const closeSparkApp = function(appContainerElement, callback, data) {
        const iframe = appContainerElement.find('iframe').get()[0];

        // this will cleanly remove iFrameResizer and call connected close listeners
        if (iframe.iFrameResizer) {
            iframe.iFrameResizer.close();
        }

        appContainerElement.remove();
        if (callback) {
            callback(data);
        }
    };

    // helper function that setups the required SPARK context and iFrameResizer for the iframe
    const loadSparkToIframe = function(iframeElement, options) {

        const iframeDomEl = iframeElement.get()[0];

        // add contextdata to a path from which the SPARK counterpart injected into
        // the iframe's content can find it
        // the data is added as an extra field of the iframe DOM element, and it can
        // be accessed from the content document by window.frameElement (works
        // as long as the iframe and the host have same domain)
        // client code in the iframe should always use SPARK.getContextData() etc to access
        // this data (and not rely on the current internal implementation)

        const iframeSparkContext = {};
        iframeDomEl.SPARK = iframeSparkContext;

        iframeSparkContext.contextData = options.contextData;

        iframeSparkContext.setContainerWidth = function(width) {
            // By default the width of the iframe is set to '100%' and its height grows as needed
            // Setting the size of the container element will thus also cause needed iframe size with new width to be set
            iframeElement.parent().width(width);
        };

        if (options.customContext) {
            iframeSparkContext.customContext = options.customContext;
        }

        // Setup iFrame Resizer
        let resizerSettings = {
            autoResize: true,
            heightCalculationMethod: 'max'
        };

        if (options.iframeResizerSettings) {
            resizerSettings = $.extend(resizerSettings, options.iframeResizerSettings);
        }

        iframeResizer(resizerSettings, iframeDomEl);

        return { iframeDomEl, iframeSparkContext };

    };

    /**
     * Creates an iframe (without any other wrapping) with the SPARK context setup
     *
     * Options:
     * {
     *      contextData: data that can be accessed inside the iframe with SPARK.getContextData(),
     *      customContext: context (eg functions for accessing add-on specific functionality), can be accessed in the iframe with
     *              SPAR.getCustomContext,
     *      iframeResizerSettings: settings that can for IFrameResizer - by default autoResize:
     *              true and heightCalculationoMethod: 'max' is set,
     *      queryString: the query part to be concatenated to the appPath
     *      containerEl: if specified, the created iframe will be appended to this element -
     *              if not specified the iframe won't be attached to the DOM
     * }
     *
     * @param appId name of the app (used as prefix for eg. element ids)
     * @param appPath relative path from which the iframe content is to be loaded
     * @param options optional extra parameters for setting up SPARK app
     * @returns an object with {
     *      triggerEl: element used for opening the inline dialog,
     *      iFrameDomEl: dom element of the iframe,
     *      iframeSparkContext: the JS object added to the iframe element that provides SPARK context to the app inside the iframe,
     *      appContainerId: id of the main inline dialog element
     *  }
     */
    const createAppIframe = function(appId, appPath, options) {

        if (!appId) {
            throw new Error('Parameter missing - \'appId\'');
        } else if (!appPath) {
            throw new Error('Parameter missing - \'appPath\'');
        } else if (!options) {
            throw new Error('Parameter missing - \'options\'');
        }

        const iframeId = appId + '-spark-iframe';

        const iframeSrc = getIframeSource(appPath, options.queryString);

        const iframeElement = $(sparkTemplates.bootstrappedIframe({
            id: iframeId,
            src: iframeSrc,
            className: cssClassName
        }));

        if (options.containerEl) {
            options.containerEl.append(iframeElement);
        }

        let { iframeDomEl, iframeSparkContext } = loadSparkToIframe(iframeElement, options);

        return {
            iframeSparkContext,
            iframeDomEl,
            appContainerId: iframeId
        };

    };

    /**
     * Creates a fullscreen iframe that will load the js app in given path.
     *
     * Simulates (quite loosely) how fullscreen dialog with an iframe
     * in Atlassian Connect would work.
     *
     * A chrome bar with close/submit buttons can be added to the dialog by specifying
     * 'addChrome': true in the 'options' object.
     *
     * A JS-object containing controls for interacting with the parent window (eg. closing
     * the iframe dialog) will be available in the iframe context using SPARK.getDialogControls()
     *
     * The SPARK.dialogControls will contain method for closing the dialog 'closeDialog',
     * and 'dialogChrome' object for controlling possible dialog toolbar. If there is
     * no toolbar 'dialogChrome' is null, otherwise it will contain references to the buttons
     * in the dialog chrome ('cancelBtn' and 'confirmBtn').
     *
     * If 'addChromeCloseHandlers': true is specified in the 'options' object, click handlers will be added to the close and
     * submit buttons of the dialog chrome, that will call closeDialog()
     *
     * Otherwise the 'options' object supports the same values as in createAppIframe
     *
     * @param appName name of the app (used as prefix for eg. element ids)
     * @param appPath relative path from which the iframe content is to be loaded
     * @param options optional extra parameters for setting up SPARK app and dialog creation
     * @returns an object with {
     *      iFrameDomEl: dom element of the iframe,
     *      iframeSparkContext: the JS object added to the iframe element that provides SPARK context to the app inside the iframe,
     *      appContainerId: id of the main inline dialog element
     * }
     */
    const openFullscreenIframeDialog = function(appName, appPath, options) {

        options = options || {};

        if (!appName) {
            throw new Error('Parameter missing - \'appName\'');
        } else if (!appPath) {
            throw new Error('Parameter missing - \'appPath\'');
        }

        const bodyEl = $('body');

        // to remove scrollers from content below the iframe dialog
        bodyEl.addClass('spark-no-scroll');

        const iframeSrc = getIframeSource(appPath, options.queryString);

        const elementIdSparkAppContainer = appName + '-spark-app-container';

        const dialogSettings = $.extend({ 'addChrome': false, 'addChromeCloseHandlers': false }, options);

        // make sure that element with the id is not already there
        // (in normal operation it is removed on dialog close)
        $('#' + elementIdSparkAppContainer).remove();

        const iframeWrapperElement = $(sparkTemplates.appFullscreenContainerIframe({
            id: elementIdSparkAppContainer,
            src: iframeSrc,
            createOptions: dialogSettings,
            className: cssClassName
        }));

        const closeFullScreenDialog = function(resultData) {
            // This is specific to full screen dialog
            bodyEl.removeClass('spark-no-scroll');
            closeSparkApp(iframeWrapperElement, dialogSettings.onClose, resultData);
        };

        // add an easy way for the contained iframe to access the dialog chrome (if added)
        let dialogChrome = null;
        if (dialogSettings.addChrome) {
            const cancelBtnDomEl = iframeWrapperElement.find('#' + elementIdSparkAppContainer + '-chrome-cancel').get()[0];
            const confirmBtnDomEl = iframeWrapperElement.find('#' + elementIdSparkAppContainer + '-chrome-submit').get()[0];
            dialogChrome = {
                'cancelBtn': cancelBtnDomEl,
                'confirmBtn': confirmBtnDomEl
            };

            // Close the dialog on submit / close click if set to use default close
            if (dialogSettings.addChromeCloseHandlers) {
                dialogChrome.cancelBtn.addEventListener('click', closeFullScreenDialog);
                dialogChrome.confirmBtn.addEventListener('click', closeFullScreenDialog);
            }
        }

        // Add the spark full screen dialog app to the body
        iframeWrapperElement.appendTo(bodyEl);

        // Setup the iFrame

        const iframeElement = iframeWrapperElement.find('iframe');

        function getMaxHeight() {
            let maxHeight = window.innerHeight;
            if (dialogSettings.addChrome) {
                maxHeight -= 51; // 51px is the height of the black bar at the top
            }
            return maxHeight;
        }

        const iframeResizerSettings = {
            'autoResize': true,
            'heightCalculationMethod': 'max',
            'maxHeight': getMaxHeight(),
            'scrolling': 'auto',
            'onResized': function(data) {
                // need to re-set maxHeight when window is resized
                // resizedCallback is called when the iframe is resized which should be enough
                data.iframe.style.maxHeight = getMaxHeight() + 'px';
            }
        };

        // deeply merge the iframeResizer default settings to the iframe options
        const sparkIframeSettings = $.extend(true, { 'iframeResizerSettings': iframeResizerSettings }, options);

        let { iframeDomEl, iframeSparkContext } = loadSparkToIframe(iframeElement, sparkIframeSettings);

        // Add dialog specific context data
        iframeSparkContext.dialogControls = {
            'closeDialog': closeFullScreenDialog,
            'dialogChrome': dialogChrome
        };

        return {
            iframeDomEl,
            iframeSparkContext,
            appContainerId: elementIdSparkAppContainer
        };

    };

    /**
     * Creates an inline dialog that is filled with an iframe loading the specified app
     *
     * The trigger element (triggerEl in returned object) needs to be added to a visible location in the DOM. Clicking on the trigger
     * element will show the inline dialog.
     *
     * Extra options available for inline dialog compared to other SPARK containers:
     * {
     *      alignment: alignment of the inline dialog regards to trigger element (eg. bottom left),
     *      width: (initial) width of the inline dialog,
     *      triggerText: the text to use for the trigger component,
     *      ... all the options that createAppIframe accepts
     * }
     *
     * @param appName name of the app (used as prefix for eg. element ids)
     * @param appPath relative path from which the iframe content is to be loaded
     * @param options optional extra parameters for setting up SPARK app and dialog creation
     * @returns an object with {
     *      triggerEl: element used for opening the inline dialog,
     *      iFrameDomEl: dom element of the iframe,
     *      iframeSparkContext: the JS object added to the iframe element that provides SPARK context to the app inside the iframe,
     *      appContainerId: id of the main inline dialog element
     * }
     */
    const openInlineIframeDialog = function(appName, appPath, options) {

        // Parameter validations
        if (!appName) {
            throw new Error('Parameter missing - \'appName\'');
        } else if (!appPath) {
            throw new Error('Parameter missing - \'appPath\'');
        }

        const bodyEl = $('body');

        const iframeSrc = getIframeSource(appPath, options.queryString);

        //Setup the Dialog
        const appContainerId = appName + '-spark-app-container';

        const dialogSettings = $.extend({
            width: '540px',
            triggerText: 'Inline trigger',
            alignment: 'bottom left'
        }, options);

        // Remove the app container if it still exists on the page
        // Ideally, closing the dialog should remove the container
        $('#' + appContainerId).remove();

        // Init the byline dialog trigger element and bind it to the body later
        const triggerEl = sparkTemplates.inlineDialogTrigger({
            targetId: appContainerId,
            text: dialogSettings.triggerText
        });

        // Init the spark inline dialog app container, and bind it to the body later
        const dialogEl = $(sparkTemplates.inlineDialogAppContainer({
            id: appContainerId,
            createOptions: dialogSettings,
            src: iframeSrc,
            className: cssClassName
        }));

        $(bodyEl).append(dialogEl);

        const iframeElement = dialogEl.find('iframe');

        // Setup the iFrame
        let { iframeDomEl, iframeSparkContext } = loadSparkToIframe(iframeElement, dialogSettings);

        return {
            triggerEl,
            iframeDomEl,
            iframeSparkContext,
            appContainerId
        };
    };

    return {
        'openFullscreenIframeDialog': openFullscreenIframeDialog,
        'openInlineIframeDialog': openInlineIframeDialog,
        'createAppIframe': createAppIframe
    };

};

export default {
    appLoader: new AppLoader(),
    iframeAppLoader: initIframeAppLoader(iframeResizer),
    // the init is exposed here too so that tests can initialize with mocked iFrameResizer
    initIframeAppLoader: initIframeAppLoader
};
