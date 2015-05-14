(function () {

    angular.module("dialog-app_v13", [])
        .run(function ($rootScope) {
            $rootScope.app = {
                helpLink: 'http://example.com',
                title: 'SPARK Dialog App w/ Angular 1.3!',
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
