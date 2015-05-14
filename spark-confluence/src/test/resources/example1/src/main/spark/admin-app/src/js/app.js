(function () {

    angular.module("admin-app", [])
        .controller("HelloController", function ($scope) {
            $scope.user = {
                'name': 'World'
            };
            $scope.angularVersion = angular.version.full;
        });

})();
