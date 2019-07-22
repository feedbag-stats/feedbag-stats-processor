package entity.activity;

import entity.ActivityType;
import entity.TaggedInstantBase;
import entity.User;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="ActivityEntry")
public class ActivityEntry extends TaggedInstantBase{

    @Column(nullable = false)
    private ActivityType type;

    public ActivityEntry() {}

    public ActivityEntry(Instant instant, ActivityType type, User user) {
    	super(instant, user);
        this.type = type;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }
}
