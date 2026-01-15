package mybookstore.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tools.dynamia.domain.Descriptor;
import tools.dynamia.domain.contraints.NotEmpty;
import tools.dynamia.domain.jpa.SimpleEntity;

@Entity
@Table(name = "tags")
@Descriptor(fields = "tag")
public class CategoryTag extends SimpleEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;
    @NotEmpty
    private String tag;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return tag;
    }
}
