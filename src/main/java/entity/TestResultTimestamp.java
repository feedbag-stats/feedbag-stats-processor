package entity;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import cc.kave.commons.model.naming.codeelements.IMethodName;

@Entity
@Table(name="testresulttimestamp")
public class TestResultTimestamp extends TaggedInstant<Boolean>{
	
	@Column
	private final IMethodName methodName;

	public TestResultTimestamp(Instant instant, IMethodName methodName, Boolean pass, User user) {
		super(instant, pass, user);
		this.methodName = methodName;
	}
	
	public IMethodName methodName() {
		return methodName;
	}
	
}
