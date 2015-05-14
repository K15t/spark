AJS.toInit(function () {

    'use strict';

    var spark = SPARK.noConflict();

    var dialog13, dialog14;

    AJS.$('#spark-dialog-app13-webitem').on('click', function () {
        if (!dialog13) {
            dialog13 = spark.createDialogApp('dialog-app_v13', '/plugins/servlet/dialog-app13/');
        } else {
            dialog13.show();
        }
    });

    AJS.$('#spark-dialog-app14-webitem').on('click', function () {
        if (!dialog14) {
            dialog14 = spark.createDialogApp('dialog-app_v14', '/plugins/servlet/dialog-app14/');
        } else {
            dialog14.show();
        }
    });

});