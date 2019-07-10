package entity.location;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import aggregation.location.LocationLevel;
import entity.TaggedInstantBase;
import entity.User;

@Entity
@Table(name="SolutionTimestamp")
public class LocationTimestamp extends TaggedInstantBase{
	
	@Column(nullable=false)
	private String locationName;
	
	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private LocationLevel level;

	public LocationTimestamp() {}
	
	public LocationTimestamp(User user, Instant instant, String name, LocationLevel level) {
		super(instant, user);
		locationName = name;
		this.level = level;
	}

	public String getLocationName() {
		return locationName;
	}
	
	public LocationLevel getLevel() {
		return level;
	}

}
