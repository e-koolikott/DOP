package ee.hm.dop.model.taxon;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

@Entity
@DiscriminatorValue("SUBTOPIC")
public class Subtopic extends Taxon {

    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "topic", nullable = false)
    private Topic topic;

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    @JsonIgnore
    @Override
    public Taxon getParent() {
        return getTopic();
    }

    @JsonIgnore
    @Override
    public Set<? extends Taxon> getChildren() {
        return null;
    }
}
