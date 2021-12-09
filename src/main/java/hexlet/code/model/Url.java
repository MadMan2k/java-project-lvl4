package hexlet.code.model;

import io.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import java.util.Date;

@Entity
public class Url extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Lob
    @Column(name = "name")
    private String name;

    @Column(name = "created_at")
    private Date createdAt;

    public Url(String s) {
        this.name = s;
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
        this.createdAt = new Date();
    }


}
