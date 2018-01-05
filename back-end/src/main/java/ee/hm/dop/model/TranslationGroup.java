package ee.hm.dop.model;

import javax.persistence.*;
import java.util.Map;

import static javax.persistence.FetchType.EAGER;

@Entity
public class TranslationGroup implements AbstractEntity {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "lang", nullable = false)
    private Language language;

    @ElementCollection(fetch = EAGER)
    @CollectionTable(name = "Translation", uniqueConstraints = @UniqueConstraint(columnNames = {"translationGroup",
            "translationKey"}), joinColumns = @JoinColumn(name = "translationGroup"))
    @MapKeyColumn(name = "translationKey")
    @Column(name = "translation", nullable = false)
    private Map<String, String> translations;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Map<String, String> getTranslations() {
        return translations;
    }

    public void setTranslations(Map<String, String> translations) {
        this.translations = translations;
    }
}
