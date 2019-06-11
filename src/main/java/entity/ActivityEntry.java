package entity;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class ActivityEntry implements Serializable {

    public static String[] CATEGORIES = {"Reading", "Writing", "Testing", "Debugging"};

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private int begin;

    @Column(nullable = false)
    private int end;

    @Column(nullable = false)
    private String type;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonIgnore
    private User user;

    public ActivityEntry() {
    }

    public ActivityEntry(int begin, int end, String type, User user) {
        this.begin = begin;
        this.end = end;
        this.type = type;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Adds a duration
     *
     * @param duration
     */
    public void addDuration(int duration) {
        this.setEnd(this.getEnd() + duration);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
