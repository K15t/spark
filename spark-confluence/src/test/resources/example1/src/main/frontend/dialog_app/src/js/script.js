(function() {

    angular.module("myapp1", [])
        .run(function($rootScope) {
            $rootScope.app = {
                helpLink : 'http://example.com',
                title : 'Hullo.'
            };
        })
        .controller("HelloController", function($scope) {
            $scope.helloTo = {};
            $scope.helloTo.title = "World, AngularJS";
            $scope.angularVersion = angular.version;
        }
    );

})();
