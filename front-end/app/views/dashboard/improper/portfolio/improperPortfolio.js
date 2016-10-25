define([
    'app',
    'angular-material-data-table',
    'services/serverCallService',
    'views/dashboard/baseTable/baseTable'
], function(app) {
    app.controller('imporperPortfoliosController', ['$scope', 'serverCallService', '$controller', '$filter',
        function($scope, serverCallService, $controller, $filter) {
            var base = $controller('baseTableController', {
                $scope: $scope
            });

            serverCallService.makeGet("rest/impropers/portfolios", {}, getImproperPortfoliosSuccess, base.getItemsFail);

            $scope.title = $filter('translate')('DASHBOARD_IMRPOPER_PORTFOLIOS');

            $scope.bindTable = function() {
                base.buildTable('#improper-portfolios-table', 'views/dashboard/improper/improper.html');
            };

            $scope.getLearningObjectTitle = function(portfolio) {
            	return portfolio.title;
            };

            $scope.getLearningObjectUrl = function(learningObject) {
            	return "/portfolio?id=" + learningObject.id;
            };

            function getImproperPortfoliosSuccess(impropers) {
                if (impropers) {
                    base.getItemsSuccess(impropers, 'byReportCount', true);
                } else {
                    base.getItemsFail();
                }
            }
        }
    ]);
});
