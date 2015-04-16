(function() {

    angular.module("myapp2", [])
        .run(function($rootScope) {
            $rootScope.app = {
                title : 'Dialog 2.'
            };
        })
        .controller("HelloController", function($scope) {
            $scope.helloTo = {};
            $scope.helloTo.title = "World, AngularJS 222";
            $scope.angularVersion = angular.version;
        } );

})();
