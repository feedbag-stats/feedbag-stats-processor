package aggregation;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;

import cc.kave.commons.model.events.testrunevents.TestResult;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.model.ssts.IExpression;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;
import cc.kave.commons.model.ssts.impl.expressions.assignable.CompletionExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.InvocationExpression;
import cc.kave.commons.model.ssts.statements.IExpressionStatement;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public class TDDCycleDetector {

	BiFunction<? super Instant, ? super Instant, ? extends Instant> instantMin = (Instant a, Instant b) -> a==null || a.isBefore(b) ? a : b;
	
	//Stores for each file when it was first mentioned and what tests it contained
	private Map<ITypeName,Pair<Instant,ArrayList<IMethodName>>> firstFileOccurrence = new HashMap<>();
	//stores for each test when it was first mentioned
	private Map<IMethodName,Instant> firstTestOccurrence = new HashMap<>();
	//stores for each test when it first passed
	private Map<IMethodName,Instant> firstPass = new HashMap<>();
	//stores for each test when it first failed
	private Map<IMethodName,Instant> firstFail = new HashMap<>();
	
	public void addSST(ISST sst, Instant time) {
		//update firstFileOccurrence if necessary
		Pair<Instant,ArrayList<IMethodName>> oldValue = firstFileOccurrence.get(sst.getEnclosingType());
		if (oldValue==null || oldValue.getLeft().isAfter(time)) {
			ArrayList<IMethodName> containedMethods = new ArrayList<>();
			for (IMethodDeclaration m : sst.getMethods()) {
				containedMethods.add(m.getName());
			}
			firstFileOccurrence.put(sst.getEnclosingType(), new Pair<Instant, ArrayList<IMethodName>>(time, containedMethods));
		}
		
		//update firstMethodOccurrence if necessary
		for(IMethodDeclaration m : sst.getMethods()) {
			if (isTest(m)) {
				firstTestOccurrence.merge(m.getName(), time, instantMin);
			}
		}
	}
	
	public void addTestResult(IMethodName name, Instant startTime, TestResult result) {
		(result.equals(TestResult.Success) ? firstPass : firstFail).merge(name, startTime, instantMin);
	}
	
	public int getMaxConsecutiveCycles() {
		ArrayList<TaggedInstant<Integer>> tempResults = new ArrayList<>();
		
		for (ActivityInterval i : getAllCycles()) {
			tempResults.add(new TaggedInstant<Integer>(i.end(), Math.max(maxUntil(tempResults,i.begin())+1, maxUntil(tempResults,i.end()))));
		}
		
		int maxTag = tempResults.stream().map((i)->i.tag()).max(Integer::max).orElse(0);
		
		return maxTag;
	}
	
	private SortedSet<ActivityInterval> getAllCycles() {
		//a cycle...
		SortedSet<ActivityInterval> cycles = new TreeSet<>(ActivityInterval.END_COMPARATOR);
		
		//...was newly written and...
		ArrayList<IMethodName> addedMethods = getAddedTests();
		for (IMethodName m : addedMethods) {
			if (firstPass.containsKey(m) && firstFail.containsKey(m)) {
				//...first failed and then passed.
				if (firstPass.get(m).isAfter(firstFail.get(m))) {
					//a cycle begins when the test is added
					//and ends when it first passes
					cycles.add(new ActivityInterval(firstTestOccurrence.get(m), firstPass.get(m), null));
				}
			}
		}
		
		return cycles;
	}
	
	//returns an arraylist of all tests that were not contained in any file when it first was logged
	private ArrayList<IMethodName> getAddedTests() {
		ArrayList<IMethodName> newTests = new ArrayList<>();
		for (IMethodName m : firstTestOccurrence.keySet()) {
			Instant firstOccurrence = firstTestOccurrence.get(m);
			boolean testIsNewlyWritten = true;
			for(Pair<Instant,ArrayList<IMethodName>> p : firstFileOccurrence.values()) {
				if (p.getLeft().isAfter(firstOccurrence)) continue;
				if (p.getRight().contains(m)) {
					testIsNewlyWritten = false;
					break;
				}
				if(!testIsNewlyWritten) break;
			}
			if (testIsNewlyWritten) {
				newTests.add(m);
			}
		}
		return newTests;
	}
	
	//assumption: every test calls a NUnit function at some point
	private boolean isTest(IMethodDeclaration method) {
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
				.max(Integer::max)
				.orElse(0);
	}
	
	public String toString() {
		return "TDDCycleDetector[\nfirstFileOccurrence: "+
				firstFileOccurrence.toString()+"\nfirstTestOccurrence: "+
				firstTestOccurrence.toString()+"\nfirstPass: "+
				firstPass.toString()+"\nfirstFail: "+
				firstFail.toString()+"\ncycles: "+
				getMaxConsecutiveCycles()+"]\n";
	}
}
