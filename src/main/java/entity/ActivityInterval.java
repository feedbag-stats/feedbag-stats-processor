package entity;

import java.time.Instant;

import javax.persistence.*;

import aggregation.ActivityType;

@Entity
@Table(name="activityinterval")
@PrimaryKeyJoinColumn(name="id")
public class ActivityInterval extends BaseInterval {
	
	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private final ActivityType type;
	
	public ActivityInterval(Instant begin, Instant end, ActivityType type) {
		super(begin, end);
		this.type = type;
	}
	
	public boolean isVisible() {
		return end.minus(type.minDisplayedDuration()).compareTo(begin) >= 0;
	}
	
	public boolean covers(ActivityInterval i) {
		return type.equals(i.getType()) && contains(i);
	}
	
	public boolean canMerge(BaseInterval i) {
		if(!(i instanceof ActivityInterval)) return false;
		ActivityInterval interval = (ActivityInterval) i;
		return interval.getType().equals(type) && (canMerge(interval.begin()) || canMerge(interval.end()));
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

	public ActivityType getType() {
		return type;
	}
	
	public String toString() {
		return "ActivityInterval["+begin.toString()+","+end.toString()+","+(type==null ? "NOTYPE" : type.toString())+"]";
	}
	
	public String toJSON(String userid) {
		return "{\"begin\":\""+begin().toString()+"\",\"end\":\""+end().toString()+"\",\"user\"=\""+userid+"\",\"type\"=\""+getType().toString()+"\"}";
	}
}
