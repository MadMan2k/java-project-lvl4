package hexlet.code.model;

import io.ebean.Model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "URLs")
public class UrlModel extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

//    @Lob
    /* LAZY by default */
//    @Basic(fetch = FetchType.EAGER)
    @Column(name = "name")
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "response_code")
    private int responseCode;

    public UrlModel() {
    }

    public UrlModel(String inputName) {
        this.name = inputName;
    }

    /**
     * Save with current date.
     */
    @Override
    public void save() {
        createdAt();
        super.save();
    }

    /**
     * Current date for save().
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
     * @param inputId
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
     * @param inputName
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
     * @param createdAtNow
     */
    public void setCreatedAt(LocalDateTime createdAtNow) {
        this.createdAt = createdAtNow;
    }

    /**
     * @return http response code
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * @param httpResponseCode
     */
    public void setResponseCode(int httpResponseCode) {
        this.responseCode = httpResponseCode;
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
                + ", responseCode=" + responseCode
                + '}';
    }
}
