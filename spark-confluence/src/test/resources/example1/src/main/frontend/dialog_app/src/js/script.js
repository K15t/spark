(function() {

    //var angular = window.angular;

    console.log('initializing app');

    angular.module("myapp1", [])
        .controller("HelloController", function($scope) {
            $scope.helloTo = {};
            $scope.helloTo.title = "World, AngularJS";
            $scope.angularVersion = angular.version;
        } );

    console.log('app strted');

    console.log('starting app');
    console.log(AJS.$('#spark-dialog-test1 .spark-dialog-app'));
    angular.bootstrap(angular.element('#spark-dialog-test1 .spark-dialog-app'), ['myapp1']);
    window.angular = window.existingWindowDotAngular;
    delete window.existingWindowDotAngular;

})();
