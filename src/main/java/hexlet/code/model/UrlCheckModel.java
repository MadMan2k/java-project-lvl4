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
@Table(name = "url_checks")
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

    public UrlCheckModel(int statusCode, String title, String h1, String description) {
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
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
     * Current date for save()
     */
    @PrePersist
    void createdAt() {
        this.createdAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getH1() {
        return h1;
    }

    public void setH1(String h1) {
        this.h1 = h1;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UrlModel getUrlModel() {
        return urlModel;
    }

    public void setUrlModel(UrlModel urlModel) {
        this.urlModel = urlModel;
    }

    @Override
    public String toString() {
        return "UrlCheckModel{" +
                "id=" + id +
                ", statusCode=" + statusCode +
                ", title='" + title + '\'' +
                ", h1='" + h1 + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

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
