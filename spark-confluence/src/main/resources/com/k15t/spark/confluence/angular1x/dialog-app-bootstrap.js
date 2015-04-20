(function ($) {

    'use strict';

    var oldSpark = window.SPARK;

    window.SPARK = {};
    window.SPARK.Templates = oldSpark.Templates;


    window.SPARK.noConflict = function () {
        var newSpark = window.SPARK;
        window.SPARK = oldSpark;
        return newSpark;
    };


    window.SPARK.createDialogApp = function (angularApp, appPath) {

        // append trailing slash if not there.
        var fullAppPath = contextPath + (/\/$/.test(appPath) ? appPath : appPath + '/');

        var dialog = createDialog('spark-dialog-' + angularApp);

        // We have to use an additional element (div#spark-dialog-app-wrapper),
        // because body doesn't work, because it is using the browsers .innerHtml
        // property, which doesn't work consistently across browsers and for
        // example in Chrome includes elements from head, too. The additional
        // element is inserted automatically by the AppServlet.
        // More info: https://api.jquery.com/load/#loading-page-fragments
        $(dialog.$contentEl).load(fullAppPath + ' div#spark-dialog-app-wrapper > *', function complete(response, status, xhr) {

            if (status == "error") {
                var absoluteAppPath = location.protocol + '://' + location.host + fullAppPath;
                dialog.$titleEl.html('Error');
                dialog.$contentEl.html(
                    '<h2>Could not load dialog app from \'' + fullAppPath + '\'</h2>' +
                    '<p>Have you created a servlet module for the DialogAppServlet in <code>atlassian-plugin.xml</code>?</p>' +
                    '<p>It should be available at <a href="' + fullAppPath + '">' + absoluteAppPath + '</a></p>'
                );

            } else {

                window.existingWindowDotAngular = window.angular;
                var angular = (window.angular = {});

                // https://github.com/rgrove/lazyload
                LazyLoad.js(getScripts(dialog.$contentEl), function () {
                    angular.bootstrap(dialog.$appEl, [angularApp]);
                    window.angular = window.existingWindowDotAngular ? window.existingWindowDotAngular : window.angular;
                    delete window.existingWindowDotAngular;
                });
            }
        });

        return dialog;
    };


    var createDialog = function (dialogId) {
        var dialog;

        if (AJS.dialog2) {
            $('body').append(SPARK.DialogApp.Templates.dialog2({
                dialogId: dialogId
            }));
            dialog = AJS.dialog2('#' + dialogId);
            dialog.$appEl = dialog.$el;
            dialog.$titleEl = $('h2:contains({{app.title}})', dialog.$appEl);
            dialog.$contentEl = $('.spark-app-content', dialog.$appEl);

        } else {
            dialog = new AJS.Dialog({
                width: 800,
                height: 500,
                id: dialogId
            });
            dialog.$appEl = dialog.popup.element;
            dialog.$appEl.html(Spark.DialogApp.Templates.dialog());
            dialog.$titleEl = $('h2:contains({{app.title}})', dialog.$appEl);
            dialog.$contentEl = $('.spark-app-content', dialog.$appEl);
            dialog.$contentEl.height(dialog.$appEl.height() - 105);
        }

        dialog.show();
        // TODO use Confluence.PageLoadingIndicator().show() here and
        $('.aui-blanket').addClass('spark-loading');
        return dialog;
    };


    var getScripts = function ($contentEl) {
        var scripts = [];

        $('meta[name=script]', $contentEl).each(function () {
            scripts.push($(this).attr('content'));
            $(this).remove();
        });

        return scripts;
    };

})(AJS.$);