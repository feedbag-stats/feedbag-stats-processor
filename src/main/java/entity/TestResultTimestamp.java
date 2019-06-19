package entity;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import cc.kave.commons.model.naming.codeelements.IMethodName;

@Entity
@Table(name="testresulttimestamp")
public class TestResultTimestamp extends TaggedInstantBase{
	
	@Column
	private final IMethodName methodName;

	@Column
	private final boolean pass;

	public TestResultTimestamp(Instant instant, IMethodName methodName, Boolean pass, User user) {
		super(instant, user);
		this.methodName = methodName;
		this.pass = pass;
	}
	
	public IMethodName methodName() {
		return methodName;
	}
	
	public boolean pass() {
		return pass;
	}
	
	public String toString() {
		return "<"+instant.toString()+", "+user+", "+methodName+", "+pass+">";
	}
	
}
