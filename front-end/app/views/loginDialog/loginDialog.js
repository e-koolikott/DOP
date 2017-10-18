'use strict'

angular.module('koolikottApp').controller('loginDialogController', [
'$scope', '$mdDialog', 'authenticationService', '$location', 'translationService', '$rootScope', 'authenticatedUserService', '$translate',
function($scope, $mdDialog, authenticationService, $location, translationService, $rootScope, authenticatedUserService, $translate) {
    $scope.mobileId = {};
    $scope.idCardFlag = false;
    $scope.validation = {
        error: {}
    };

    $scope.title = this.title
        ? this.title
        : $translate.instant('LOGIN_CHOOSE_LOGIN_METHOD')

    $scope.$watch(function() {
        return authenticatedUserService.isAuthenticated()
    }, function(newValue, oldValue) {
        if (newValue == true) {
            $mdDialog.hide();
        }
    }, false);

    $rootScope.$on('$routeChangeSuccess', function() {
        $mdDialog.hide();
    });

    $scope.hideLogin = function() {
        $rootScope.sidenavLogin = null;
        $mdDialog.hide();
    }

    $scope.idCardAuth = function() {
        $scope.loginButtonFlag = true;
        authenticationService.loginWithIdCard();
    };

    $scope.taatAuth = function() {
        authenticationService.loginWithTaat();
    };

    $scope.ekoolAuth = function() {
        authenticationService.loginWithEkool();
    };

    $scope.stuudiumAuth = function() {
        authenticationService.loginWithStuudium();
    };

    $scope.mobileIdAuth = function() {
        var idCodeValid = validateIdCode();
        var phoneNumberValid = validatePhoneNumber();

        if (idCodeValid && phoneNumberValid) {
            var language = translationService.getLanguage();
            authenticationService.loginWithMobileId($scope.mobileId.phoneNumber, $scope.mobileId.idCode, language,
                mobileIdSuccess, mobileIdFail, mobileIdReceiveChallenge);
            }
        };

        function mobileIdSuccess() {
            $scope.mobileIdChallenge = null;
            $scope.mobileId.idCode = null;
            $scope.mobileId.phoneNumber = null;
            $scope.hideLogin();
        }

        function mobileIdFail() {
            $scope.mobileIdChallenge = null;
        }

        function mobileIdReceiveChallenge(challenge) {
            $scope.mobileIdChallenge = challenge;
        }

        function validateIdCode() {
            $scope.validation.error.idCode = null;

            var isValid = false;

            if (isEmpty($scope.mobileId.idCode)) {
                $scope.validation.error.idCode = "required";
            } else {
                isValid = isIdCodeValid($scope.mobileId.idCode);

                if (!isValid) {
                    $scope.validation.error.idCode = "invalid";
                }
            }

            return isValid;
        }

        function validatePhoneNumber() {
            $scope.validation.error.phoneNumber = null;

            var isValid = false;

            if (isEmpty($scope.mobileId.phoneNumber)) {
                $scope.validation.error.phoneNumber = "required";
            } else {
                isValid = isPhoneNumberEstonian($scope.mobileId.phoneNumber);

                if (!isValid) {
                    $scope.validation.error.phoneNumber = "notEstonian";
                }
            }

            return isValid;
        }

        function isPhoneNumberEstonian(phoneNumber) {
            return !phoneNumber.startsWith("+") || phoneNumber.startsWith("+372");
        }
    }
]);
