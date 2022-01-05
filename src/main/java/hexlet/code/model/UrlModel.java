package hexlet.code.model;

import io.ebean.Model;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "url")
public class UrlModel extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "urlModel")
    private List<UrlCheckModel> urlChecks;

    public UrlModel() {
    }

    public UrlModel(String inputName) {
        createdAt();
        this.name = inputName;
    }

    /**
     * Current date for constructor.
     */
    @PrePersist
    void createdAt() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * @return id
     */
    public long getId() {
        return id;
    }

    /**
     * @param inputId input ID
     */
    public void setId(long inputId) {
        this.id = inputId;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @param inputName input URL
     */
    public void setName(String inputName) {
        this.name = inputName;
    }

    /**
     * @return actual date
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAtNow creation time
     */
    public void setCreatedAt(LocalDateTime createdAtNow) {
        this.createdAt = createdAtNow;
    }

    /**
     * @return list of checks
     */
    public List<UrlCheckModel> getUrlChecks() {
        return urlChecks;
    }

    /**
     * @param urlChecksOfUrlModel finished checks
     */
    public void setUrlChecks(List<UrlCheckModel> urlChecksOfUrlModel) {
        this.urlChecks = urlChecksOfUrlModel;
    }

    /**
     * Add urlCheckModel to urlModel and vice-versa.
     * @param urlCheckModel
     */
    public void addCheckToUrl(UrlCheckModel urlCheckModel) {
        if (urlChecks == null) {
            urlChecks = new ArrayList<>();
        }

        urlChecks.add(urlCheckModel);
        urlCheckModel.setUrlModel(this);
    }

    /**
     * @return full String
     */
    @Override
    public String toString() {
        return "UrlModel{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", createdAt=" + createdAt
                + '}';
    }
}
