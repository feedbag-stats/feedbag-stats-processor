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
	
	public AllEvents() {}
	
	public AllEvents(Instant i, User u, String text) {
		super(i, u);
		event = text;
	}
	
	public String getEvent() {
		return event;
	}

}
