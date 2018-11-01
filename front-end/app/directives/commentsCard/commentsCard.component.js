'use strict'

{
const COMMENTS_PER_PAGE = 5
const SHOW_COMMENT_REPORT_MODAL_HASH = 'dialog-report-comment'

class controller extends Controller {
    $onChanges({ learningObject } = {}) {
        if (learningObject && learningObject.currentValue)
            this.$scope.comments = learningObject.currentValue.comments
    }
    $onInit() {
        this.user = this.authenticatedUserService.getUser()
        this.visibleCommentsCount = COMMENTS_PER_PAGE
        this.$scope.newComment = { text: '' }

        // Commentbox hotfix
        setTimeout(() =>
            angular
                .element(document.getElementById('comment-list'))
                .find('textarea')
                .css('height', '112px'),
            1000
        )

        // auto-launch the report dialog upon login or page load if hash is found in location URL
        this.$timeout(() =>
            window.location.hash.includes(SHOW_COMMENT_REPORT_MODAL_HASH)
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
            window.location.hash.includes(SHOW_COMMENT_REPORT_MODAL_HASH) &&
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
    isAuthorized() {
        return this.authenticatedUserService.isAuthenticated()
            && !this.authenticatedUserService.isRestricted()
    }
    getLoadMoreCommentsLabel() {
        let commentsLeft = this.getLeftCommentsCount()

        return commentsLeft <= COMMENTS_PER_PAGE
            ? `(${commentsLeft})`
            : `(${COMMENTS_PER_PAGE}/${commentsLeft})`
    }
    showMoreComments() {
        let commentsLeft = this.getLeftCommentsCount()

        commentsLeft - COMMENTS_PER_PAGE >= 0
            ? this.visibleCommentsCount += COMMENTS_PER_PAGE
            : this.visibleCommentsCount = this.learningObject.comments.length
    }
    showMoreCommentsButton() {
        return this.getLeftCommentsCount() > 0
    }
    addComment() {
        if (this.$scope.newComment.text && typeof this.submitClick === 'function') {
            // setting up POST body
            const { id, type } = this.learningObject
            const body = {
                comment: this.$scope.newComment,
                learningObject: { id, type }
            }

            // optimistically adding new comment to $scope.comments
            const added = new Date()
            this.$scope.comments.unshift(
                Object.assign({
                    creator: this.user,
                    id: added.getTime(), // timestamp as mock id
                    added
                }, body.comment)
            )
            // undo optimism
            const undo = () => {
                const id = added.getTime()
                const idx = this.$scope.comments.findIndex(c => c.id === id)
                if (idx > -1)
                    this.$scope.comments.splice(idx, 1)
            }

            this.serverCallService
                .makePost('rest/comment', body)
                .then(({status, data}) => {
                    if (status < 200 || 300 <= status) {
                        undo()
                    } else {
                        this.$scope.newComment.text = ''
                        if (this.isPortfolio(response.data)) {
                            (this.storageService.setPortfolio(response.data))
                        } else {
                            this.storageService.setMaterial(response.data), undo
                        }
                    }
                })
        }
    }
    reportComment(evt) {
        this.authenticatedUserService.isAuthenticated()
            ? this.showReportDialog(evt)
            : this.showLoginDialog(evt)
    }
    showReportDialog(targetEvent) {
        const { serverCallService, learningObject } = this

        return this.$mdDialog
            .show({
                controller: ['$scope', '$mdDialog', 'title', function ($scope, $mdDialog, title){
                    $scope.title = title
                    $scope.data = {
                        learningObject,
                        reportingText: ''
                    }
                    $scope.cancel = () => {
                        $scope.data.reportingText = ''
                        $mdDialog.cancel()
                    }
                    $scope.sendReport = () => $mdDialog.hide($scope)
                    $scope.loading = true
                    $scope.submitEnabled = true

                    serverCallService
                        .makeGet('rest/learningMaterialMetadata/commentReportingReasons')
                        .then(({ data: reasons }) => {
                            if (Array.isArray(reasons)) {
                                $scope.hideReasons = reasons.length === 1
                                $scope.reasons = reasons.map(key => ({
                                    key,
                                    checked: reasons.length === 1
                                }))
                            }
                            $scope.loading = false
                        })

                    $scope.characters = { used: 0, remaining: 255 }
                    $scope.$watch('data.reportingText', (newValue) => {
                        const used = newValue ? newValue.length : 0
                        $scope.characters = { used, remaining: 255 - used }
                    })
                }],
                templateUrl: 'directives/report/improper/improper.dialog.html',
                clickOutsideToClose: true,
                escapeToClose: true,
                targetEvent,
                locals: {
                    title: this.$translate.instant('COMMENT_TOOLTIP_REPORT_AS_IMPROPER')
                }
            })
            .then(({ data, reasons }) => {
                Object.assign(data, {
                    reportingReasons: reasons.reduce((reportingReasons, r) =>
                        r.checked
                            ? reportingReasons.concat({ reason: r.key })
                            : reportingReasons,
                        []
                    )
                })
                return this.serverCallService
                    .makePut('rest/impropers', data)
                    .then(({ status, data }) => {
                        if (status === 200) {
                            this.$rootScope.learningObjectImproper = true
                            this.$rootScope.$broadcast('errorMessage:reported')
                            this.toastService.show('TOAST_NOTIFICATION_SENT_TO_ADMIN')
                        }
                    })
            })
    }
    showLoginDialog(targetEvent) {
        this.addHash()
        this.loginDialog = this.$mdDialog.show({
            templateUrl: 'views/loginDialog/loginDialog.html',
            controller: 'loginDialogController',
            bindToController: true,
            locals: {
                title: this.$translate.instant('LOGIN_MUST_LOG_IN_TO_REPORT_IMPROPER')
            },
            clickOutsideToClose: true,
            escapeToClose: true,
            targetEvent
        })
        .catch(this.removeHash)
    }
    addHash() {
        window.history.replaceState(null, null,
            ('' + window.location).split('#')[0] + '#' + SHOW_COMMENT_REPORT_MODAL_HASH
        )
    }
    removeHash() {
        window.history.replaceState(null, null,
            ('' + window.location).split('#')[0]
        )
    }
    getLeftCommentsCount() {
        return Array.isArray(this.learningObject.comments)
            ? this.learningObject.comments.length - this.visibleCommentsCount
            : 0
    }
}
controller.$inject = [
    '$scope',
    '$rootScope',
    '$mdDialog',
    '$translate',
    '$timeout',
    'authenticatedUserService',
    'serverCallService',
    'toastService',
    'storageService'
]
component('dopCommentsCard', {
    bindings: {
        learningObject: '<',
        isOpen: '<',
        submitClick: '&'
    },
    templateUrl: 'directives/commentsCard/commentsCard.html',
    controller
})
}
