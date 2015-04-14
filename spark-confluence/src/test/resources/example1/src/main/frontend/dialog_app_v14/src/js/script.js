(function() {

    angular.module("myapp2", [])
        .controller("HelloController", function($scope) {
            $scope.helloTo = {};
            $scope.helloTo.title = "World, AngularJS 222";
            $scope.angularVersion = angular.version;
        } );

})();
