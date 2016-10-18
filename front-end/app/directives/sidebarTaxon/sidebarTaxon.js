define([
    'angularAMD',
    'services/recursionHelper',
], function (angularAMD) {
    angularAMD.directive('dopSidebarTaxon', ['RecursionHelper', function (RecursionHelper) {
        return {
            scope: {
                taxon: '=',
                icon: '=',
                tree: '='
            },
            templateUrl: 'directives/sidebarTaxon/sidebarTaxon.html',
            compile: function(element) {
              return RecursionHelper.compile(element);
            },
            controller: function ($rootScope, $scope, $location) {
                $scope.id;
                if ($scope.taxon) {
                    if ($scope.taxon.children.length > 0) {
                        $scope.hasChildren = true;
                        $scope.childrenCount = $scope.taxon.children.length;
                        $scope.id = $scope.taxon.id;
                    }

                    if($scope.tree) {
                        if ($scope.taxon.id == $scope.tree.id) {
                            $scope.opened = true;
                        }
                    }

                }

                $scope.toggleChildren = function(id) {
                    if ($scope.opened == null) {
                        $location.url('search/result?q=&taxon=' + id);
                        $rootScope.currentlyOpenTaxonId = id;
                        $scope.opened = true;
                    } else if ($scope.opened == true) {
                        $scope.opened = false;
                    } else if ($scope.opened == false) {
                        $location.url('search/result?q=&taxon=' + id);
                        $rootScope.currentlyOpenTaxonId = id;
                        $scope.opened = true;
                    }
                    console.log($scope.id);
                    console.log($rootScope.currentlyOpenTaxonId);
                }

                $scope.getTaxonTranslation = function(data) {
                    if(data.level !== '.EducationalContext') {
                        return data.level.toUpperCase().substr(1) + "_" + data.name.toUpperCase();
                    } else {
                        return data.name.toUpperCase();
                    }

                }
            }
        }
    }]);
});
