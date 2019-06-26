package aggregation.tdd;

import cc.kave.commons.model.naming.codeelements.IMethodName;

public class MethodIdentifier {
	private final String declaringType;
	private final String methodName;
	
	public MethodIdentifier(IMethodName mname) {
		this(mname.getDeclaringType().toString(), mname.getName().toString());
	}
	
	public MethodIdentifier(String declaringType, String methodName) {
		this.declaringType = declaringType;
		this.methodName = methodName;
	}

	public String getDeclaringType() {
		return declaringType;
	}

	public String getMethodName() {
		return methodName;
	}
	
	public String getIdentifier() {
		return declaringType + methodName;
	}
}
