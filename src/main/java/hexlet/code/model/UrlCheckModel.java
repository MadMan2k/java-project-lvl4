package hexlet.code.model;

import io.ebean.Model;
import org.jetbrains.annotations.NotNull;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "url_check")
public class UrlCheckModel extends Model implements Comparable<UrlCheckModel> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "status_code")
    private int statusCode;

    @Column(name = "title")
    private String title;

    @Column(name = "h1")
    private String h1;


    @Lob
    @Column(name = "description")
    private String description;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "url_id")
    private UrlModel urlModel;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public UrlCheckModel() {
    }

    public UrlCheckModel(int httpResponseCode, String titleContent, String h1Content, String descriptionContent) {
        createdAt();
        this.statusCode = httpResponseCode;
        this.title = titleContent;
        this.h1 = h1Content;
        this.description = descriptionContent;
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
     * @param newId
     */
    public void setId(long newId) {
        this.id = newId;
    }

    /**
     * @return http response status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @param httpResponseCode
     */
    public void setStatusCode(int httpResponseCode) {
        this.statusCode = httpResponseCode;
    }

    /**
     * @return content in html tag 'title'
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param titleContent
     */
    public void setTitle(String titleContent) {
        this.title = titleContent;
    }

    /**
     * @return content in first html tag 'h1'
     */
    public String getH1() {
        return h1;
    }

    /**
     * @param h1Content
     */
    public void setH1(String h1Content) {
        this.h1 = h1Content;
    }

    /**
     * @return content in html tag 'meta name="description" content='...''
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param descriptionContent
     */
    public void setDescription(String descriptionContent) {
        this.description = descriptionContent;
    }

    /**
     * @return return creation date
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @param creationDate
     */
    public void setCreatedAt(LocalDateTime creationDate) {
        this.createdAt = creationDate;
    }

    /**
     * @return urlModel
     */
    public UrlModel getUrlModel() {
        return urlModel;
    }

    /**
     * @param urlModelOfCheck
     */
    public void setUrlModel(UrlModel urlModelOfCheck) {
        this.urlModel = urlModelOfCheck;
    }

    /**
     * @return
     */
    @Override
    public String toString() {
        return "UrlCheckModel{"
                + "id=" + id
                + ", statusCode=" + statusCode
                + ", title='" + title + '\''
                + ", h1='" + h1 + '\''
                + ", description='" + description + '\''
                + ", createdAt=" + createdAt
                + '}';
    }

    /**
     * Logic of two urlCheck comparison.
     * @param another urlCheckModel to compare
     * @return result of comparison
     */
    @Override
    public int compareTo(@NotNull UrlCheckModel another) {
        if (this.id == another.id) {
            return 0;
        } else if (this.id < another.id) {
            return 1;
        } else {
            return -1;
        }
    }
}
