package aggregation.activity;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import entity.ActivityType;
import entity.TaggedInstantBase;
import entity.User;
import entity.activity.ActivityEntry;
import entity.activity.ActivityInterval;
import entity.activity.TestingStateTimestamp;


//receives ActivityIntervals and TaggedInstant<Boolean>s
//produces nice ActivityIntervals
public class IntervalBuilder {
	
	private final User user;
	
	private Set<ActivityInterval> intervals = new HashSet<>();
	private Set<TestingStateTimestamp> testingState = new HashSet<>();
	
	public IntervalBuilder(User user) {
		this.user = user;
	}
	
	//adds a flagged instant to build the testing intervals later
	public void addTestingState(Instant i, boolean testingStatus) {
		testingState.add(new TestingStateTimestamp(i, testingStatus, user));
	}
	
	public void addTestingStates(Collection<TestingStateTimestamp> timestamps) {
		testingState.addAll(timestamps);
	}
	
	//adds an instant to an existing interval or creates a new interval
	public void addActivity(Instant i, ActivityType type) {
		//try to fit activity to any existing interval
		int matches = addToIntervals(i, type);
		//if no nearby interval was found, create a new interval
		if(matches == 0) {
			intervals.add(new ActivityInterval(i, i, type, user));
		}
	}
	
	//add instant to all matching intervals
	//returns number of matches
	private int addToIntervals(Instant i, ActivityType type) {
		int matches = 0;
		for(ActivityInterval p : intervals) {
			if(p.canMerge(i, type)) {
				matches++;
				p.merge(i);
			}
		}
		return matches;
	}
	
	public void addActivityEntries(Collection<ActivityEntry> entries) {
		for(ActivityEntry e : entries) {
			addActivity(e.instant(), e.getType());
		}
	}
	
	public void addActivityInterval(ActivityInterval interval) {
		intervals.add(interval);
	}
	
	public void addActivityIntervals(Collection<ActivityInterval> intervals) {
		this.intervals.addAll(intervals);
	}
	
	public Set<ActivityInterval> getVisibleIntervals() {
		Set<ActivityInterval> is = getVisibleStoredIntervals();
		is.addAll(getTestingIntervals());
		return is;
	}

	//returns all visible intervals, but not the testing intervals because those have to be derived
	private Set<ActivityInterval> getVisibleStoredIntervals() {
		cleanIntervals();
		return intervals.stream()
				.filter(i->i.isVisible())
				.collect(Collectors.toSet());
	}

	//merges intervals that should extend each other, deletes duplicates
	public void cleanIntervals() {
		mergeIntervals();
		dedupIntervals();
	}
	
	private void mergeIntervals() {
		int maxMerges;
		do {
			Set<ActivityInterval> mergedIntervals = new HashSet<>();
			maxMerges = 0;
			for(ActivityInterval i : intervals) {
				int merges = 0;
				for(ActivityInterval m : mergedIntervals) {
					if(m.canMerge(i)) {
						m.merge(i);
						merges++;
					}
				}
				if(merges==0) mergedIntervals.add(i);
				maxMerges = Math.max(maxMerges, merges);
			}
			intervals = mergedIntervals;
		} while (maxMerges > 1); //if we merged the same interval with 2 or more intervals, those might be able to merge
	}
	
	private void dedupIntervals() {
		Set<ActivityInterval> deduped = new HashSet<>();
		for(ActivityInterval i : intervals) {
			boolean coverAny = false;
			for(ActivityInterval d : deduped) {
				if(d.covers(i)) {
					coverAny = true;
					break;
				}
			}
			if(!coverAny) {
				deduped.add(i);
			}
		}
		intervals = deduped;
	}
	
	private Set<ActivityInterval> getTestingIntervals() {
		Set<ActivityInterval> activeIntervals = intervals.stream()
				.filter(i->i.getType().equals(ActivityType.ACTIVE))
				.collect(Collectors.toSet());
		Set<ActivityInterval> testingIntervals = activeIntervals.stream()
				.map(i->intervalToTestingIntervals(i))
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
		return testingIntervals;
	}
	
	private Set<ActivityInterval> intervalToTestingIntervals(ActivityInterval i) {
		Set<TestingStateTimestamp> stateChanges = testingState.stream().
				filter(m->i.contains(m.instant()))
				.collect(Collectors.toSet());
		stateChanges.add(new TestingStateTimestamp(i.begin(), getTestingStateAt(i.begin()), user));
		stateChanges.add(new TestingStateTimestamp(i.end(), false, user));
		return stateChanges.stream()
			.filter(t->t.isTesting())
			.map(t->toTestingInterval(t, stateChanges))
			.collect(Collectors.toSet());
	}
	
	private ActivityInterval toTestingInterval(TestingStateTimestamp t, Set<TestingStateTimestamp> stateChanges) {
		Instant firstInstantAfterT = stateChanges.stream()
				.filter(i->i.instant().isAfter(t.instant()))
				.min(TaggedInstantBase.INSTANT_COMPARATOR)
				.map(i->i.instant()).orElse(t.instant());
		return new ActivityInterval(t.instant(), firstInstantAfterT, ActivityType.TESTINGSTATE, user);
	}
	
	private boolean getTestingStateAt(Instant begin) {
		return testingState.stream()
				.filter(i->i.instant().compareTo(begin)<=0)
				.max(TaggedInstantBase.INSTANT_COMPARATOR)
				.map(t->t.isTesting())
				.orElse(false);
	}
	
	public Set<ActivityInterval> exportRawIntervals() {
		return intervals;
	}
	
	public Set<TestingStateTimestamp> exportRawTimestamps() {
		return testingState;
	}

	public String toString() {
		cleanIntervals();
		return "intervals: "+intervals.toString()+"\n";
	}
	
	public static Instant min(Instant a, Instant b) {
		return a.isBefore(b) ? a : b;
	}
	
	public static Instant max(Instant a, Instant b) {
		return a.isAfter(b) ? a : b;
	}
}
