package entity;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="fileedittimestamp")
public class FileEditTimestamp extends TaggedInstantBase{
	
	@Column
	private final String filename;

	public FileEditTimestamp(Instant instant, String filename, User user) {
		super(instant, user);
		this.filename = filename;
	}

	public String filename() {
		return filename;
	}

	@Override
	public String toString() {
		return "FileEditTimestamp [filename=" + filename + ", instant=" + instant + ", user=" + user + "]";
	}

}
