package entity.various;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Table;

import entity.TaggedInstantBase;
import entity.User;

@Entity
@Table(name="VersionControlTimestamp")
public class CommitTimestamp extends TaggedInstantBase{

	public CommitTimestamp() {}
	
	public CommitTimestamp(Instant i, User u) {
		super(i, u);
	}

}
