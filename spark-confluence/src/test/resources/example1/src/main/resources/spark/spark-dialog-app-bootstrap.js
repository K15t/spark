AJS.toInit(function() {

    var dialog1;
    var dialog2;

    AJS.whenIType('1').execute(function() {
        if (!dialog1) {
            dialog1 = startDialogApp(
                'spark-dialog-test1',
                '/plugins/servlet/dialog-app1',
                'The \'Hello World\' SPARK Dialog App'
            );
        } else {
            dialog1.show();
        }
    });

    AJS.whenIType('2').execute(function() {
        if (!dialog2) {
            dialog2 = startDialogApp(
                'spark-dialog-test2',
                '/plugins/servlet/dialog-app2',
                'The \'Hello World\' SPARK Dialog App'
            );
        } else {
            dialog2.show();
        }
    });


    var startDialogApp = function(appId, appPath, title) {



        AJS.$('body').append(Spark.DialogApp.Templates.dialog({
            appId: appId,
            title: title
        }));

        var dialog = AJS.dialog2('#' + appId);


        // == test 1 ==========================================================

        AJS.$('.spark-dialog-app', dialog.$el)
            .load(contextPath + appPath, function complete(response, status, xhr) {

                if ( status == "error" ) {
                    // TODO: Nice error handling
                    AJS.$('.spark-dialog-app', dialog.$el).html(
                        '<h2>Error</h2>' +
                        '<p>Error loading dialog app from \'' + appPath + '\'</p>'
                    );
                    dialog.show();
                } else {
                    var scripts = [];

                    AJS.$('meta[name=script]', dialog.$el).each(function() {
                        scripts.push($(this).attr('content'));
                    });

                    console.log('Scripts for app: ' + appId);
                    console.log(scripts);

                    // https://github.com/rgrove/lazyload
                    LazyLoad.js(scripts, function() {
                        dialog.show();
                    });
                }
            });

        return dialog;
    };

});