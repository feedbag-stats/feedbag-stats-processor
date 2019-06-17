package entity;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="fileedittimestamp")
public class FileEditTimestamp extends TaggedInstant<String>{

	public FileEditTimestamp(Instant instant, String filename, User user) {
		super(instant, filename, user);
	}

}
