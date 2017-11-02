'use strict';

angular.module('koolikottApp')
    .controller('materialController', [
        '$scope', 'serverCallService', '$route', 'translationService', '$rootScope',
        'searchService', '$location', 'alertService', 'authenticatedUserService', 'dialogService',
        'toastService', 'iconService', '$mdDialog', 'storageService', 'targetGroupService', 'taxonService', 'taxonGroupingService', 'eventService', 'materialService', '$sce',
        function ($scope, serverCallService, $route, translationService, $rootScope,
                  searchService, $location, alertService, authenticatedUserService, dialogService,
                  toastService, iconService, $mdDialog, storageService, targetGroupService, taxonService, taxonGroupingService, eventService, materialService, $sce) {

            $scope.showMaterialContent = false;
            $scope.newComment = {};
            $scope.pageUrl = $location.absUrl();
            $scope.getMaterialSuccess = getMaterialSuccess;
            $scope.taxonObject = {};
            $scope.materialCommentsOpen = false;

            const licenceTypeMap = {
                'CCBY': ['by'],
                'CCBYSA': ['by', 'sa'],
                'CCBYND': ['by', 'nd'],
                'CCBYNC': ['by', 'nc'],
                'CCBYNCSA': ['by', 'nc', 'sa'],
                'CCBYNCND': ['by', 'nc', 'nd']
            };

            if (storageService.getMaterial() && storageService.getMaterial().type !== ".ReducedMaterial" && storageService.getMaterial().type !== ".AdminMaterial") {
                $scope.material = storageService.getMaterial();

                if ($rootScope.isEditPortfolioMode || authenticatedUserService.isAuthenticated()) {
                    $rootScope.selectedSingleMaterial = $scope.material;
                }

                init();
            } else {
                getMaterial(getMaterialSuccess, getMaterialFail);
            }

            $scope.$watch(() => {
                return $scope.material;
            }, () => {
                if ($scope.material && $scope.material.id) {
                    getContentType();
                }
            });

            $scope.toggleCommentSection = () => {
                $scope.commentsOpen = !$scope.commentsOpen;
            };

            function getContentType() {
                var baseUrl = document.location.origin;
                var materialSource = getSource($scope.material);
                // If the initial type is a LINK, try to ask the type from our proxy
                if (materialSource && (matchType(materialSource) === 'LINK' || !materialSource.startsWith(baseUrl))) {
                    $scope.fallbackType = matchType(materialSource);
                    $scope.proxyUrl = baseUrl + "/rest/material/externalMaterial?url=" + encodeURIComponent($scope.material.source);
                    serverCallService.makeHead($scope.proxyUrl, {}, probeContentSuccess, probeContentFail);
                }
                if (materialSource) {
                    $scope.sourceType = matchType(getSource($scope.material));
                    if ($scope.sourceType == "EBOOK" && isIE()) $scope.material.source += "?archive=true";
                }
            }

            function probeContentSuccess(response) {
                if (!response()['content-disposition']) {
                    $scope.sourceType = $scope.fallbackType;
                    return;
                }
                var filename = response()['content-disposition'].match(/filename="(.+)"/)[1];
                $scope.sourceType = matchType(filename);
                if ($scope.sourceType !== 'LINK') {
                    $scope.material.source = $scope.proxyUrl;
                }
            }

            function probeContentFail() {
                console.log("Content probing failed!");
            }

            $rootScope.$on('fullscreenchange', () => {
                $scope.$apply(() => {
                    $scope.showMaterialContent = !$scope.showMaterialContent;
                });
            });

            $scope.$watch(() => {
                return storageService.getMaterial();
            }, updateMaterial);

            function updateMaterial(newMaterial, oldMaterial) {
                if (newMaterial !== oldMaterial) {
                    $scope.material = newMaterial;
                    $scope.material.source = decodeUTF8($scope.material.source);
                    processMaterial();
                }
            }

            function getMaterial(success, fail) {
                materialService.getMaterialById($route.current.params.id)
                    .then(success, fail)
            }

            function getMaterialSuccess(material) {
                if (isEmpty(material)) {
                    console.log('No data returned by getting material. Redirecting to landing page');
                    alertService.setErrorAlert('ERROR_MATERIAL_NOT_FOUND');
                    $location.url("/");
                } else {
                    $scope.material = material;

                    if ($rootScope.isEditPortfolioMode || authenticatedUserService.isAuthenticated()) {
                        $rootScope.selectedSingleMaterial = $scope.material;
                    }
                    init();
                }
            }

            function getMaterialFail() {
                log('Getting materials failed. Redirecting to landing page');
                alertService.setErrorAlert('ERROR_MATERIAL_NOT_FOUND');
                $location.url("/");
            }

            function processMaterial() {
                if ($scope.material) {
                    if ($scope.sourceType == "EBOOK") {
                        $scope.ebookLink = "/libs/bibi/bib/i/?book=" +
                            $scope.material.uploadedFile.id + "/" +
                            $scope.material.uploadedFile.name;
                    }

                    eventService.notify('material:reloadTaxonObject');
                    $scope.targetGroups = getTargetGroups();
                    $rootScope.learningObjectChanged = ($scope.material.changed > 0);

                    if ($scope.material.embeddable && $scope.sourceType === 'LINK') {
                        if (authenticatedUserService.isAuthenticated()) {
                            getSignedUserData()
                        } else {
                            $scope.material.iframeSource = $sce.trustAsResourceUrl($scope.material.source);
                        }
                    }
                }
            }

            function init() {
                $scope.material.source = getSource($scope.material);
                getContentType();
                processMaterial();

                eventService.subscribe($scope, 'taxonService:mapInitialized', getTaxonObject);
                eventService.subscribe($scope, 'material:reloadTaxonObject', getTaxonObject);

                eventService.notify('material:reloadTaxonObject');

                $rootScope.learningObjectBroken = ($scope.material.broken > 0);
                $rootScope.learningObjectImproper = ($scope.material.improper > 0);
                $rootScope.learningObjectDeleted = ($scope.material.deleted == true);
                $rootScope.learningObjectUnreviewed = !!$scope.material.unReviewed;

                materialService.increaseViewCount($scope.material);
            }

            $scope.getLicenseIconList = () => {
                if ($scope.material && $scope.material.licenseType) {
                    return licenceTypeMap[$scope.material.licenseType.name];
                }
            };

            $scope.getMaterialEducationalContexts = () => {
                var educationalContexts = [];
                if (!$scope.material || !$scope.material.taxons) return;

                $scope.material.taxons.forEach((taxon) => {
                    let edCtx = taxonService.getEducationalContext(taxon);
                    if (edCtx && !educationalContexts.includes(edCtx)) educationalContexts.push(edCtx);
                });

                return educationalContexts;
            };

            $scope.getCorrectLanguageString = (languageStringList) => {
                if (languageStringList) {
                    return getUserDefinedLanguageString(languageStringList, translationService.getLanguage(), $scope.material.language);
                }
            };

            $scope.formatMaterialIssueDate = (issueDate) => formatIssueDate(issueDate);
            $scope.formatMaterialUpdatedDate = (updatedDate) => formatDateToDayMonthYear(updatedDate);
            $scope.isNullOrZeroLength = (arg) => !arg || !arg.length;

            $scope.getAuthorSearchURL = ($event, firstName, surName) => {
                $event.preventDefault();

                searchService.setSearch('author:"' + firstName + " " + surName + '"');
                $location.url(searchService.getURL());
            };

            $scope.showSourceFullscreen = ($event, ctrl) => {
                $event.preventDefault();
                ctrl.toggleFullscreen();
            };

            $scope.isLoggedIn = () => authenticatedUserService.isAuthenticated();
            $scope.isAdmin = () => authenticatedUserService.isAdmin();
            $scope.isModerator = () => authenticatedUserService.isModerator();
            $scope.isRestricted = () => authenticatedUserService.isRestricted();
            $scope.modUser = () => !!(authenticatedUserService.isModerator() || authenticatedUserService.isAdmin());

            $scope.processMaterial = () => {
                processMaterial();
            };

            $scope.$on("tags:updateMaterial", (event, value) => {
                updateMaterial(value, $scope.material);
            });

            $scope.isAdminButtonsShowing = () => {
                return ($rootScope.learningObjectDeleted == false
                    && $rootScope.learningObjectImproper == false
                    && $rootScope.learningObjectBroken == true)
                    || ($rootScope.learningObjectDeleted == false
                    && $rootScope.learningObjectBroken == false
                    && $rootScope.learningObjectImproper == true)
                    || ($rootScope.learningObjectDeleted == false
                    && $rootScope.learningObjectBroken == true
                    && $rootScope.learningObjectImproper == true)
                    || ($rootScope.learningObjectDeleted == true);
            };

            function getTaxonObject() {
                if ($scope.material && $scope.material.taxons) {
                    $scope.taxonObject = taxonGroupingService.getTaxonObject($scope.material.taxons);
                }
            }

            function getSignedUserData() {
                serverCallService.makeGet("rest/user/getSignedUserData", {}, getSignedUserDataSuccess, getSignedUserDataFail);
            }

            function getSignedUserDataSuccess(data) {
                var url = $scope.material.source;
                var v = encodeURIComponent(data);
                url += (url.split('?')[1] ? '&' : '?') + "dop_token=" + v;

                $scope.material.iframeSource = url;
            }

            function getSignedUserDataFail(data, status) {
                console.log("Failed to get signed user data.")
            }

            $scope.addComment = (newComment, material) => {
                materialService.addComment(newComment, material)
                    .then(addCommentSuccess, addCommentFailed);
            };

            $scope.edit = () => {
                var editMaterialScope = $scope.$new(true);
                editMaterialScope.material = $scope.material;

                $mdDialog.show({
                    templateUrl: 'addMaterialDialog.html',
                    controller: 'addMaterialDialogController',
                    scope: editMaterialScope
                }).then((material) => {
                    if (material) {
                        $scope.material = material;
                        processMaterial();
                        $rootScope.$broadcast('materialEditModalClosed');
                    }
                });
            };

            function addCommentSuccess() {
                $scope.newComment.text = "";

                getMaterial((material) => {
                    $scope.material = material;
                }, () => {
                    log("Comment success, but failed to reload material.");
                });
            }

            function addCommentFailed() {
                log('Adding comment failed.');
            }

            $scope.getType = () => {
                if ($scope.material === undefined || $scope.material === null) return '';

                return iconService.getMaterialIcon($scope.material.resourceTypes);
            };

            $scope.getTypeName = () => {
                if (!$scope.material) return;

                var resourceTypes = $scope.material.resourceTypes;
                if (resourceTypes.length == 0) {
                    return 'NONE';
                }
                return resourceTypes[resourceTypes.length - 1].name;
            };

            $scope.confirmMaterialDeletion = () => {
                dialogService.showConfirmationDialog(
                    'MATERIAL_CONFIRM_DELETE_DIALOG_TITLE',
                    'MATERIAL_CONFIRM_DELETE_DIALOG_CONTENT',
                    'ALERT_CONFIRM_POSITIVE',
                    'ALERT_CONFIRM_NEGATIVE',
                    deleteMaterial);
            };

            function deleteMaterial() {
                this.serverCallService
                    .makeDelete('rest/material/'+material.id)
                    .then(() => {
                        this-toastService.showOnRouteChange('MATERIAL_DELETED')
                        this.$scope.material.deleted = true
                        this.$rootScope.learningObjectDeleted = true
                        this.$rootScope.$broadcast('dashboard:adminCountsUpdated')
                    })
            }

            $scope.isUsersMaterial = () => {
                if ($scope.material && authenticatedUserService.isAuthenticated() && !authenticatedUserService.isRestricted()) {
                    var userID = authenticatedUserService.getUser().id;
                    var creator = $scope.material.creator;

                    return creator && creator.id === userID
                }
            };

            $scope.restoreMaterial = () => {
                materialService.restoreMaterial($scope.material)
                    .then(restoreSuccess, restoreFail);
            };

            function restoreSuccess() {
                toastService.show('MATERIAL_RESTORED');
                $scope.material.deleted = false
                $scope.material.improper = false
                $scope.material.unReviewed = false
                $scope.material.broken = false
                $scope.material.changed = false
                $rootScope.learningObjectDeleted = false
                $rootScope.learningObjectImproper = false
                $rootScope.learningObjectUnreviewed = false
                $rootScope.learningObjectBroken = false
                $rootScope.learningObjectChanged = false
                $rootScope.$broadcast('dashboard:adminCountsUpdated');
            }

            function restoreFail() {
                log("Restoring material failed");
            }

            function getTargetGroups() {
                if ($scope.material.targetGroups[0]) {
                    return targetGroupService.getConcentratedLabelByTargetGroups($scope.material.targetGroups);
                }
            }

            $scope.setRecommendation = (recommendation) => {
                if ($scope.material)
                    $scope.material.recommendation = recommendation
            }
        }
    ]);
