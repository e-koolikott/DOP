package ee.hm.dop.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

import static ee.hm.dop.model.LearningObject.LO_ALLOWED_HTML_TAGS_POLICY;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

@Entity
public class Chapter implements AbstractEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String title;

    @ManyToMany(fetch = EAGER, cascade = ALL)
    @Fetch(FetchMode.SELECT)
    @OrderColumn(name = "rowOrder", nullable = false)
    @JoinTable(
            name = "Chapter_ChapterBlock",
            joinColumns = {@JoinColumn(name = "chapter")},
            inverseJoinColumns = {@JoinColumn(name = "chapterBlock")},
            uniqueConstraints = @UniqueConstraint(columnNames = {"chapter", "chapterBlock", "rowOrder"}))
    private List<ChapterBlock> blocks;

    @Deprecated
    @Column(columnDefinition = "TEXT", name = "textValue")
    private String text;

    @Deprecated
    @ManyToMany(fetch = EAGER, cascade = ALL)
    @Fetch(FetchMode.SELECT)
    @OrderColumn(name = "rowOrder", nullable = false)
    @JoinTable(
            name = "Chapter_Row",
            joinColumns = {@JoinColumn(name = "chapter")},
            inverseJoinColumns = {@JoinColumn(name = "row")},
            uniqueConstraints = @UniqueConstraint(columnNames = {"chapter", "row", "rowOrder"}))
    private List<ContentRow> contentRows;

    @Deprecated
    @OneToMany(fetch = EAGER, cascade = ALL)
    @JoinColumn(name = "parentChapter")
    private List<Chapter> subchapters;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        if (text != null) {
            text = LO_ALLOWED_HTML_TAGS_POLICY.sanitize(text);
        }
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return title;
    }

    public List<Chapter> getSubchapters() {
        return subchapters;
    }

    public void setSubchapters(List<Chapter> subchapters) {
        this.subchapters = subchapters;
    }

    public List<ContentRow> getContentRows() {
        return contentRows;
    }

    public void setContentRows(List<ContentRow> rows) {
        this.contentRows = rows;
    }

    public List<ChapterBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<ChapterBlock> blocks) {
        this.blocks = blocks;
    }
}
