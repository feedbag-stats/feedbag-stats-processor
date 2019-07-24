package entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "EventTimestamp")
public class EventTimeStamp extends TaggedInstantBase {

    @Column
    private final String name;

    public EventTimeStamp(Instant instant, String name, User user) {
        super(instant, user);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return "<" + instant + ", " + name + ", " + user + ">";
    }
}
