package entity;

import java.time.Instant;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.codeelements.IParameterName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.model.naming.types.ITypeParameterName;

@Entity
@Table(name="testresulttimestamp")
public class TestResultTimestamp extends TaggedInstantBase{
	
	@Column
	private final String methodName;
	
	@Column
	private final String declaringType;

	@Column
	private final boolean pass;

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
	
	public boolean pass() {
		return pass;
	}
	
	public String toString() {
		return "<"+instant.toString()+", "+user+", "+methodName+", "+pass+">";
	}
	
}
