package aggregation;

import java.util.HashSet;
import java.util.Set;

public class IntervalBuilder {
	private final long timeoutInterval;
	private Set<Pair<Long,Long>> intervals = new HashSet<>();
	
	public IntervalBuilder (long timeoutInterval) {
		this.timeoutInterval = timeoutInterval;
	}
	
	public void add(long l) {
		boolean matchedAnyInterval = false;
		for(Pair<Long,Long> p : intervals) {
			if(l > p.getRight() && l <= p.getRight()+timeoutInterval) {
				p.setRight(l);
				
				matchedAnyInterval = true;
			}
		}
		if(!matchedAnyInterval) {
			intervals.add(new Pair<Long, Long>(l, l));
		}
	}
	
	public Set<Pair<Long,Long>> getIntervals() {
		fuseIntervals();
		return intervals;
	}

	private void fuseIntervals() {
		Set<Pair<Long,Long>> fusedIntervals = new HashSet<>();
		
		for(Pair<Long,Long> p : intervals) {
			boolean isInsideExistingInterval = false;
			for(Pair<Long,Long> f : fusedIntervals) {
				if(isInside(f, p)) isInsideExistingInterval = true;
			}
			if(isInsideExistingInterval) continue;
			
			fusedIntervals.add(p);
			for(Pair<Long,Long> q : intervals) {
				if(canMergeIntervals(p, q)) {
					p.setLeft(min(p.getLeft(),q.getLeft()));
					p.setRight(max(p.getRight(),q.getRight()));
				}
			}
		}
		
		intervals = fusedIntervals;
		
	}

	private boolean canMergeIntervals(Pair<Long, Long> p, Pair<Long, Long> q) {
		boolean canMergePQ = canMergeOneWay(p, q);
		boolean canMergeQP = canMergeOneWay(q, p);
		return canMergePQ && canMergeQP;
	}
	
	private boolean canMergeOneWay(Pair<Long, Long> l, Pair<Long, Long> r) {
		boolean canMergeRstart = r.getLeft() > l.getLeft()-timeoutInterval && r.getLeft() < l.getRight()+timeoutInterval;
		boolean canMergeRend = r.getRight() > l.getLeft()-timeoutInterval && r.getRight() < l.getRight()+timeoutInterval;
		boolean canMergeEclipsing = r.getLeft() < l.getLeft() && r.getRight() > l.getRight();
		return canMergeEclipsing || canMergeRend || canMergeRstart;
	}
	
	private boolean isInside(Pair<Long, Long> p, Pair<Long, Long> q) {
		return p.getLeft() <= q.getLeft() && p.getRight() >= q.getRight();
	}
	
	public String toString() {
		fuseIntervals();
		return intervals.toString();
	}
	
	private long min(long a, long b) {
		return a < b ? a : b;
	}
	
	private long max(long a, long b) {
		return a > b ? a : b;
	}
}
