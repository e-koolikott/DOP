'use strict'

{
class controller extends Controller {
    constructor(...args) {
        super(...args)

        this.getModerators()

        this.$scope.isSubmitButtonEnabled = false
        this.$scope.isDownloadButtonEnabled = false;
        this.$scope.isExpertsSelectVisible = true

        this.$scope.params = {}
        this.$scope.filter = {
            from: moment().subtract(1, 'month').startOf('month').toDate(),
            to: moment().subtract(1, 'month').endOf('month').toDate(),
        }
        this.$scope.isInfoTextOpen = false
        this.$scope.data = {}
        this.$scope.sortBy = 'byEducationalContext'
        this.$scope.sort = this.sort.bind(this)
        this.$scope.paginate = this.paginate.bind(this)
        this.$scope.maxDate = new Date()
        this.$scope.perPage = 100
        this.$scope.page = 1
        this.$scope.numPages = 1
        this.$translate('EXPERT_STATISTICS').then((translation) => this.$rootScope.tabTitle = translation);

        this.$scope.$watch('filter', this.onFilterChange.bind(this), true)
        this.$scope.$watch('params', this.onParamsChange.bind(this), true)
        this.$scope.$watch('educationalContext', this.onEducationalContextChange.bind(this), true)

        // Set the info text height in pixels for css-animatable collapse
        this.setInfoTextHeight = this.setInfoTextHeight.bind(this)
        window.addEventListener('resize', this.setInfoTextHeight)
        this.$scope.$on('$destroy', () => window.removeEventListener('resize', this.setInfoTextHeight))
        setTimeout(this.setInfoTextHeight)
    }
    isModerator() {
        return this.authenticatedUserService.isModerator()
    }
    getModerators() {
        this.serverCallService
            .makeGet('rest/admin/moderator')
            .then(res => {
                this.$scope.moderators = res.data
            })
    }
    getStatistics() {
        this.$scope.fetching = true
        this.serverCallService
            .makePost('rest/admin/statistics', this.getPostParams())
            .then(({ status, data: { rows, sum } }) => {
                this.$scope.fetching = false

                if (200 <= status && status < 300) {
                    this.$scope.allRows = this.getFlattenedRows(rows)
                    this.$scope.data.sum = sum
                    this.$scope.page = 1
                    this.$scope.numPages = Math.ceil(this.$scope.allRows.length / this.$scope.perPage)
                    this.paginate(this.$scope.page, this.$scope.perPage)
                    this.sort(this.$scope.sortBy)
                    this.$scope.isDownloadButtonEnabled = this.$scope.allRows.length > 0;
                }
            })
    }
    downloadStatistics(format) {
        this.$scope.fetchingDownload = true
        this.serverCallService
            .makePost(`rest/admin/statistics/export/`, Object.assign(this.$scope.paramsForDownload, { format }))
            .then(({ status, data: filename }) => {
                this.$scope.fetchingDownload = false

                this.serverCallService
                    .makeGet(`/rest/admin/statistics/export/download/${filename}`)
                    .then(({status, data}) => {
                        const arrayBuffer = this.stringToArrayBuffer(atob(data));
                        const filenameWithFormat = 'statistika_aruanne.' + format;

                        if (200 <= status && status < 300) {
                            let file = new Blob([arrayBuffer]);

                            if (window.navigator && window.navigator.msSaveOrOpenBlob) {
                                window.navigator.msSaveOrOpenBlob(file, filenameWithFormat);
                                return;
                            }
                            let link = document.createElement('a');
                            link.href = window.URL.createObjectURL(file);
                            link.download = filenameWithFormat;
                            document.body.appendChild(link);
                            link.click();
                            document.body.removeChild(link);
                        }
                })
            })
    }
    // function from https://stackoverflow.com/questions/34993292/how-to-save-xlsx-data-to-file-as-a-blob
    stringToArrayBuffer(s) {
        let buf = new ArrayBuffer(s.length);
        let view = new Uint8Array(buf);
        for (let i=0; i!=s.length; ++i) view[i] = s.charCodeAt(i) & 0xFF;
        return buf;
    }

    returnEvent() {
        let event;
        if (typeof(MouseEvent) === 'function') {
            event = new MouseEvent('click');
        } else {
            event = document.createEvent('MouseEvent');
            event.initEvent('click', true, true);
        }
        return event;
    }
    // Copy filter values to POST params with one exception: params.users = [filter.user].
    onFilterChange(filter) {
        const params = Object.assign({}, filter)

        if (params.user) {
            params.users = [params.user]
            delete params.user
        }

        this.$scope.params = params
    }
    onParamsChange({ from, to, users, taxons }) {
        this.$scope.isSubmitButtonEnabled = from && to
            && moment(from).startOf('day').toDate() < moment(to).endOf('day').toDate()
            && (users || taxons)
        this.$scope.isTaxonSelectVisible = !users
    }
    onEducationalContextChange(educationalContext) {
        this.$scope.isExpertsSelectVisible = !educationalContext
        this.$scope.sortBy = educationalContext ? 'byDomainOrSubject' : 'byEducationalContext'
        this.$scope.isDownloadButtonEnabled = false;
        this.onParamsChange({});
    }
    clear() {
        !this.isModerator() ? this.clearAllDataAndFilters() : this.clearDataForModerator();
    }

    clearDataForModerator() {
        this.$scope.filter.from = ''
        this.$scope.filter.to = ''
        this.$scope.data = {}
    }

    clearAllDataAndFilters() {
        this.$scope.filter = {}
        this.$scope.educationalContext = undefined
        this.$scope.data = {}
    }

    onSelectTaxons(taxons) {
        this.$scope.filter.taxons = taxons
    }
    setInfoTextHeight(cb) {
        requestAnimationFrame(() => {
            const infoText = document.querySelector('.statistics-info-text')
            infoText.style.height = infoText.scrollHeight + 'px'
        })
    }
    getPostParams() {
        const params = Object.assign({}, this.$scope.params)
        if (params.from){
            params.from = moment(params.from).startOf('day').toDate()
        }
        if (params.to){
            params.to = moment(params.to).endOf('day').toDate()
        }
        if (params.taxons) {
            params.taxons = params.taxons.map(({ id, level }) => ({ id, level }))
        }

        if (params.users) {
            params.users = params.users.map(({ id }) => ({ id }))
        }
        this.$scope.paramsForDownload = params;
        return params
    }
    getFlattenedRows(rows) {
        return rows.reduce((flattenedRows, educationalContextRow) => {
            const rows = educationalContextRow.rows.reduce((flattenedDomainRows, domainOrSubjectRow) => {
                if (domainOrSubjectRow.domainUsed || domainOrSubjectRow.noUsersFound)
                    flattenedDomainRows.push(domainOrSubjectRow)

                if (domainOrSubjectRow.subjects.length)
                    [].push.apply(flattenedDomainRows, domainOrSubjectRow.subjects)

                return flattenedDomainRows
            }, [])

            ;[].push.apply(flattenedRows, rows)

            return flattenedRows
        }, [])
    }
    sort(order) {
        this.$scope.sortBy = order
        this.sortService.orderItems(this.$scope.allRows, order)
        this.paginate(this.$scope.page, this.$scope.perPage)
    }
    openDownloadMenu($mdMenu, evt) {
        $mdMenu.open(evt)
    }
    toggleInfoText() {
        this.setInfoTextHeight()
        this.$scope.isInfoTextOpen = !this.$scope.isInfoTextOpen
    }
    paginate(page, perPage) {
        const startIdx = (page - 1) * perPage
        this.$scope.page = page
        this.$scope.data.rows = this.$scope.allRows.slice(startIdx, startIdx + perPage)
    }
}
controller.$inject = [
    '$scope',
    '$rootScope',
    '$translate',
    'serverCallService',
    'sortService',
    'authenticatedUserService'
]
angular.module('koolikottApp').controller('statisticsController', controller)
}
