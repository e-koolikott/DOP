'use strict'

{
const SHOW_GENERAL_REPORT_MODAL_HASH = 'dialog-report-general'

class controller extends Controller {
    $onInit() {
        this.$scope.data = {
            reportingReasons: [],
            reportingText: '',
            learningObject: this.$scope.learningObject
        }
        this.$scope.$watch('learningObject', (newValue) => {
            if (newValue)
                this.$scope.data = Object.assign({}, this.$scope.data, {
                    learningObject: newValue
                })
        })
        this.$scope.reasons = this.getReasons()
        this.$scope.showDialog = (evt) => {
            this.authenticatedUserService.isAuthenticated()
                ? this.showReportDialog(evt)
                : this.showLoginDialog(evt)

            if(this.isPortfolio(this.$scope.learningObject)){
                gTagCaptureEvent('report', 'teaching portfolio')
            } else {
                gTagCaptureEvent('report', 'teaching material')
            }
        }

        // auto-launch the report dialog upon login or page load if hash is found in location URL
        this.$timeout(() =>
            window.location.hash.includes(SHOW_GENERAL_REPORT_MODAL_HASH)
                ? this.onLoginSuccess()
                : this.unsubscribeLoginSuccess = this.$rootScope.$on('login:success', this.onLoginSuccess.bind(this))
        )
    }
    $onDestroy() {
        if (typeof this.unsubscribeLoginSuccess === 'function')
            this.unsubscribeLoginSuccess()
    }
    onLoginSuccess() {
        if (
            window.location.hash.includes(SHOW_GENERAL_REPORT_MODAL_HASH) &&
            this.authenticatedUserService.isAuthenticated()
        ) {
            this.removeHash()
            !this.loginDialog
                ? this.showReportDialog()
                : this.loginDialog.then(() => {
                    this.showReportDialog()
                    delete this.loginDialog
                })
        }
    }

    showReportDialog(targetEvent) {
        this.$scope.reasons.then(reasons =>
            this.$mdDialog
                .show({
                    controller: ['$scope', '$mdDialog', 'data', 'reasons', 'title', function ($scope, $mdDialog, data, reasons, title) {
                        $scope.title = title
                        $scope.data = data
                        $scope.reasons = reasons
                        $scope.cancel = () => {
                            reasons.forEach(reason => reason.checked = false)
                            data.reportingReasons = []
                            data.reportingText = ''
                            $mdDialog.cancel()
                        }
                        $scope.sendReport = () => {
                            if (data.reportingReasons.length)
                                return $mdDialog.hide()

                            $scope.errors = { reasonRequired: true }
                            $scope.submitEnabled = false
                        }
                        $scope.$watch('reasons', (newValue) => {
                            if (Array.isArray(newValue)) {
                                $scope.anyChecked = false

                                $scope.data.reportingReasons = newValue.reduce((reportingReasons, r) =>
                                    r.checked
                                        ? ($scope.anyChecked = true) && reportingReasons.concat({ reason: r.key })
                                        : reportingReasons,
                                    []
                                )

                                if ($scope.anyChecked)
                                    $scope.errors = null
                            }
                        }, true)

                        $scope.characters = { used: 0, remaining: 255 }
                        $scope.$watch('data.reportingText', (newValue) => {
                            const used = newValue ? newValue.length : 0
                            $scope.characters = { used, remaining: 255 - used }
                        })
                        $scope.$watch('[anyChecked, characters.used]', (newValues) => {
                            if (newValues[0] && newValues[1] > 4) {
                                $scope.submitEnabled = true
                            } else {
                                $scope.submitEnabled = false
                            }
                        });
                    }],
                    templateUrl: '/directives/report/improper/improper.dialog.html',
                    clickOutsideToClose: true,
                    escapeToClose: true,
                    targetEvent,
                    locals: {
                        title: this.$translate.instant('REPORT_IMPROPER_TITLE'),
                        data: this.$scope.data,
                        reasons
                    }
                })
                .then(this.sendReport.bind(this))
        )
    }
    showLoginDialog(targetEvent) {
        this.$rootScope.showLocationDialog = false

        this.addHash()

        setTimeout(() => {
            this.showLoginDialogAfterDelay(targetEvent);
        }, 600)
    }

    showLoginDialogAfterDelay(targetEvent) {
        loginDialogController.$inject.push('title')
        this.loginDialog = this.$mdDialog.show({
            templateUrl: '/views/loginDialog/loginDialog.html',
            controller: loginDialogController,
            bindToController: true,
            locals: {
                title: this.$translate.instant('LOGIN_MUST_LOG_IN_TO_REPORT_IMPROPER')
            },
            clickOutsideToClose: true,
            escapeToClose: true,
            targetEvent
        })
            .catch(this.removeHash)

        setTimeout(() =>
            setTimeout(() =>
                loginDialogController.$inject.pop()
            )
        )
    }

    addHash() {
        window.history.replaceState(null, null,
            ('' + window.location).split('#')[0] + '#' + SHOW_GENERAL_REPORT_MODAL_HASH
        )
    }
    removeHash() {
        window.history.replaceState(null, null,
            ('' + window.location).split('#')[0]
        )
    }
    getReasons() {
        return this.serverCallService
            .makeGet('rest/learningMaterialMetadata/learningObjectReportingReasons')
            .then(({ data: reasons }) =>
                (reasons || []).map(key => ({
                    key,
                    checked: false
                }))
            )
    }
    sendReport() {
        const data = Object.assign({}, this.$scope.data)
        this.serverCallService
            .makePut('rest/impropers', data)
            .then(({ status }) => {
                if (status == 200) {
                    this.$scope.reasons.then(reasons =>
                        reasons.forEach(reason => reason.checked = false)
                    )
                    this.$scope.data.reportingReasons = []
                    this.$scope.data.reportingText = ''
                    this.$rootScope.learningObjectImproper = true
                    this.$rootScope.$broadcast('errorMessage:reported')
                    this.toastService.show('TOAST_NOTIFICATION_SENT_TO_ADMIN')
                }
            })
    }
    isAdminOrModerator() {
        return this.authenticatedUserService.isAdmin() || this.authenticatedUserService.isModerator()
    }
}
controller.$inject = [
    '$scope',
    '$rootScope',
    '$mdDialog',
    '$translate',
    '$timeout',
    'toastService',
    'serverCallService',
    'authenticatedUserService'
]
/**
 * Declaring this as a directive since we need to use it as an attribute on
 * <md-menu-item> (component usage is restricted to element tagname only).
 */
directive('dopReportImproper', {
    scope: {
        learningObject: '<'
    },
    templateUrl: '/directives/report/improper/improper.html',
    controller
})
}
