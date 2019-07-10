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
	private int testsFixed = 0;

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

	public void setTotalBuildDurationInMs(long totalBuildDurationInMs) {
		this.totalBuildDurationInMs = totalBuildDurationInMs;
	}

	public int getBuildCount() {
		return buildCount;
	}

	public void setBuildCount(int buildCount) {
		this.buildCount = buildCount;
	}

	public int getTestsRun() {
		return testsRun;
	}

	public void setTestsRun(int testsRun) {
		this.testsRun = testsRun;
	}

	public int getSuccessfulTests() {
		return successfulTests;
	}

	public void setSuccessfulTests(int successfulTests) {
		this.successfulTests = successfulTests;
	}

	public int getCommits() {
		return commits;
	}

	public void setCommits(int commits) {
		this.commits = commits;
	}

	public int getTestsFixed() {
		return testsFixed;
	}

	public void setTestsFixed(int testsFixed) {
		this.testsFixed = testsFixed;
	}

	public String toString() {
		return "DailyVariousStats["+user+" date:"+date+" buildDur:"+totalBuildDurationInMs+" buildCount:"+buildCount+" testsRun:"+testsRun+" passes:"+successfulTests+" commits:"+commits+"]";
	}
}
