(function () {

    angular.module("myapp2", [])
        .run(function ($rootScope) {
            $rootScope.app = {
                helpLink: 'http://example.com',
                title: 'SPARK Dialog App 2!',
                footer: {
                    text: 'Powered by SPARK and Angular ' + angular.version.full + '.'
                }
            };
        })
        .controller("HelloController", function ($scope) {
            $scope.user = {
                'name': 'World'
            };
        });

})();
