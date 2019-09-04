'use strict'

{
class controller extends Controller {
    constructor(...args) {
        super(...args)

        this.$scope.forms = {}
        this.$scope.mobileId = { idCode: '', phoneNumber: '' }
        this.$scope.title = this.title
            ? this.title
            : this.$translate.instant('LOGIN_CHOOSE_LOGIN_METHOD')

        this.unsubscribeRouteChangeSuccess = this.$rootScope.$on('$routeChangeSuccess', () => this.$mdDialog.hide())
        this.$scope.$watch(
            () => this.authenticatedUserService.isAuthenticated(),
            (newValue, oldValue) => newValue == true && this.$mdDialog.hide(),
            false
        )

        this.$scope.hideLogin = () => {
            this.$rootScope.sidenavLogin = null
            this.$mdDialog.hide()
        }
        this.$scope.idCardAuth = () => {
            this.$rootScope.authenticationOption = 'idCard'
            this.$scope.loginButtonFlag = true
            this.authenticationService.loginWithIdCard()
        }
        // this.$scope.taatAuth = () => this.authenticationService.loginWithTaat()
        this.$scope.ekoolAuth = () => {
            this.$rootScope.authenticationOption = 'ekool'
            this.authenticationService.loginWithEkool()
        }
        this.$scope.stuudiumAuth = () => {
            this.$rootScope.authenticationOption = 'stuudium'
            this.authenticationService.loginWithStuudium()
        }
        this.$scope.haridAuth= () => {
            this.$rootScope.authenticationOption = 'harID'
            this.authenticationService.loginWithHarid()
        }
        this.$scope.mobileIdAuth = () => {
            this.$rootScope.authenticationOption = 'mID'
            if (!this.validatorsBound) {
                this.bindValidators()
                this.$scope.forms.mobileIdForm.$$controls.forEach(c => c.$validate())
            }
            if (this.$scope.forms.mobileIdForm.$valid)
                this.authenticationService.loginWithMobileId(
                    this.$scope.mobileId.phoneNumber.trim().replace(/^372/, ''),
                    this.$scope.mobileId.idCode,
                    this.translationService.getLanguage(),
                    () => {
                        this.$scope.mobileIdChallenge = null
                        this.$scope.mobileId.idCode = null
                        this.$scope.mobileId.phoneNumber = null
                        this.$scope.hideLogin()
                    },
                    () =>
                        this.$scope.mobileIdChallenge = null,
                    (challenge) =>
                        this.$scope.mobileIdChallenge = challenge
                )
        }
    }
    $onDestroy() {
        if (typeof this.unsubscribeRouteChangeSuccess === 'function')
            this.unsubscribeRouteChangeSuccess()
    }
    bindValidators() {
        this.validatorsBound = true
        this.$scope.forms.mobileIdForm.idCode.$validators.required = (value) => !!value
        this.$scope.forms.mobileIdForm.idCode.$validators.validEstonianIdCode = this.validateIdCode.bind(this)
        this.$scope.forms.mobileIdForm.phoneNumber.$validators.required = (value) => !!value
        this.$scope.forms.mobileIdForm.phoneNumber.$validators.validEstonianPhoneNumber = this.validatePhoneNumber.bind(this)
    }
    validateIdCode(modelValue, viewValue) {
        if (!modelValue || modelValue.length !== 11)
            return false

        let controlCode, firstSum = 0
        const firstWeights = [1, 2, 3, 4, 5, 6, 7, 8, 9, 1]
        const secondWeights = [3, 4, 5, 6, 7, 8, 9, 1, 2, 3]

        for (let i = 0; i < 10; i++)
            firstSum += modelValue.charAt(i) * firstWeights[i]

        if (firstSum % 11 !== 10)
            controlCode = firstSum % 11
        else {
            // Calculate second sum using second set of weights
            let secondSum = 0

            for (let i = 0; i < 10; i++)
                secondSum += modelValue.charAt(i) * secondWeights[i]

            controlCode = secondSum % 11 !== 10
                ? secondSum % 11
                : 0
        }

        return modelValue[10] == controlCode
    }
    validatePhoneNumber(modelValue, viewValue) {
        // no country code whatsoever is fine
        if (!modelValue.startsWith('+') && !modelValue.startsWith('00'))
            return true

        return modelValue.startsWith('+372') || modelValue.startsWith('00372')
    }
}
controller.$inject = [
    '$scope',
    '$rootScope',
    '$translate',
    '$mdDialog',
    'authenticationService',
    'translationService',
    'authenticatedUserService'
]

window.loginDialogController = controller

angular.module('koolikottApp').controller('loginDialogController', controller)
}
