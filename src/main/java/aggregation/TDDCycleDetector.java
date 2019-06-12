package aggregation;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

//Detects edit-fail-pass-cycles for tests
public class TDDCycleDetector {
	
	//stores for each testing file when it was edited
	private Map<IMethodName,ArrayList<Instant>> methodEdits = new HashMap<>();
	//stores for each test when it passed
	private Map<IMethodName,ArrayList<Instant>> passes = new HashMap<>();
	//stores for each test when it failed
	private Map<IMethodName,ArrayList<Instant>> fails = new HashMap<>();
	
	public void addEditEvent(ISST sst, Instant time) {
		if(isTestFile(sst)) {
			for (IMethodDeclaration m : sst.getMethods()) {
				if(!methodEdits.containsKey(m.getName())) {
					methodEdits.put(m.getName(), new ArrayList<>());
				}
				methodEdits.get(m.getName()).add(time);
			}
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
	
	public void addTestResult(IMethodName name, Instant startTime, TestResult result) {
		Map<IMethodName,ArrayList<Instant>> map = result.equals(TestResult.Success) ? passes : fails;
		if(!map.containsKey(name)) {
			map.put(name, new ArrayList<>());
		}
		map.get(name).add(startTime);
	}
	
	public int getMaxConsecutiveCycles() {
		ArrayList<TaggedInstant<Integer>> tempResults = new ArrayList<>();
		
		for (ActivityInterval i : getAllCycles()) {
			tempResults.add(new TaggedInstant<Integer>(i.end(), Math.max(maxUntil(tempResults,i.begin())+1, maxUntil(tempResults,i.end()))));
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
		ArrayList<TaggedInstant<IMethodName>> startingPoints = new ArrayList<>();
		for(IMethodName name : methodEdits.keySet()) {
			for(Instant i : methodEdits.get(name)) {
				startingPoints.add(new TaggedInstant<IMethodName>(i, name));
			}
		}
		
		for(TaggedInstant<IMethodName> tagged : startingPoints) {
			//...is followed by a failing test...
			Instant fail = firstInstantAfter(fails.get(tagged.tag()), tagged.instant());
			if(fail == null) continue; //no fail found afterwards. no cycle
			//...which in turn is followed by a succeeding test...
			Instant pass = firstInstantAfter(passes.get(tagged.tag()), fail);
			if(pass == null) continue; //no pass found afterwards. no cycle
			//...and has no edit even in between.
			if(!containsInstantBetween(methodEdits.get(tagged.tag()), tagged.instant(), pass)) {
				cycles.add(new ActivityInterval(tagged.instant(), pass, null));
			}
		}
		
		return cycles;
	}
	
	private Instant firstInstantAfter(Collection<Instant> instants, Instant startingPoint) {
		if(instants==null) return null;
		Instant first = null;
		for(Instant i : instants) {
			if(i.isAfter(startingPoint)) {
				if(first==null || i.isBefore(first)) {
					first = i;
				}
			}
		}
		return first;
	}
	
	private boolean containsInstantBetween(Collection<Instant> instants, Instant begin, Instant end) {
		for(Instant i : instants) {
			if(i.isAfter(begin) && i.isBefore(end)) return true;
		}
		return false;
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
	
	private int maxUntil(ArrayList<TaggedInstant<Integer>> list, Instant maxTime) {
		return list.stream()
				.filter(((i)->(i.instant().isBefore(maxTime) || i.instant().equals(maxTime))))
				.map((i)->i.tag())
				.reduce(Integer::max)
				.orElse(0);
	}
	
	public String toString() {
		return "TDDCycleDetector[\nmethodEdits: "+
				methodEdits.toString()+"\npass: "+
				passes.toString()+"\nfail: "+
				fails.toString()+"\nall cycles:"+
				getAllCycles().toString()+"\nconsecutive cycles: "+
				getMaxConsecutiveCycles()+"]\n";
	}
}
