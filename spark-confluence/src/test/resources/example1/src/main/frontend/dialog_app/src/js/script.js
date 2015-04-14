(function() {

    angular.module("myapp1", [])
        .run(function($rootScope) {
            $rootScope.dialog = {};
            $rootScope.dialog.helpLink = 'http://example.com';
            $rootScope.dialog.title = 'Hullo.';
        })
        .controller("HelloController", function($scope) {
            $scope.helloTo = {};
            $scope.helloTo.title = "World, AngularJS";
            $scope.angularVersion = angular.version;
        }
    );

})();
