package entity.various;

import java.time.LocalDate;

import javax.persistence.*;

import entity.User;

@Entity
@Table(name="DailyVariousStats")
public class DailyVariousStats {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name="id")
	private Long id;
	
	@ManyToOne(optional=false)
	protected User user;
	
	@Column(nullable=false)
	LocalDate date;
	
	@Column(nullable=false)
	private long totalBuildDurationInMs = 0;
	
	@Column(nullable=false)
	private int solutionSwitches = 0;
	
	@Column(nullable=false)
	private int packageSwitches = 0;

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
	
	@Column(nullable=false)
	private int numSessions = 0;
	
	@Column(nullable=false)
	private int numSessionsLongerThanTenMin = 0;
	
	@Column(nullable=false)
	private long totalSessionDurationMillis = 0;
	
	@Column(nullable=false)
	private int breaks = 0;
	
	@Column(nullable=false)
	private int filesEdited = 0;
	
	public DailyVariousStats() {}
	
	public DailyVariousStats(User user, LocalDate date) {
		this.user = user;
		this.date = date;
	}

	public int getFilesEdited() {
		return filesEdited;
	}

	public void setFilesEdited(int filesEdited) {
		this.filesEdited = filesEdited;
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

	public int getSolutionSwitches() {
		return solutionSwitches;
	}

	public void setSolutionSwitches(int solutionSwitches) {
		this.solutionSwitches = solutionSwitches;
	}

	public int getPackageSwitches() {
		return packageSwitches;
	}

	public void setPackageSwitches(int packageSwitches) {
		this.packageSwitches = packageSwitches;
	}

	public int getNumSessions() {
		return numSessions;
	}

	public void setNumSessions(int numSessions) {
		this.numSessions = numSessions;
	}

	public int getNumSessionsLongerThanTenMin() {
		return numSessionsLongerThanTenMin;
	}

	public void setNumSessionsLongerThanTenMin(int numSessionsLongerThanTenMin) {
		this.numSessionsLongerThanTenMin = numSessionsLongerThanTenMin;
	}

	public long getTotalSessionDurationMillis() {
		return totalSessionDurationMillis;
	}

	public void setTotalSessionDuration(long totalSessionDurationMillis) {
		this.totalSessionDurationMillis = totalSessionDurationMillis;
	}

	public int getBreaks() {
		return breaks;
	}

	public void setBreaks(int breaks) {
		this.breaks = breaks;
	}

	public String toString() {
		return "DailyVariousStats["+user+" date:"+date+" buildDur:"+totalBuildDurationInMs+" buildCount:"+buildCount+" testsRun:"+testsRun+" passes:"+successfulTests+" commits:"+commits+"]";
	}
}
