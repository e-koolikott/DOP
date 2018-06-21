package ee.hm.dop.model;

import ee.hm.dop.model.enums.Visibility;
import ee.hm.dop.model.taxon.Taxon;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

public class SearchFilter {

    private List<Taxon> taxon;
    private boolean paid = true;
    private String type;
    private Language language;
    private List<TargetGroup> targetGroups;
    private ResourceType resourceType;
    private boolean isSpecialEducation;
    private Integer issuedFrom;
    private List<CrossCurricularTheme> crossCurricularThemes;
    private List<KeyCompetence> keyCompetences;
    private List<Visibility> visibility;
    private boolean isCurriculumLiterature;
    private String sort;
    private SortDirection sortDirection;
    private boolean myPrivates;
    private boolean recommended;
    private boolean favorites;
    private Long creator;
    private User requestingUser;
    private String searchType = "AND";
    private List<Long> excluded;
    private boolean grouped = false;

    public boolean isGrouped() {
        return grouped;
    }

    public void setGrouped(boolean grouped) {
        this.grouped = grouped;
    }

    public boolean isEmptySearch() {
        return isEmpty(taxon) &&
                paid &&
                StringUtils.isNotBlank(type) && type.equals("all") &&
                language == null &&
                isEmpty(targetGroups) &&
                resourceType == null &&
                !isSpecialEducation &&
                issuedFrom == null &&
                isEmpty(crossCurricularThemes) &&
                isEmpty(keyCompetences) &&
                !isCurriculumLiterature &&
                creator == null && !recommended && !favorites;
    }

    public List<Long> getExcluded() {
        return excluded;
    }

    public void setExcluded(List<Long> excluded) {
        this.excluded = excluded;
    }

    public List<Taxon> getTaxons() {
        return taxon;
    }

    public void setTaxons(List<Taxon> taxon) {
        this.taxon = taxon;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public List<TargetGroup> getTargetGroups() {
        return targetGroups;
    }

    public void setTargetGroups(List<TargetGroup> targetGroups) {
        this.targetGroups = targetGroups;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public boolean isSpecialEducation() {
        return isSpecialEducation;
    }

    public void setSpecialEducation(boolean isSpecialEducation) {
        this.isSpecialEducation = isSpecialEducation;
    }

    public Integer getIssuedFrom() {
        return issuedFrom;
    }

    public void setIssuedFrom(Integer issuedFrom) {
        this.issuedFrom = issuedFrom;
    }

    public List<CrossCurricularTheme> getCrossCurricularThemes() {
        return crossCurricularThemes;
    }

    public void setCrossCurricularThemes(List<CrossCurricularTheme> crossCurricularThemes) {
        this.crossCurricularThemes = crossCurricularThemes;
    }

    public List<KeyCompetence> getKeyCompetences() {
        return keyCompetences;
    }

    public void setKeyCompetences(List<KeyCompetence> keyCompetences) {
        this.keyCompetences = keyCompetences;
    }

    public List<Visibility> getVisibility() {
        return visibility;
    }

    public void setVisibility(List<Visibility> visibility) {
        this.visibility = visibility;
    }

    public boolean isCurriculumLiterature() {
        return isCurriculumLiterature;
    }

    public void setCurriculumLiterature(boolean isCurriculumLiterature) {
        this.isCurriculumLiterature = isCurriculumLiterature;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

    public boolean getMyPrivates() {
        return myPrivates;
    }

    public void setMyPrivates(boolean myPrivates) {
        this.myPrivates = myPrivates;
    }

    public Long getCreator() {
        return creator;
    }

    public void setCreator(Long creator) {
        this.creator = creator;
    }

    public User getRequestingUser() {
        return requestingUser;
    }

    public void setRequestingUser(User requestingUser) {
        this.requestingUser = requestingUser;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    public boolean isFavorites() {
        return favorites;
    }

    public void setFavorites(boolean favorites) {
        this.favorites = favorites;
    }

    public enum SortDirection {
        ASCENDING("asc"), DESCENDING("desc");

        private String direction;

        SortDirection(String direction) {
            this.direction = direction;
        }

        public static SortDirection getByValue(String value) {
            for (SortDirection sortDirection : SortDirection.values()) {
                if (sortDirection.getValue().equalsIgnoreCase(value)) return sortDirection;
            }
            return null;
        }

        public String getValue() {
            return direction;
        }
    }

}
