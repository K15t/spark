AJS.toInit(function($) {

    'use strict';


    function AppLoader() {

        var startedApps = {};

        this.loadAppInDialog = function(title, angularAppName, appPath, createOptions, callbackStarted) {
            var elementIdSparkAppContainer = angularAppName + '-spark-dialog-app-container';
            var dialog = createDialog(elementIdSparkAppContainer, SPARK.Common.Templates.appBootstrapContainerDialog2WithiFrame({
                id: elementIdSparkAppContainer,
                title: title,
                src: location.protocol + '//' + location.host + appPath,
                createOptions: createOptions
            }), createOptions.width, createOptions.height);

            $('#' + elementIdSparkAppContainer + '-iframe').iFrameResize([{
                log: true,
                autoResize: true,
                bodyBackground: 'red',
                readyCallback: callbackStarted
            }]);

            dialog.show();

            AJS.$('#closeDialogButton', dialog.$el).click(function(e) {
                e.preventDefault();
                dialog.hide();
            });
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

                if (createOptions.width === undefined) {
                    createOptions.width = '100%';
                }

                if (createOptions.height === undefined) {
                    createOptions.height = '100%';
                }

                $(element).append(SPARK.Common.Templates.appBootstrapContaineriFrame({
                    id: elementIdSparkAppContainer,
                    src: location.protocol + '//' + location.host + fullAppPath,
                    createOptions: createOptions
                }));

                return;
            }

            $(element).append(SPARK.Common.Templates.appBootstrapContainer({
                id: elementIdSparkAppContainer
            }));

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


        var createErrorDialog = function(id) {
            var dialog;

            if (AJS.dialog2) {
                dialog = createDialog(id, SPARK.Common.Templates.errorDialog2({
                    id: id,
                    title: 'An error happened ...'
                }));
            } else {
                dialog = createDialog(id, SPARK.Common.Templates.errorDialog({
                    title: 'An error happened ...'
                }), 800, 500);
            }

            $('.aui-blanket').addClass('spark-loading');

            return dialog;
        };


        var createDialog = function(id, dialogMarkup, width, height) {

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

    // init SPARK context ==================================================================

    if (window.SPARK === undefined) {
        window.SPARK = {};
    }

    if (window.SPARK.appLoader === undefined) {
        window.SPARK.appLoader = new AppLoader();
    }

})(AJS.$);