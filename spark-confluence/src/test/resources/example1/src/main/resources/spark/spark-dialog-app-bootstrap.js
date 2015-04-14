

AJS.toInit(function () {

    var dialog1;
    var dialog2;

    AJS.whenIType('1').execute(function () {
        if (!dialog1) {
            dialog1 = startDialogApp('myapp1', '/plugins/servlet/dialog-app1');
        } else {
            dialog1.show();
        }
    });

    AJS.whenIType('2').execute(function () {
        if (!dialog2) {
            dialog2 = startDialogApp('myapp2', '/plugins/servlet/dialog-app2');
        } else {
            dialog2.show();
        }
    });


    var startDialogApp = function (angularApp, appPath) {

        var dialog = createDialog('spark-dialog-' + angularApp);


        // == test 1 ==========================================================

        AJS.$(dialog.$contentEl)
            .load(contextPath + appPath, function complete(response, status, xhr) {

                if (status == "error") {
                    dialog.$contentEl.html(
                        '<h2>Error</h2>' +
                        '<p>Error loading dialog app from \'' + appPath + '\'</p>'
                    );
                    dialog.show();

                } else {
                    var scripts = [];

                    AJS.$('meta[name=script]', dialog.$contentEl).each(function () {
                        scripts.push($(this).attr('content'));
                    });

                    console.log('Scripts for app: ' + angularApp);
                    console.log(scripts);


                    var angular;
                    window.existingWindowDotAngular = window.angular;
                    if (window.angular) {
                        var angular = (window.angular = {});
                    }

                    // https://github.com/rgrove/lazyload
                    LazyLoad.js(scripts, function () {
                        dialog.show();

                        console.log('starting app');
                        angular = window.angular;
                        angular.bootstrap(dialog.$appEl, [angularApp]);

                        window.angular = window.existingWindowDotAngular;
                        delete window.existingWindowDotAngular;
                    });
                }
            });

        return dialog;
    };

    var createDialog = function (dialogId) {
        var dialog;

        if (AJS.dialog2) {
            AJS.$('body').append(Spark.DialogApp.Templates.dialog2({
                dialogId: dialogId,
                title: 'xxx'
            }));
            dialog = AJS.dialog2('#' + dialogId);
            dialog.$appEl = dialog.$el;
            dialog.$contentEl = AJS.$('.spark-app-content', dialog.$appEl);

        } else {
            dialog = new AJS.Dialog({
                width: 800,
                height: 500,
                id: dialogId
            });
            dialog.$appEl = dialog.popup.element;
            dialog.$appEl.html(Spark.DialogApp.Templates.dialog());
            dialog.$contentEl = AJS.$('.spark-app-content', dialog.$appEl);
            dialog.$contentEl.height(dialog.$appEl.height() - 105);
        }

        return dialog;
    }

});