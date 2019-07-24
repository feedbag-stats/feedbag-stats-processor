package entity.tdd;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import entity.TaggedInstantBase;
import entity.User;

@Entity
@Table(name="FileEditTimestamp")
public class FileEditTimestamp extends TaggedInstantBase{
	
	@Column
	private String filename;
	
	public FileEditTimestamp() {}

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
