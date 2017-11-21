package ee.hm.dop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ee.hm.dop.model.interfaces.IMaterial;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"repositoryIdentifier", "repository"})})
public class Material extends LearningObject implements Searchable, IMaterial {

    @NotNull
    @ManyToMany(fetch = EAGER, cascade = {PERSIST, MERGE})
    @Fetch(FetchMode.SELECT)
    @JoinTable(
            name = "Material_Title",
            joinColumns = {@JoinColumn(name = "material")},
            inverseJoinColumns = {@JoinColumn(name = "title")},
            uniqueConstraints = @UniqueConstraint(columnNames = {"material", "title"}))
    private List<LanguageString> titles;

    @ManyToOne
    @JoinColumn(name = "lang")
    private Language language;

    @ManyToMany(fetch = EAGER, cascade = {PERSIST, MERGE})
    @Fetch(FetchMode.SELECT)
    @JoinTable(
            name = "Material_Author",
            joinColumns = {@JoinColumn(name = "material")},
            inverseJoinColumns = {@JoinColumn(name = "author")},
            uniqueConstraints = @UniqueConstraint(columnNames = {"material", "author"}))
    private List<Author> authors;

    @OneToOne(cascade = {PERSIST, MERGE})
    @JoinColumn(name = "issueDate")
    private IssueDate issueDate;

    @ManyToMany(fetch = EAGER, cascade = {PERSIST, MERGE})
    @Fetch(FetchMode.SELECT)
    @JoinTable(
            name = "Material_Description",
            joinColumns = {@JoinColumn(name = "material")},
            inverseJoinColumns = {@JoinColumn(name = "description")},
            uniqueConstraints = @UniqueConstraint(columnNames = {"material", "description"}))
    private List<LanguageString> descriptions;

    @Column(columnDefinition = "TEXT")
    private String source;

    @ManyToMany(fetch = EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(
            name = "Material_ResourceType",
            joinColumns = {@JoinColumn(name = "material")},
            inverseJoinColumns = {@JoinColumn(name = "resourceType")},
            uniqueConstraints = @UniqueConstraint(columnNames = {"material", "resourceType"}))
    private List<ResourceType> resourceTypes;

    @OneToMany
    @JoinTable(
            name = "Material_PeerReview",
            joinColumns = {@JoinColumn(name = "material")},
            inverseJoinColumns = {@JoinColumn(name = "peerReview")},
            uniqueConstraints = @UniqueConstraint(columnNames = {"material", "peerReview"}))
    private List<PeerReview> peerReviews;

    @ManyToOne
    @JoinColumn(name = "licenseType")
    private LicenseType licenseType;

    @ManyToMany(fetch = EAGER, cascade = {PERSIST, MERGE})
    @Fetch(FetchMode.SELECT)
    @JoinTable(
            name = "Material_Publisher",
            joinColumns = {@JoinColumn(name = "material")},
            inverseJoinColumns = {@JoinColumn(name = "publisher")},
            uniqueConstraints = @UniqueConstraint(columnNames = {"material", "publisher"}))
    private List<Publisher> publishers;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "uploadedFile")
    private UploadedFile uploadedFile;

    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "repository")
    private Repository repository;

    @JsonIgnore
    @Column(nullable = false)
    private boolean curriculumLiterature = false;

    /**
     * The ID in the repository. Null when created in DOP
     */
    @Column
    private String repositoryIdentifier;

    @Column(nullable = false)
    private boolean paid = false;

    @Column(nullable = false)
    private Boolean embeddable = false;

    @Column(nullable = false)
    private boolean isSpecialEducation = false;

    public List<LanguageString> getTitles() {
        return titles;
    }

    public void setTitles(List<LanguageString> titles) {
        this.titles = titles;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public IssueDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(IssueDate issueDate) {
        this.issueDate = issueDate;
    }

    public List<LanguageString> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<LanguageString> descriptions) {
        this.descriptions = getSanitizedHTML(descriptions);
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<ResourceType> getResourceTypes() {
        return resourceTypes;
    }

    public void setResourceTypes(List<ResourceType> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    public List<PeerReview> getPeerReviews() {
        return peerReviews;
    }

    public void setPeerReviews(List<PeerReview> peerReviews) {
        this.peerReviews = peerReviews;
    }

    public LicenseType getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(LicenseType licenseType) {
        this.licenseType = licenseType;
    }

    public List<Publisher> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<Publisher> publishers) {
        this.publishers = publishers;
    }

    public String getRepositoryIdentifier() {
        return repositoryIdentifier;
    }

    public void setRepositoryIdentifier(String repositoryIdentifier) {
        this.repositoryIdentifier = repositoryIdentifier;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setIsPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isEmbeddable() {
        return embeddable != null ? embeddable : false;
    }

    public void setEmbeddable(Boolean embeddable) {
        this.embeddable = embeddable;
    }

    public boolean isSpecialEducation() {
        return isSpecialEducation;
    }

    public void setSpecialEducation(boolean isSpecialEducation) {
        this.isSpecialEducation = isSpecialEducation;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public Boolean getEmbeddable() {
        return embeddable;
    }

    @JsonIgnore
    public void setCurriculumLiterature(boolean curriculumLiterature) {
        this.curriculumLiterature = curriculumLiterature;
    }

    //FIXME: curriculumLiterature variable is supported as a legacy, all new materials need peer reviews
    @JsonProperty
    public Boolean isCurriculumLiterature() {
        return curriculumLiterature || (getPeerReviews() != null && getPeerReviews().size() != 0);
    }

    private List<LanguageString> getSanitizedHTML(List<LanguageString> descriptions) {
        if (descriptions != null) {
            descriptions
                    .stream()
                    .filter(description -> description.getText() != null)
                    .forEach(description -> description.setText(LO_ALLOWED_HTML_TAGS_POLICY.sanitize(description.getText())));
        }

        return descriptions;
    }
}
