package entity.various;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Table;

import entity.TaggedInstantBase;
import entity.User;

@Entity
@Table(name="BuildTimestamp")
public class BuildTimestamp extends TaggedInstantBase {
	
	private long duration;

	public BuildTimestamp() {}

	public BuildTimestamp(Instant instant, User user, long dur) {
		super(instant, user);
		duration = dur;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

}
