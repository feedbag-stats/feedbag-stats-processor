package aggregation;

import java.time.Instant;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

public class IntervalBuilder {
	
	private SortedSet<ActivityInterval> intervals = new TreeSet<>(ActivityInterval.COMPARATOR);
	private SortedSet<FlaggedInstant> testingState = new TreeSet<FlaggedInstant>(FlaggedInstant.COMPARATOR);
	
	//adds a flagged instant to build the testing intervals later
	public void add(Instant i, boolean testingStatus) {
		testingState.add(new FlaggedInstant(i, testingStatus));
	}
	
	//adds an instant to add to an existing interval or create a new interval
	public void add(Instant i, ActivityType type) {
		//try to fit activity to any existing interval
		boolean matchedAnyInterval = false;
		for(ActivityInterval p : intervals) {
			
			//only match with intervals of the same type
			if(!p.getType().equals(type)) continue;
			
			//see if interval extends boundaries
			if(i.isAfter(p.end()) && (i.isBefore(p.end().plus(type.timeoutDuration())) || i.equals(p.end().plus(type.timeoutDuration())))) {
				//extends end time
				p.setEnd(i);
				matchedAnyInterval = true;
			} else if(i.isBefore(p.begin()) && (i.isAfter(p.begin().minus(type.timeoutDuration())) || i.equals(p.begin().minus(type.timeoutDuration())))) {
				//extends begin time
				p.setBegin(i);
				matchedAnyInterval = true;
			} else if ((i.isAfter(p.begin()) || i.equals(p.begin())) && (i.isBefore(p.end()) || i.equals(p.end()))) {
				//falls within existing interval
				matchedAnyInterval = true;
			}
		}
		
		//if no nearby interval was found, create a new interval
		if(!matchedAnyInterval) {
			intervals.add(new ActivityInterval(i, i, type));
		}
	}
	
	public void add(ActivityInterval interval) {
		intervals.add(interval);
	}
	
	public SortedSet<ActivityInterval> getIntervals() {
		SortedSet<ActivityInterval> intervals = getLongEnoughIntervals();
		intervals.addAll(getTestingIntervals());
		return intervals;
	}

	private SortedSet<ActivityInterval> getLongEnoughIntervals() {
		SortedSet<ActivityInterval> result = new TreeSet<>(ActivityInterval.COMPARATOR);
		fuseIntervals();
		for(ActivityInterval interval : intervals) {
			if(interval.end().minus(interval.getType().minDisplayedDuration()).isAfter(interval.begin())) {
				result.add(interval);
			}
		}
		return result;
	}

	//merges intervals that should extend each other, deletes duplicates
	private void fuseIntervals() {
		SortedSet<ActivityInterval> fusedIntervals = new TreeSet<>(ActivityInterval.COMPARATOR);
		SortedSet<ActivityInterval> dedupedIntervals = new TreeSet<>(ActivityInterval.COMPARATOR);
		
		for(ActivityInterval p : intervals) {
			
			//ignore intervals that are already entirely contained
			boolean isInsideExistingInterval = false;
			for(ActivityInterval f : fusedIntervals) {
				if(!f.getType().equals(p.getType())) continue;
				if(isInside(f, p)) isInsideExistingInterval = true;
			}
			if(isInsideExistingInterval) continue;
			
			fusedIntervals.add(p);
			for(ActivityInterval q : intervals) {
				if(canMergeIntervals(p, q)) {
					p.setBegin(min(p.begin(),q.begin()));
					p.setEnd(max(p.begin(),q.end()));
				}
			}
		}
		
		for(ActivityInterval i : fusedIntervals) {
			if(!dedupedIntervals.contains(i)) {
				dedupedIntervals.add(i);
			}
		}
		
		intervals = dedupedIntervals;
		
	}

	private boolean canMergeIntervals(ActivityInterval p, ActivityInterval q) {
		if(!p.getType().equals(q.getType())) return false;
		boolean canMergePQ = canMergeOneWay(p, q);
		boolean canMergeQP = canMergeOneWay(q, p);
		return canMergePQ && canMergeQP;
	}
	
	//assumes l.type == r.type
	private boolean canMergeOneWay(ActivityInterval l, ActivityInterval r) {
		ActivityType type = l.getType();
		boolean canMergeRstart = r.begin().isAfter(l.begin().minus(type.timeoutDuration())) && r.begin().isBefore(l.end().plus(type.timeoutDuration()));
		boolean canMergeRend = r.end().isAfter(l.begin().minus(type.timeoutDuration())) && r.end().isBefore(l.end().plus(type.timeoutDuration()));
		boolean canMergeEclipsing = r.begin().isBefore(l.begin()) && r.end().isAfter(l.end());
		return canMergeEclipsing || canMergeRend || canMergeRstart;
	}
	
	private boolean isInside(ActivityInterval p, ActivityInterval q) {
		boolean pBeginsBeforeQ = p.begin().equals(q.begin()) || p.begin().isBefore(q.begin());
		boolean pEndsAfterQ = p.end().equals(q.end()) || p.end().isAfter(q.end());
		return pBeginsBeforeQ && pEndsAfterQ;
	}
	
	private SortedSet<ActivityInterval> getTestingIntervals() {
		SortedSet<ActivityInterval> testingIntervals = new TreeSet<>(ActivityInterval.COMPARATOR);
		Iterator<ActivityInterval> activeIntervals = intervals.iterator();
		PeekingIterator<FlaggedInstant> testingStateChanges = Iterators.peekingIterator(testingState.iterator());
		FlaggedInstant currentTestingState = new FlaggedInstant(Instant.ofEpochMilli(0),false);
		
		//go through all intervals where we had activity
		while(activeIntervals.hasNext()) {
			ActivityInterval i = activeIntervals.next();
			//forward testing state changes until we get to the current point in time
			while(testingStateChanges.hasNext() && testingStateChanges.peek().instant().isBefore(i.begin())) {
				currentTestingState = testingStateChanges.next();
			}
			
			//create tagged instants for every new testing state change
			TreeSet<FlaggedInstant> periods = new TreeSet<>(FlaggedInstant.COMPARATOR);
			periods.add(new FlaggedInstant(i.begin(),currentTestingState.flag()));
			while(testingStateChanges.hasNext() && testingStateChanges.peek().instant().isBefore(i.end())) {
				currentTestingState = testingStateChanges.next();
				periods.add(currentTestingState);
			}
			
			//make tagged periods out of tagged instants
			PeekingIterator<FlaggedInstant> periodIt = Iterators.peekingIterator(periods.iterator());
			while(periodIt.hasNext()) {
				FlaggedInstant start = periodIt.next();
				if(!start.flag()) continue;
				Instant end = periodIt.hasNext() ? periodIt.peek().instant() : i.end();
				testingIntervals.add(new ActivityInterval(start.instant(),end, ActivityType.TESTINGSTATE));
			}
			
		}
		
		return testingIntervals;
		
	}
	
	public String toString() {
		fuseIntervals();
		return getLongEnoughIntervals().toString();
	}
	
	public static Instant min(Instant a, Instant b) {
		return a.isBefore(b) ? a : b;
	}
	
	public static Instant max(Instant a, Instant b) {
		return a.isAfter(b) ? a : b;
	}
}
