package entity;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity  
@Table(name = "testingstatetimestamp")
public class TestingStateTimestamp extends TaggedInstantBase{
	
	@Column
	private final boolean isTesting;

	public TestingStateTimestamp(Instant instant, Boolean isTesting, User user) {
		super(instant, user);
		this.isTesting = isTesting;
	}
	
	public boolean isTesting() {
		return isTesting;
	}
	
	public String toString() {
		return "<"+instant+", "+user+", "+user+">";
	}

}
