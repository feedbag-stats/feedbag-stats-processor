package aggregation;

import java.time.Instant;
import java.util.Comparator;

public class ActivityInterval {
	
	public static final Comparator<ActivityInterval> COMPARATOR = new Comparator<ActivityInterval>() {
		@Override
		public int compare(ActivityInterval i1, ActivityInterval i2) {
			return i1.begin().compareTo(i2.begin());
		}
	};
	
	private Instant begin;
	private Instant end;
	private final ActivityType type;
	
	public ActivityInterval(Instant begin, Instant end, ActivityType type) {
		this.begin = begin;
		this.end = end;
		this.type = type;
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
		return "ActivityInterval["+begin.toString()+","+end.toString()+","+type.toString()+"]";
	}
}
