package ee.hm.dop.model;


import ee.hm.dop.model.interfaces.IMaterial;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.FetchType.EAGER;

@Entity
@Table(name = "Material")
public class ReducedMaterial extends ReducedLearningObject implements IMaterial {

    @ManyToMany(fetch = EAGER, cascade = {PERSIST, MERGE})
    @Fetch(FetchMode.SELECT)
    @JoinTable(
            name = "Material_Title",
            joinColumns = {@JoinColumn(name = "material")},
            inverseJoinColumns = {@JoinColumn(name = "title")},
            uniqueConstraints = @UniqueConstraint(columnNames = {"material", "title"}))
    private List<LanguageString> titles;

    @ManyToMany(fetch = EAGER, cascade = {PERSIST, MERGE})
    @Fetch(FetchMode.SELECT)
    @JoinTable(
            name = "Material_Author",
            joinColumns = {@JoinColumn(name = "material")},
            inverseJoinColumns = {@JoinColumn(name = "author")},
            uniqueConstraints = @UniqueConstraint(columnNames = {"material", "author"}))
    private List<Author> authors;

    @ManyToMany(fetch = EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinTable(
            name = "Material_ResourceType",
            joinColumns = {@JoinColumn(name = "material")},
            inverseJoinColumns = {@JoinColumn(name = "resourceType")},
            uniqueConstraints = @UniqueConstraint(columnNames = {"material", "resourceType"}))
    private List<ResourceType> resourceTypes;

    @Override
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

    public List<ResourceType> getResourceTypes() {
        return resourceTypes;
    }

    public void setResourceTypes(List<ResourceType> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }
}
