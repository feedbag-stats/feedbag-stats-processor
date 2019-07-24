package entity.activity;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import entity.TaggedInstantBase;
import entity.User;

@Entity  
@Table(name = "TestingstateTimestamp")
public class TestingStateTimestamp extends TaggedInstantBase{
	
	@Column
	private boolean isTesting;
	
	public TestingStateTimestamp() {}

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
