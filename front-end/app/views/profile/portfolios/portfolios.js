'use strict'

angular.module('koolikottApp')
.controller('userPortfoliosController',
[
    '$scope', '$route', 'authenticatedUserService',
    function ($scope, $route, authenticatedUserService) {
        function init() {
            $scope.cache = false;
            $scope.url = "rest/portfolio/getByCreator";
            $scope.params = {
                'maxResults': 20,
                'username': $route.current.params.username
            };

            setTitle();
        }

        function setTitle() {
            var user = authenticatedUserService.getUser();
            if (user && $route.current.params.username === user.username) {
                $scope.title = 'MYPROFILE_PAGE_TITLE_PORTFOLIOS';
            } else {
                $scope.title = 'PROFILE_PAGE_TITLE_PORTFOLIOS';
            }
        }

        init();
    }
]);
