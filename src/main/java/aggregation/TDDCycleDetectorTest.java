package aggregation;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.*;

import cc.kave.commons.model.events.testrunevents.TestResult;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.codeelements.IParameterName;
import cc.kave.commons.model.naming.types.IArrayTypeName;
import cc.kave.commons.model.naming.types.IDelegateTypeName;
import cc.kave.commons.model.naming.types.IPredefinedTypeName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.model.naming.types.ITypeParameterName;
import cc.kave.commons.model.naming.types.organization.IAssemblyName;
import cc.kave.commons.model.naming.types.organization.INamespaceName;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.declarations.IDelegateDeclaration;
import cc.kave.commons.model.ssts.declarations.IEventDeclaration;
import cc.kave.commons.model.ssts.declarations.IFieldDeclaration;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.ssts.declarations.IPropertyDeclaration;
import cc.kave.commons.model.ssts.visitor.ISSTNode;
import cc.kave.commons.model.ssts.visitor.ISSTNodeVisitor;

public class TDDCycleDetectorTest {
	private TDDCycleDetector detector;
	private IMethodName mName = new IMethodName() {
		
		@Override
		public boolean hasParameters() {
			return false;
		}
		
		@Override
		public List<IParameterName> getParameters() {
			return null;
		}
		
		@Override
		public boolean hasTypeParameters() {
			return false;
		}
		
		@Override
		public List<ITypeParameterName> getTypeParameters() {
			return null;
		}
		
		@Override
		public boolean isUnknown() {
			return false;
		}
		
		@Override
		public boolean isHashed() {
			return false;
		}
		
		@Override
		public String getIdentifier() {
			return "methodIdentifier";
		}
		
		@Override
		public boolean isStatic() {
			return false;
		}
		
		@Override
		public ITypeName getValueType() {
			return null;
		}
		
		@Override
		public String getName() {
			return "methodName";
		}
		
		@Override
		public String getFullName() {
			return "methodFullName";
		}
		
		@Override
		public ITypeName getDeclaringType() {
			return null;
		}
		
		@Override
		public boolean isInit() {
			return false;
		}
		
		@Override
		public boolean isExtensionMethod() {
			return false;
		}
		
		@Override
		public boolean isConstructor() {
			return false;
		}
		
		@Override
		public ITypeName getReturnType() {
			return null;
		}
	};
	
