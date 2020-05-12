'use strict'

{
class controller extends Controller {
    $onInit() {
        this.getFavoriteData()
    }
    $onChanges({ learningObject }) {
        // When asynchronous learningObject request finishes after component init
        if (learningObject && learningObject.currentValue !== learningObject.previousValue)
            this.getFavoriteData()
    }
    getFavoriteData() {
        if (this.learningObject && this.authenticatedUserService.isAuthenticated()) {
            if (this.learningObject.favorite)
                this.hasFavorited = true
            else if (this.learningObject.favorite == null)
                this.serverCallService
                    .makeGet('rest/learningObject/favorite', { 'id': this.learningObject.id })
                    .then(({ data }) => {
                        if (data && data.id)
                            this.hasFavorited = true
                    })
            else this.hasFavorited = false
        }
    }
    favorite($event) {
        $event.preventDefault()
        $event.stopPropagation()

        const makePost = ({ id, type }) =>
            this.serverCallService
                .makePost('rest/learningObject/favorite', { id, type })
                .then(response => {
                    if (200 <= status && status < 300) {
                        this.toastService.show('ADDED_TO_FAVORITES')
                        this.hasFavorited = true
                    }
                }, () =>
                    this.hasFavorited = false
                )

        if (this.authenticatedUserService.isAuthenticated()) {
            this.isPortfolio(this.learningObject)
                ? this.portfolioService.getPortfolioById(this.learningObject.id).then(makePost)
                : this.isMaterial(this.learningObject)
                    && this.materialService.getMaterialById(this.learningObject.id).then(makePost)

            this.hasFavorited = true

            if(this.isPortfolio(this.learningObject)){
                gTagCaptureEvent('bookmark', 'teaching portfolio')
            } else if (this.isMaterial(this.learningObject)){
                gTagCaptureEvent('bookmark', 'teaching material')
            }
        }
    }
    removeFavorite($event) {
        $event.preventDefault()
        $event.stopPropagation()

        const { id, type } = this.learningObject

        if (this.hasFavorited && this.authenticatedUserService.isAuthenticated()) {
            this.serverCallService
                .makeDelete('rest/learningObject/favorite', { id, type })
                .then(({ status }) => {
                    if (200 <= status && status < 300) {
                        this.toastService.show('REMOVED_FROM_FAVORITES')
                        this.hasFavorited = false
                    }
                }, () =>
                    this.hasFavorited = true
                )

            this.hasFavorited = false
        }
    }
}
controller.$inject = [
    '$rootScope',
    'serverCallService',
    'authenticatedUserService',
    'toastService',
    'materialService',
    'portfolioService',
    '$location',
]
component('dopFavorite', {
    bindings: {
        learningObject: '<',
        hover: '<'
    },
    templateUrl: '/directives/favorite/favorite.html',
    controller
})
}
