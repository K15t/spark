(function() {

    //var angular = window.angular;

    console.log('initializing app');

    angular.module("myapp2", [])
        .controller("HelloController", function($scope) {
            $scope.helloTo = {};
            $scope.helloTo.title = "World, AngularJS 222";
            $scope.angularVersion = angular.version;
        } );

    console.log('app strted');

    console.log('starting app');
    console.log(AJS.$('#spark-dialog-test2 .spark-dialog-app'));
    angular.bootstrap(angular.element('#spark-dialog-test2 .spark-dialog-app'), ['myapp2']);
    window.angular = window.existingWindowDotAngular;
    delete window.existingWindowDotAngular;

})();
