package entity;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name="ActivityEntry")
public class ActivityEntry extends TaggedInstantBase {

	@Column(nullable=false)
	private String event;
	
	@Column(nullable=false)
	private String version;

	@Column(nullable=false)
	@Type(type="text")
	private String payload;
	
	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private ActivityType type;
	
	public ActivityEntry() {}
	
	public ActivityEntry(Instant i, User u, String event, String version, String payload, ActivityType type) {
		super(i, u);
		this.event = event;
		this.version = version;
		this.payload = payload;
		this.type = type;
	}
	
	public String getEvent() {
		return event;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public ActivityType getType() {
		return type;
	}

	public void setType(ActivityType type) {
		this.type = type;
	}

}
