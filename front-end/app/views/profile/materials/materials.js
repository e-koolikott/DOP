'use strict'

angular.module('koolikottApp')
.controller('userMaterialsController',
[
    '$scope', '$route', '$location', 'authenticatedUserService',
    function ($scope, $route, $location, authenticatedUserService) {
        function init() {
            $scope.cache = false;
            $scope.url = "rest/material/getByCreator";
            $scope.params = {
                'maxResults': 20,
                'username': authenticatedUserService.getUser().username
            };
            $location.url(`/${$scope.params.username}/oppematerjalid`)

            setTitle();
        }

        function setTitle() {
            const user = authenticatedUserService.getUser();
            if (user && $route.current.params.username === user.username) {
                $scope.title = 'MYPROFILE_PAGE_TITLE_MATERIALS';
            } else {
                $scope.title = 'PROFILE_PAGE_TITLE_MATERIALS';
            }
        }

        init();
    }
]);
