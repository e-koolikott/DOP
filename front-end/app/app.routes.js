'use strict';

angular.module('koolikottApp')
    .config([
        '$routeProvider',
        function ($routeProvider) {

            $routeProvider.otherwise('/');

            $routeProvider
                .when('/', {
                    templateUrl: 'views/home/home.html',
                    controller: 'homeController'
                })
                .when('/search/result', {
                    templateUrl: 'views/search/result/searchResult.html',
                    controller: 'searchResultController',
                    reloadOnSearch: false
                })
                .when('/material', {
                    templateUrl: 'views/material/material.html',
                    controller: 'materialController',
                    reloadOnSearch: false
                })
                .when('/help', {
                    templateUrl: 'views/static/abstractStaticPage.html',
                    controller: 'helpController'
                })
                .when('/about', {
                    templateUrl: 'views/static/abstractStaticPage.html',
                    controller: 'aboutController'
                })
                .when('/portfolio', {
                    templateUrl: 'views/portfolio/portfolio.html',
                    controller: 'portfolioController',
                    reloadOnSearch: false
                })
                .when('/portfolio/edit', {
                    templateUrl: 'views/editPortfolio/editPortfolio.html',
                    controller: 'editPortfolioController',
                    reloadOnSearch: false
                })
                .when('/dashboard', {
                    templateUrl: 'views/dashboard/dashboard.html',
                    controller: 'dashboardController',
                    permissions: ['ADMIN', 'MODERATOR']
                })
                .when('/dashboard/improperMaterials', {
                    templateUrl: 'views/dashboard/baseTableView.html',
                    controller: 'baseTableViewController',
                    controllerAs: '$ctrl',
                    permissions: ['ADMIN', 'MODERATOR']
                })
                .when('/dashboard/improperPortfolios', {
                    templateUrl: 'views/dashboard/baseTableView.html',
                    controller: 'baseTableViewController',
                    controllerAs: '$ctrl',
                    permissions: ['ADMIN', 'MODERATOR']
                })
                .when('/dashboard/brokenMaterials', {
                    templateUrl: 'views/dashboard/baseTableView.html',
                    controller: 'baseTableViewController',
                    controllerAs: '$ctrl',
                    permissions: ['ADMIN', 'MODERATOR']
                })
                .when('/dashboard/deletedMaterials', {
                    templateUrl: 'views/dashboard/baseTableView.html',
                    controller: 'baseTableViewController',
                    controllerAs: '$ctrl',
                    permissions: ['ADMIN', 'MODERATOR']
                })
                .when('/dashboard/deletedPortfolios', {
                    templateUrl: 'views/dashboard/baseTableView.html',
                    controller: 'baseTableViewController',
                    controllerAs: '$ctrl',
                    permissions: ['ADMIN', 'MODERATOR']
                })
                .when('/dashboard/moderators', {
                    templateUrl: 'views/dashboard/baseTableView.html',
                    controller: 'baseTableViewController',
                    controllerAs: '$ctrl',
                    permissions: ['ADMIN']
                })
                .when('/dashboard/restrictedUsers', {
                    templateUrl: 'views/dashboard/baseTableView.html',
                    controller: 'baseTableViewController',
                    controllerAs: '$ctrl',
                    permissions: ['ADMIN']
                })
                .when('/dashboard/changedLearningObjects', {
                    templateUrl: 'views/dashboard/baseTableView.html',
                    controller: 'baseTableViewController',
                    controllerAs: '$ctrl',
                    permissions: ['ADMIN', 'MODERATOR']
                })
                .when('/dashboard/unReviewed', {
                    templateUrl: 'views/dashboard/baseTableView.html',
                    controller: 'baseTableViewController',
                    controllerAs: '$ctrl',
                    permissions: ['ADMIN', 'MODERATOR']
                })
                .when('/loginRedirect', {
                    templateUrl: 'views/loginRedirect/loginRedirect.html',
                    controller: 'loginRedirectController'
                })
                .when('/:username', {
                    templateUrl: 'views/profile/profile.html',
                    controller: 'profileController',
                    resolve: UserPathResolver
                })
                .when('/:username/materials', {
                    templateUrl: 'views/profile/materials/materials.html',
                    controller: 'userMaterialsController',
                    resolve: UserPathResolver
                })
                .when('/:username/portfolios', {
                    templateUrl: 'views/profile/portfolios/portfolios.html',
                    controller: 'userPortfoliosController',
                    resolve: UserPathResolver
                })
                .when('/:username/favorites', {
                    templateUrl: 'views/profile/favorites/favorites.html',
                    controller: 'userFavoritesController',
                    resolve: UserPathResolver

                })
                .when('/dev/login/:idCode', {
                    templateUrl: 'views/dev/login/login.html',
                    controller: 'devLoginController'
                });
        }
    ]);

let UserPathResolver = {
    isLoggedIn: ['authenticatedUserService', '$location', function (authenticatedUserService, $location) {
        if (!authenticatedUserService.isAuthenticated()) {
            $location.path("/");
        }
    }]
};
