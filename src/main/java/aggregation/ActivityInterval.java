package aggregation;

import java.time.Instant;

public class ActivityInterval {
	
	private Instant begin;
	private Instant end;
	private final ActivityType type;
	
	public ActivityInterval(Instant begin, Instant end, ActivityType type) {
		this.begin = begin;
		this.end = end;
		this.type = type;
	}
	
	public boolean isVisible() {
		return end.minus(type.minDisplayedDuration()).compareTo(begin) >= 0;
	}
	
	public boolean covers(ActivityInterval i) {
		return type.equals(i.getType()) && contains(i);
	}
	
	public boolean contains(ActivityInterval i) {
		return contains(i.begin()) && contains(i.end());
	}
	
	public boolean contains(Instant i) {
		boolean beginsBefore = begin.equals(i) || begin.isBefore(i);
		boolean endsAfter = end.equals(i) || end.isAfter(i);
		return beginsBefore && endsAfter; 
	}
	
	public void merge(ActivityInterval i) {
		merge(i.begin());
		merge(i.end());
	}
	
	public void merge(Instant i) {
		this.begin = min(begin, i);
		this.end = max(end, i);
	}
	
	public boolean canMerge(ActivityInterval i) {
		return i.getType().equals(type) && (canMerge(i.begin()) || canMerge(i.end()));
	}

	public boolean canMerge(Instant i, ActivityType type2) {
		return type.equals(type2) && canMerge(i);
	}
	
	public boolean canMerge(Instant i) {
		return contains(i) || extendsBoundaries(i);
	}
	
	private boolean extendsBoundaries(Instant i) {
		return extendsBeginBoundary(i) || extendsEndBoundary(i);
	}

	private boolean extendsEndBoundary(Instant i) {
		Instant timeoutEnd = end.plus(type.timeoutDuration());
		return i.isAfter(end) && i.compareTo(timeoutEnd) <= 0;
	}

	private boolean extendsBeginBoundary(Instant i) {
		Instant timeoutBegin = begin.minus(type.timeoutDuration());
		return i.isBefore(begin) && i.compareTo(timeoutBegin) >= 0;
	}

	public Instant begin() {
		return begin;
	}

	public void setBegin(Instant begin) {
		this.begin = begin;
	}

	public Instant end() {
		return end;
	}

	public void setEnd(Instant end) {
		this.end = end;
	}

	public ActivityType getType() {
		return type;
	}
	
	public String toString() {
		return "ActivityInterval["+begin.toString()+","+end.toString()+","+(type==null ? "NOTYPE" : type.toString())+"]";
	}
	
	public String toJSON(String userid) {
		return "{\"begin\":\""+begin().toString()+"\",\"end\":\""+end().toString()+"\",\"user\"=\""+userid+"\",\"type\"=\""+getType().toString()+"\"}";
	}
	
	public static Instant min(Instant a, Instant b) {
		return a.isBefore(b) ? a : b;
	}
	
	public static Instant max(Instant a, Instant b) {
		return a.isAfter(b) ? a : b;
	}
}
