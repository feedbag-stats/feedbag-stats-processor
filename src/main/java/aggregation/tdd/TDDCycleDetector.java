package aggregation.tdd;

import java.util.Set;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import aggregation.TaggedInstantGeneric;
import cc.kave.commons.model.events.testrunevents.TestResult;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.ssts.IExpression;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.ssts.impl.expressions.assignable.CompletionExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.InvocationExpression;
import cc.kave.commons.model.ssts.statements.IExpressionStatement;
import entity.ActivityInterval;
import entity.BaseInterval;
import entity.FileEditTimestamp;
import entity.TestResultTimestamp;
import entity.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;

//Detects edit-fail-pass-cycles for tests
public class TDDCycleDetector {
	
	private final User user;
	
	//stores for each testing file when it was edited
	private Set<FileEditTimestamp> fileEdits = new HashSet<>();
	private Set<TestResultTimestamp> testresults = new HashSet<>();
	
	public TDDCycleDetector(User user) {
		this.user = user;
	}
	
	public void addEditEvents(Collection<FileEditTimestamp> edits) {
		fileEdits.addAll(edits);
	}
	
	public void addEditEvent(ISST sst, Instant time) {
		if(isTestFile(sst)) {
			fileEdits.add(new FileEditTimestamp(time, sst.getEnclosingType().toString(), user));
		}
	}
	
	private boolean isTestFile(ISST sst) {
		if (sst.getEnclosingType().getFullName().endsWith("Test")
		 || sst.getEnclosingType().getFullName().endsWith("Tests")) {
			return true;
		}
		for (IMethodDeclaration m : sst.getMethods()) {
			if(callsNUnit(m)) {
				return true;
			}
		}
		return false;
	}
	
	public void addTestResults(Collection<TestResultTimestamp> results) {
		testresults.addAll(results);
	}
	
	public void addTestResult(IMethodName name, Instant startTime, TestResult result) {
		testresults.add(new TestResultTimestamp(startTime, name, result.equals(TestResult.Success), user));
	}
	
	public int getMaxConsecutiveCycles() {
		ArrayList<TaggedInstantGeneric<Integer>> tempResults = new ArrayList<>();
		
		for (ActivityInterval i : getAllCycles()) {
			tempResults.add(new TaggedInstantGeneric<Integer>(i.end(), Math.max(maxUntil(tempResults,i.begin())+1, maxUntil(tempResults,i.end()))));
		}
		
		int maxTag = tempResults.stream()
				.map((i)->i.tag())
				.reduce(Integer::max)
				.orElse(0);
		
		return maxTag;
	}
	
	private SortedSet<ActivityInterval> getAllCycles() {
		//a cycle...
		SortedSet<ActivityInterval> cycles = new TreeSet<>(BaseInterval.BEGIN_COMPARATOR);
		
		//...starts with an edited test method...
		ArrayList<TaggedInstantGeneric<MethodIdentifier>> startingPoints = new ArrayList<>();
		for(FileEditTimestamp t : fileEdits) {
			for(TestResultTimestamp r : testresults) {
				if(t.filename().equals(r.getDeclaringType())) {
					startingPoints.add(new TaggedInstantGeneric<MethodIdentifier>(t.instant(), new MethodIdentifier(r.getDeclaringType(), r.getMethodName())));
				}
			}
		}
		
		for(TaggedInstantGeneric<MethodIdentifier> t : startingPoints) {
			//...is followed by a failing test...
			Instant fail = firstTestResultAfter(false, t.tag(), t.instant());
			if(fail == null) continue; //no fail found afterwards. no cycle
			//...which in turn is followed by a succeeding test...
			Instant pass = firstTestResultAfter(true, t.tag(), fail);
			if(pass == null) continue; //no pass found afterwards. no cycle
			//...and has no edit even in between.
			if(!wasEditedBetween(t.tag(), t.instant(), pass)) {
				cycles.add(new ActivityInterval(t.instant(), pass, null, user));
			}
		}
		
		return cycles;
	}
	
	private Instant firstTestResultAfter(boolean pass, MethodIdentifier methodIdentifier, Instant startingPoint) {
		Instant first = null;
		for(TestResultTimestamp t : testresults.stream().filter(t->t.pass()==pass && t.getIdentifier().equals(methodIdentifier.getIdentifier())).collect(Collectors.toSet())) {
			if(t.instant().isAfter(startingPoint)) {
				if(first==null || t.instant().isBefore(first)) {
					first = t.instant();
				}
			}
		}
		return first;
	}
	
	private boolean wasEditedBetween(MethodIdentifier declaringType, Instant begin, Instant end) {
		long editCount = fileEdits.stream()
			.filter(t->t.filename().equals(declaringType.getDeclaringType())) //file was edited
			.filter(t->t.instant().isAfter(begin))
			.filter(t->t.instant().isBefore(end)) 
			.count();
		return editCount > 0;
	}
	
	//assumption: every test calls a NUnit function at some point
	private boolean callsNUnit(IMethodDeclaration method) {
		for (IStatement s : method.getBody()) {
			try {
				if (s instanceof IExpressionStatement) {
					IExpression e = ((IExpressionStatement) s).getExpression();
					if (e instanceof InvocationExpression) {
						if (((InvocationExpression)e).getMethodName().getFullName().contains("NUnit")) return true;
					} else if (e instanceof CompletionExpression) {
						if (((CompletionExpression)e).getTypeReference().getFullName().contains("NUnit")) return true;
					}
				}
			} catch (Exception e) {
				// could not get name, therefore we assume it's no test
			}
		}
		return false;
	}
	
	private int maxUntil(ArrayList<TaggedInstantGeneric<Integer>> list, Instant maxTime) {
		return list.stream()
				.filter(((i)->(i.instant().isBefore(maxTime) || i.instant().equals(maxTime))))
				.map((i)->i.tag())
				.reduce(Integer::max)
				.orElse(0);
	}
	
	public String toString() {
		return "TDDCycleDetector[\nmethodEdits: "+
				fileEdits.toString()+"\ntestresults: "+
				testresults.toString()+"\nall cycles:"+
				getAllCycles().toString()+"\nconsecutive cycles: "+
				getMaxConsecutiveCycles()+"]\n";
	}
}
