define(['app'], function (app) {
    app.controller('addMaterialDialog', ['$scope', '$mdDialog', 'serverCallService', 'translationService',
        function ($scope, $mdDialog, serverCallService, translationService) {
            var preferredLanguage;

            var TABS_COUNT = 5;
            if ($scope.material === undefined) {
                $scope.material = {};
            }

            $scope.material.metadata = [];
            $scope.material.tags = [];
            $scope.material.taxons = [{}];
            $scope.material.author = {};

            $scope.step = {};
            $scope.step.currentStep = 0;
            $scope.step.canProceed = false;
            $scope.step.isMaterialUrlStepValid = false;
            $scope.step.isMetadataStepValid = false;

            init();

            $scope.step.nextStep = function () {
                $scope.step.currentStep += 1;
            };

            $scope.step.previousStep = function () {
                $scope.step.currentStep -= 1;
            };

            $scope.step.isTabDisabled = function (index) {
                if (index == 0)
                    return false;

                return !isStepValid(index - 1);
            };

            $scope.step.canProceed = function () {
                return isStepValid($scope.step.currentStep);
            };

            $scope.step.canCreateMaterial = function () {
                return isStepValid(2);
            };

            $scope.step.isLastStep = function () {
                return $scope.step.currentStep === TABS_COUNT;
            };

            $scope.$watch('materialUrlForm.$valid', function (isValid) {
                $scope.step.isMaterialUrlStepValid = isValid;
            });

            $scope.addNewMetadata = function () {
                $scope.material.metadata.forEach(function (item) {
                    item.expanded = false
                });

                addNewMetadata();
            };

            $scope.deleteMetadata = function (index) {
                $scope.material.metadata.splice(index, 1);
            };

            $scope.addNewTaxon = function () {
                $scope.material.taxons.push({});
            };

            $scope.deleteTaxon = function (index) {
                $scope.material.taxons.splice(index, 1);
            };

            $scope.getLanguageById = function (id) {
                return $scope.material.languages.filter(function (language) {
                    return language.id == id;
                })[0].name;
            };

            $scope.cancel = function () {
                $mdDialog.hide();
            };

            $scope.createMaterial = function () {
                var material = $scope.material;
                var metadata = getMetadata(material);
                var titles = metadata.titles;
                var descriptions = metadata.descriptions;
                var publisher = getPublisher(material);
                var author = getAuthor(material);
                var licenseType = getLicenseType(material);
                var resourceTypes = getResourceTypes(material);
                var base64Picture = getPicture(material);
                var taxons = getTaxons(material);

                var newMaterial = {
                    type: '.Material',
                    source: material.url,
                    language: material.language,
                    titles: titles,
                    descriptions: descriptions,
                    tags: material.tags,
                    paid: material.paid,
                    publishers: [publisher],
                    authors: [author],
                    targetGroups: material.targetGroups,
                    licenseType: licenseType,
                    resourceTypes: resourceTypes,
                    picture: base64Picture,
                    taxons: taxons
                };

                serverCallService.makePost("rest/material", newMaterial, postMaterialSuccess, postMaterialFail);
                $mdDialog.hide();
            };

            function getTaxons(material) {
                if (material.taxons[0].id) {
                    var taxons = material.taxons
                }
                return taxons;
            }

            function getPicture(material) {
                if (material.picture) {
                    var base64Picture = material.picture.$ngfDataUrl;
                }
                return base64Picture;
            }

            function getResourceTypes(material) {
                var resourceTypes = [];
                material.resourceTypes.forEach(function (item) {
                    if (item.selected) {
                        resourceTypes.push(item);
                    }
                });
                return resourceTypes;
            }

            function getPublisher(material) {
                if (material.publisher) {
                    var publisher = {
                        name: material.publisher
                    }
                }
                return publisher;
            }

            function getLicenseType(material) {
                if (material.licenseType) {
                    var licenseType = JSON.parse(material.licenseType)
                }
                return licenseType;
            }

            function getMetadata(material) {
                var titles = [];
                var descriptions = [];


                material.metadata.forEach(function (item) {
                    if (item.title) {
                        var title = {
                            language: item.language,
                            text: item.title
                        };

                        titles.push(title);
                    }

                    if (item.description) {
                        var description = {
                            language: item.language,
                            text: item.description
                        };

                        descriptions.push(description);
                    }
                });

                return {
                    titles: titles,
                    descriptions: descriptions
                };
            }

            function getAuthor(material) {
                if (material.author.name && material.author.surname) {
                    var author = {
                        name: material.author.name,
                        surname: material.author.surname
                    };
                }
                return author;
            }

            function isStepValid(index) {
                switch (index) {
                    case 0:
                        return $scope.step.isMaterialUrlStepValid;
                    case 1:
                        return isStepValid(0) && isMetadataStepValid();
                    default:
                        return isStepValid(index - 1);
                }
            }

            function init() {
                serverCallService.makeGet("rest/learningMaterialMetadata/language", {}, getLanguagesSuccess, getLanguagesFail, getLanguageFinally);
                serverCallService.makeGet("rest/learningMaterialMetadata/licenseType", {}, getLicenseTypeSuccess, getLicenseTypeFail);
                serverCallService.makeGet("rest/learningMaterialMetadata/resourceType", {}, getResourceTypeSuccess, getResourceTypeFail);
            }

            function postMaterialSuccess(data) {
                if (!isEmpty(data)) {
                    console.log("material added");
                }
            }

            function postMaterialFail() {
                console.log('Failed to add material.')
            }

            function getLanguagesSuccess(data) {
                if (!isEmpty(data)) {
                    $scope.material.languages = data;

                    setDefaultMaterialMetadataLanguage();
                }
            }

            function getLanguagesFail() {
                console.log('Failed to get languages.')
            }

            function getLanguageFinally() {
                addNewMetadata();
            }

            function getLicenseTypeSuccess(data) {
                if (!isEmpty(data)) {
                    $scope.material.licenceTypes = data;
                }
            }

            function getLicenseTypeFail() {
                console.log('Failed to get license types.');
            }

            function getResourceTypeSuccess(data) {
                if (!isEmpty(data)) {
                    $scope.material.resourceTypes = data;
                }
            }

            function getResourceTypeFail() {
                console.log('Failed to get resource types.');
            }

            function setDefaultMaterialMetadataLanguage() {
                var userLanguage = translationService.getLanguage();

                preferredLanguage = $scope.material.languages.filter(function (language) {
                    return language == userLanguage;
                });
            }

            function addNewMetadata() {
                var metadata = {
                    expanded: true,
                    title: ''
                };

                if (preferredLanguage !== null && preferredLanguage !== undefined)
                    metadata.language = preferredLanguage[0];

                $scope.material.metadata.push(metadata);
            }

            function isMetadataStepValid() {
                return $scope.material.metadata.filter(function (metadata) {
                        return metadata.title.length !== 0;
                    }).length !== 0;
            }
        }
    ]);
});