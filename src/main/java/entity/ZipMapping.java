package entity;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="ZipMapping")
public class ZipMapping {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne(optional=false)
	private User user;
	
	@Column(nullable=false)
	private String zip;
	
	@Column(nullable=false)
	private LocalDate day;
	
	@Column(nullable=false)
	private boolean markedForDelete = false;

	public ZipMapping() {}
	
	public ZipMapping(User user, String zip, LocalDate day) {
		this.user = user;
		this.zip = zip;
		this.day = day;
	}

	public boolean isMarkedForDelete() {
		return markedForDelete;
	}

	public void setMarkedForDelete(boolean markedForDelete) {
		this.markedForDelete = markedForDelete;
	}

	public User getUser() {
		return user;
	}

	public String getZip() {
		return zip;
	}

	public LocalDate getDay() {
		return day;
	}

}
