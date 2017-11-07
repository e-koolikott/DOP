'use strict'

{
class controller extends Controller {
    $onInit() {
        this.$scope.$watch(
            () => this.storageService.getPortfolio(),
            (portfolio) => this.$scope.portfolio = portfolio
        )

        if (this.$location.hash()) {
            const unsubscribe = this.$scope.$watch(
                () => document.getElementById(this.$location.hash()),
                (newValue) => {
                    if (newValue != null)
                        this.$timeout(() => {
                            this.goToElement(this.$location.hash())
                            unsubscribe()
                        })
                })
        }
    }
    gotoChapter(evt, chapterId, subchapterId) {
        evt.preventDefault()
        this.goToElement(
            subchapterId != null
                ? 'chapter-' + chapterId + '-' + subchapterId
                : 'chapter-' + chapterId
        )
    }
    goToElement(elementID) {
        const $chapter = angular.element(document.getElementById(elementID))
        this.$document.scrollToElement($chapter, 60, 200)
    }
    closeSidenav(id) {
        if (window.innerWidth < BREAK_LG)
            this.$mdSidenav(id).close()
    }
}
controller.$inject = [
    '$scope',
    '$mdSidenav',
    '$document',
    '$location',
    '$timeout',
    'storageService'
]
component('dopTableOfContents', {
    templateUrl: 'directives/tableOfContents/tableOfContents.html',
    controller
})
}
