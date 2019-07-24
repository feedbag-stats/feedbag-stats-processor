package entity;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="AllEvents")
public class AllEvents extends TaggedInstantBase {

	@Column(nullable=false)
	private String event;
	
	@Column(nullable=false)
	private String version;

	@Column(nullable=false)
	private String payload;
	
	public AllEvents() {}
	
	public AllEvents(Instant i, User u, String event, String version, String payload) {
		super(i, u);
		this.event = event;
		this.version = version;
		this.payload = payload;
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

}
