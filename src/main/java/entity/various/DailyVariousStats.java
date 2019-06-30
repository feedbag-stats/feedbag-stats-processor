package entity.various;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import entity.User;

@Entity
@Table(name="DailyVariousStats")
public class DailyVariousStats {
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;
	
	@ManyToOne(optional=false)
	protected User user;
	
	@Column(nullable=false)
	LocalDate date;
	
	@Column(nullable=false)
	private long totalBuildDurationInMs = 0;
	

	@Column(nullable=false)
	private int buildCount = 0;
	

	@Column(nullable=false)
	private int testsRun = 0;
	
	@Column(nullable=false)
	private int successfulTests = 0;
	
	@Column(nullable=false)
	private int commits = 0;
	
	public DailyVariousStats() {}
	
	public DailyVariousStats(User user, LocalDate date) {
		this.user = user;
		this.date = date;
	}

	public User getUser() {
		return user;
	}

	public LocalDate getDate() {
		return date;
	}

	public long getTotalBuildDurationInMs() {
		return totalBuildDurationInMs;
	}
	
	public void addBuildDuration(long additionalMs) {
		totalBuildDurationInMs += additionalMs;
	}

	public int getBuildCount() {
		return buildCount;
	}
	
	public void addBuildCount(int n) {
		buildCount += n;
	}

	public int getTestsRun() {
		return testsRun;
	}
	
	public void addTestRuns(int n) {
		testsRun += n;
	}

	public int getCommits() {
		return commits;
	}
	
	public void addCommits(int n) {
		commits += n;
	}

	public int getSuccessfulTests() {
		return successfulTests;
	}

	public void addSuccessfulTests(int n) {
		successfulTests += n;
	}
	
	public String toString() {
		return "DailyVariousStats["+user+" date:"+date+" buildDur:"+totalBuildDurationInMs+" buildCount:"+buildCount+" testsRun:"+testsRun+" passes:"+successfulTests+" commits:"+commits+"]";
	}
}
