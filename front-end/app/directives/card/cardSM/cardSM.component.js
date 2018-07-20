'use strict'

{
class controller extends Controller {
    $onInit() {
        this.selected = false
        this.domains = []
        this.subjects = []

        this.domainSubjectList = this.taxonGroupingService.getDomainSubjectList(this.learningObject.taxons)

        this.targetGroups = this.targetGroupService
            .getConcentratedLabelByTargetGroups(this.learningObject.targetGroups || [])

        this.$scope.learningObject = this.learningObject
    }
    $doCheck() {
        if (this.learningObject !== this.$scope.learningObject) this.$scope.learningObject = this.learningObject
    }
    navigateTo() {
        const { type } = this.learningObject
        if (type === '.Material') this.storageService.setMaterial(this.learningObject)
        else if (type === '.Portfolio') this.storageService.setPortfolio(this.learningObject)
    }
    formatName(name) {
        if (name) return this.formatNameToInitials(name.trim())
    }
    formatSurname(surname) {
        if (surname) return this.formatSurnameToInitialsButLast(surname.trim())
    }
    isAuthenticated() {
        const authenticated =
            this.authenticatedUserService.getUser() &&
            !this.authenticatedUserService.isRestricted() &&
            !this.$rootScope.isEditPortfolioPage

        if (!authenticated && this.isMaterial(this.learningObject.type)) this.learningObject.selected = false

        return authenticated
    }
    hoverEnter() {
        this.cardHover = true
    }
    hoverLeave() {
        this.cardHover = false
    }
}
controller.$inject = [
    '$window',
    '$scope',
    '$location',
    '$rootScope',
    'translationService',
    'authenticatedUserService',
    'targetGroupService',
    'storageService',
    'taxonGroupingService'
]
component('dopCardSm', {
    bindings: {
        learningObject: '=',
        chapter: '=?'
    },
    templateUrl: 'directives/card/cardSM/cardSM.html',
    controller
})
}
