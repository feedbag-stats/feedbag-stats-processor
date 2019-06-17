package entity;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity  
@Table(name = "testingstatetimestamp")
public class TestingStateTimestamp extends TaggedInstant<Boolean>{

	public TestingStateTimestamp(Instant instant, Boolean isTesting, User user) {
		super(instant, isTesting, user);
	}

}
