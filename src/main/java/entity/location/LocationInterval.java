package entity.location;

import java.time.Instant;

import javax.persistence.*;

import aggregation.location.LocationLevel;
import entity.BaseInterval;
import entity.User;

@Entity
@Table(name="locationinterval")
public class LocationInterval extends BaseInterval {
	
	@Column
	private String location;
	
	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private LocationLevel level;

	public LocationInterval() {}
	
	public LocationInterval(Instant begin, Instant end, String location, User user, LocationLevel level) {
		super(begin, end, user);
		this.location = location;
		this.level = level;
	}
 
	@Override
	public boolean canMerge(BaseInterval i) {
		if(!(i instanceof LocationInterval)) return false;
		LocationInterval interval = (LocationInterval) i;
		boolean locationMatch = level.equals(interval.getLevel()) && location.equals(interval.getLocation());
		boolean intervalOverlap = canMerge(i.begin()) || canMerge(i.end()) || interval.contains(this);
		return locationMatch && intervalOverlap;
	}

	@Override
	public boolean canMerge(Instant i) {
		return begin.compareTo(i) >= 0 && end.compareTo(i) <= 0;
	}

	public String getLocation() {
		return location;
	}

	public LocationLevel getLevel() {
		return level;
	}
	
	public String toString() {
		return "LocationInterval<"+begin+"-"+end+" level: "+level+" location: "+location+">";
	}

}
