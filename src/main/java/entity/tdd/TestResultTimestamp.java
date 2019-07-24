package entity.tdd;

import java.util.Date;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import cc.kave.commons.model.naming.codeelements.IMethodName;
import entity.TaggedInstantBase;
import entity.User;

@Entity
@Table(name="TestResultTimestamp")
public class TestResultTimestamp extends TaggedInstantBase{
	
	@Column
	private String methodName;
	
	@Column
	private String declaringType;

	@Column
	private boolean pass;
	
	public TestResultTimestamp() {}

	public TestResultTimestamp(Instant instant, IMethodName methodName, Boolean pass, User user) {
		super(instant, user);
		this.methodName = methodName.getName().toString();
		this.declaringType = methodName.getDeclaringType().toString();
		this.pass = pass;
	}
	
	public String getIdentifier() {
		return declaringType+methodName;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public String getDeclaringType() {
		return declaringType;
	}
	
	public Date getDate() {
		return Date.from(instant);
	}
	
	public boolean pass() {
		return pass;
	}
	
	public String toString() {
		return "<"+instant.toString()+", "+user+", "+methodName+", "+pass+">";
	}
	
}
