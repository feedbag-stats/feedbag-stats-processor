package entity.tdd;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import entity.User;

@Entity
@Table(name="DailyTDDCycles")
public class DailyTDDCycles {
	@ManyToOne(optional=false)
	private User user;
	
	@Id
	@GeneratedValue
	@Column(name="id")
	private long id;
	
	@Column(nullable=false)
	private LocalDate date;
	
	@Column(nullable=false)
	private int cycleCount = 0;
	
	public DailyTDDCycles() {}
	public DailyTDDCycles(User u, LocalDate d, int count) {
		user = u;
		date = d;
		cycleCount = count;
	}
	
	public void setCount(int n) {
		cycleCount = n;
	}
	
	public User getUser() {
		return user;
	}
	public long getId() {
		return id;
	}
	public LocalDate getDate() {
		return date;
	}
	public int getCycleCount() {
		return cycleCount;
	}
	
	public String toString() {
		return user.getUsername()+" - "+date+" - "+cycleCount;
	}
}
