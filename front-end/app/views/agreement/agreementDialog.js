'use strict';

{
    class controller extends Controller {
        constructor(...args) {
            super(...args)

            this.$scope.validEmail = VALID_EMAIL
            this.$scope.isSaving = false
            this.$scope.termsLink = '/kasutustingimused'
            this.$scope.licensesLink = '/isikuandmete-tootlemine'

            this.unsubscribeRouteChangeSuccess = this.$rootScope.$on('$routeChangeSuccess', () => this.$mdDialog.hide())
            this.$scope.$watch(
                () => this.authenticatedUserService.isAuthenticated(),
                (newValue, oldValue) => newValue === true && this.$mdDialog.hide(),
                false
            );

            this.$scope.$watch(
                () => {
                    this.$scope.notAgreedToTermsAgreement = this.$rootScope.statusForDuplicateCheck.termsAgreement;
                    this.$scope.notAgreedToPersonalDataAgreement = this.$rootScope.statusForDuplicateCheck.personalDataAgreement;
                }
            )

            this.$scope.$watch('agreementDialogEmail', () => {
                if (!this.$rootScope.userHasEmailOnLogin)
                    this.$scope.gdprDialogContent.email.$setValidity('validationError', true)
            })

            this.$scope.$watch(() => {
                if (this.isSubmitDisabled()) {
                    this.$scope.showTooltip = true;
                }
            })

            this.$scope.agree = () => {
                if (this.$rootScope.userHasEmailOnLogin) {
                    this.$mdDialog.hide(true)
                } else {
                    this.handleLoginWithNoEmail();
                }
            }

            this.$scope.cancel = () => {
                this.$scope.showTooltip = false
                this.$mdDialog.hide()
            }
        }

        handleLoginWithNoEmail() {
            this.$scope.isSaving = true
            this.$scope.gdprDialogContent.email.$setValidity('validationError', true)
            this.$rootScope.email = this.$scope.gdprDialogContent.email.$viewValue
            this.userEmailService.checkDuplicateEmail(this.$scope.agreementDialogEmail, this.$rootScope.statusForDuplicateCheck)
                .then(response => {
                    if (response.status = 200) {
                        this.$mdDialog.hide(true)
                        this.$scope.isSaving = false
                    }
                }).catch(() => {
                this.$scope.gdprDialogContent.email.$setValidity('validationError', false)
                this.$scope.isSaving = false
            })
        }

        $onDestroy() {
            if (typeof this.unsubscribeRouteChangeSuccess === 'function')
                this.unsubscribeRouteChangeSuccess()
        }

        notAgreedToTerms() {
            return (this.$scope.notAgreedToTermsAgreement && this.$scope.notAgreedToPersonalDataAgreement) ? (!this.$scope.termsAgreed || !this.$scope.licensesAgreed) :
                this.$scope.notAgreedToTermsAgreement ? !this.$scope.termsAgreed :
                    this.$scope.notAgreedToPersonalDataAgreement ? !this.$scope.licensesAgreed : false
        }

        isSubmitDisabled() {
            if (!this.$rootScope.userHasEmailOnLogin) {
                const {email, pattern} = this.$scope.gdprDialogContent.email.$error
                return !this.$scope.gdprDialogContent.email.$viewValue || email || pattern || this.$scope.isSaving || this.notAgreedToTerms()
            }
            return this.notAgreedToTerms()
        }

        onDivAction() {
            this.$scope.showTooltip = true
        }

        onDivLeave() {
            this.$scope.showTooltip = false
        }
    }

    controller.$inject = [
        '$scope',
        '$rootScope',
        '$mdDialog',
        'authenticatedUserService',
        'userEmailService',
        'authenticationService',
        'serverCallService'
    ]

    angular.module('koolikottApp').controller('agreementDialogController', controller)
}
