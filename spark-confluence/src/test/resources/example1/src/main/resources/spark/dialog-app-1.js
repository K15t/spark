AJS.toInit(function () {

    'use strict';

    var spark = SPARK.noConflict();

    var dialog1, dialog2;

    AJS.whenIType('1').execute(function () {
        if (!dialog1) {
            dialog1 = spark.createDialogApp('myapp1', '/plugins/servlet/dialog-app1');
        } else {
            dialog1.show();
        }

    });
    AJS.whenIType('2').execute(function () {
        if (!dialog2) {
            dialog2 = spark.createDialogApp('myapp2', '/plugins/servlet/dialog-app2');
        } else {
            dialog2.show();
        }

    });

});