package aggregation;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class IntervalBuilder {
	public static final Comparator<Pair<Instant,Instant>> INTERVAL_COMP = new Comparator<Pair<Instant,Instant>>() {
		@Override
		public int compare(Pair<Instant, Instant> o1, Pair<Instant, Instant> o2) {
			return o1.getLeft().compareTo(o2.getLeft());
		}
	};
	private final TemporalAmount timeoutInterval;
	private final TemporalAmount minDuration;
	private SortedSet<Pair<Instant,Instant>> intervals = new TreeSet<>(INTERVAL_COMP);
	
	public IntervalBuilder (TemporalAmount timeoutInterval) {
		this(timeoutInterval, Duration.ZERO);
	}
	
	public IntervalBuilder (TemporalAmount timeoutInterval, Duration minDuration) {
		this.timeoutInterval = timeoutInterval;
		this.minDuration = minDuration;
	}
	
	public void add(Instant l) {
		boolean matchedAnyInterval = false;
		for(Pair<Instant,Instant> p : intervals) {
			if(l.isAfter(p.getRight()) && (l.isBefore(p.getRight().plus(timeoutInterval)) || l.equals(p.getRight().plus(timeoutInterval)))) {
				p.setRight(l);
				
				matchedAnyInterval = true;
			}
		}
		if(!matchedAnyInterval) {
			intervals.add(new Pair<Instant, Instant>(l, l));
		}
	}
	
	public void add(Pair<Instant,Instant> interval) {
		intervals.add(interval);
	}
	
	public SortedSet<Pair<Instant,Instant>> getIntervals() {
		fuseIntervals();
		return getLongEnoughIntervals();
	}

	private SortedSet<Pair<Instant, Instant>> getLongEnoughIntervals() {
		SortedSet<Pair<Instant,Instant>> result = new TreeSet<>(INTERVAL_COMP);
		for(Pair<Instant,Instant> interval : intervals) {
			if(interval.getRight().minus(minDuration).isAfter(interval.getLeft())) result.add(interval);
		}
		return result;
	}

	private void fuseIntervals() {
		SortedSet<Pair<Instant,Instant>> fusedIntervals = new TreeSet<>(INTERVAL_COMP);
		
		for(Pair<Instant,Instant> p : intervals) {
			boolean isInsideExistingInterval = false;
			for(Pair<Instant,Instant> f : fusedIntervals) {
				if(isInside(f, p)) isInsideExistingInterval = true;
			}
			if(isInsideExistingInterval) continue;
			
			fusedIntervals.add(p);
			for(Pair<Instant,Instant> q : intervals) {
				if(canMergeIntervals(p, q)) {
					p.setLeft(min(p.getLeft(),q.getLeft()));
					p.setRight(max(p.getRight(),q.getRight()));
				}
			}
		}
		
		intervals = fusedIntervals;
		
	}

	private boolean canMergeIntervals(Pair<Instant, Instant> p, Pair<Instant, Instant> q) {
		boolean canMergePQ = canMergeOneWay(p, q);
		boolean canMergeQP = canMergeOneWay(q, p);
		return canMergePQ && canMergeQP;
	}
	
	private boolean canMergeOneWay(Pair<Instant, Instant> l, Pair<Instant, Instant> r) {
		boolean canMergeRstart = r.getLeft().isAfter(l.getLeft().minus(timeoutInterval)) && r.getLeft().isBefore(l.getRight().plus(timeoutInterval));
		boolean canMergeRend = r.getRight().isAfter(l.getLeft().minus(timeoutInterval)) && r.getRight().isBefore(l.getRight().plus(timeoutInterval));
		boolean canMergeEclipsing = r.getLeft().isBefore(l.getLeft()) && r.getRight().isAfter(l.getRight());
		return canMergeEclipsing || canMergeRend || canMergeRstart;
	}
	
	private boolean isInside(Pair<Instant, Instant> p, Pair<Instant, Instant> q) {
		boolean left = p.getLeft().equals(q.getLeft()) || p.getLeft().isBefore(q.getLeft());
		boolean right = p.getRight().equals(q.getRight()) || p.getRight().isAfter(q.getRight());
		return left && right;
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