	private ISST sst = new ISST() {
		
		@Override
		public Iterable<ISSTNode> getChildren() {
			return null;
		}
		
		@Override
		public <TContext, TReturn> TReturn accept(ISSTNodeVisitor<TContext, TReturn> arg0, TContext arg1) {
			return null;
		}
		
		@Override
		public boolean isPartialClass() {
			return false;
		}
		
		@Override
		public Set<IPropertyDeclaration> getProperties() {
			return null;
		}
		
		@Override
		public String getPartialClassIdentifier() {
			return null;
		}
		
		@Override
		public Set<IMethodDeclaration> getNonEntryPoints() {
			return null;
		}
		
		@Override
		public Set<IMethodDeclaration> getMethods() {
			HashSet<IMethodDeclaration> set = new HashSet<>();
			set.add(new IMethodDeclaration() {
				
				@Override
				public Iterable<ISSTNode> getChildren() {
					return null;
				}
				
				@Override
				public <TContext, TReturn> TReturn accept(ISSTNodeVisitor<TContext, TReturn> arg0, TContext arg1) {
					return null;
				}
				
				@Override
				public boolean isEntryPoint() {
					return false;
				}
				
				@Override
				public IMethodName getName() {
					return mName;
				}
				
				@Override
				public List<IStatement> getBody() {
					return null;
				}
			});
			return set;
		}
		
		@Override
		public Set<IFieldDeclaration> getFields() {
			return null;
		}
		
		@Override
		public Set<IEventDeclaration> getEvents() {
			return null;
		}
		
		@Override
		public Set<IMethodDeclaration> getEntryPoints() {
			return null;
		}
		
		@Override
		public ITypeName getEnclosingType() {
			return new ITypeName() {
				
				@Override
				public int compareTo(ITypeName o) {
					return 0;
				}
				
				@Override
				public boolean isUnknown() {
					return false;
				}
				
				@Override
				public boolean isHashed() {
					return false;
				}
				
				@Override
				public String getIdentifier() {
					return null;
				}
				
				@Override
				public boolean hasTypeParameters() {
					return false;
				}
				
				@Override
				public List<ITypeParameterName> getTypeParameters() {
					return null;
				}
				
				@Override
				public boolean isVoidType() {
					return false;
				}
				
				@Override
				public boolean isValueType() {
					return false;
				}
				
				@Override
				public boolean isTypeParameter() {
					return false;
				}
				
				@Override
				public boolean isStructType() {
					return false;
				}
				
				@Override
				public boolean isSimpleType() {
					return false;
				}
				
				@Override
				public boolean isReferenceType() {
					return false;
				}
				
				@Override
				public boolean isPredefined() {
					return false;
				}
				
				@Override
				public boolean isNullableType() {
					return false;
				}
				
				@Override
				public boolean isNestedType() {
					return false;
				}
				
				@Override
				public boolean isInterfaceType() {
					return false;
				}
				
				@Override
				public boolean isEnumType() {
					return false;
				}
				
				@Override
				public boolean isDelegateType() {
					return false;
				}
				
				@Override
				public boolean isClassType() {
					return false;
				}
				
				@Override
				public boolean isArray() {
					return false;
				}
				
				@Override
				public INamespaceName getNamespace() {
					return null;
				}
				
				@Override
				public String getName() {
					return "nameTest";
				}
				
				@Override
				public String getFullName() {
					return "fullNameTest";
				}
				
				@Override
				public ITypeName getDeclaringType() {
					return null;
				}
				
				@Override
				public IAssemblyName getAssembly() {
					return null;
				}
				
				@Override
				public ITypeParameterName asTypeParameterName() {
					return null;
				}
				
				@Override
				public IPredefinedTypeName asPredefinedTypeName() {
					return null;
				}
				
				@Override
				public IDelegateTypeName asDelegateTypeName() {
					return null;
				}
				
				@Override
				public IArrayTypeName asArrayTypeName() {
					return null;
				}
			};
		}
		
		@Override
		public Set<IDelegateDeclaration> getDelegates() {
			return null;
		}
	};
	
	@Before
	public void setup() {
		detector = new TDDCycleDetector();
	}
	
	@Test
	public void basicCycle() {
		
		detector.addEditEvent(sst, Instant.ofEpochMilli(1));
		detector.addTestResult(mName, Instant.ofEpochMilli(2), TestResult.Failed);
		detector.addTestResult(mName, Instant.ofEpochMilli(3), TestResult.Success);
		
		assertEquals(1, detector.getMaxConsecutiveCycles());
	}
	
	@Test
	public void doubleCycle() {
		
		detector.addEditEvent(sst, Instant.ofEpochMilli(1));
		detector.addTestResult(mName, Instant.ofEpochMilli(2), TestResult.Failed);
		detector.addTestResult(mName, Instant.ofEpochMilli(3), TestResult.Success);

		detector.addEditEvent(sst, Instant.ofEpochMilli(4));
		detector.addTestResult(mName, Instant.ofEpochMilli(5), TestResult.Failed);
		detector.addTestResult(mName, Instant.ofEpochMilli(6), TestResult.Success);
		
		
		assertEquals(2, detector.getMaxConsecutiveCycles());
	}
	
	@Test
	public void noCycle() {
		
		detector.addEditEvent(sst, Instant.ofEpochMilli(1));
		detector.addTestResult(mName, Instant.ofEpochMilli(2), TestResult.Failed);
		detector.addEditEvent(sst, Instant.ofEpochMilli(3));
		detector.addTestResult(mName, Instant.ofEpochMilli(4), TestResult.Success);
		
		assertEquals(0, detector.getMaxConsecutiveCycles());
	}
}


