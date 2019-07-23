package aggregation.various;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import aggregation.DeltaImporter;
import aggregation.IDataProcessor;
import aggregation.ImportBatch;
import aggregation.location.LocationLevel;
import entity.ActivityType;
import entity.TaggedInstantBase;
import entity.User;
import entity.activity.ActivityInterval;
import entity.location.LocationTimestamp;
import entity.tdd.FileEditTimestamp;
import entity.tdd.TestResultTimestamp;
import entity.various.BuildTimestamp;
import entity.various.CommitTimestamp;
import entity.various.DailyVariousStats;

public class VariousStatsProcessor implements IDataProcessor {
	
	private SessionFactory factory;
	private static final Duration maxBreakDuration = Duration.ofMinutes(10);

	public VariousStatsProcessor(SessionFactory factory) {
		this.factory = factory;
	}

	@Override
	public void updateData(ImportBatch batch) {
		Transaction t = factory.getCurrentSession().beginTransaction();
		try {
			User user = DeltaImporter.getOrCreateUser(factory, batch.getUsername());
			for(LocalDate day : batch.getDates()) {
				updateDay(user, day);
			}
			t.commit();
		} catch (Exception e) {
			e.printStackTrace();
			t.rollback();
		}
	}

	public void updateDay(User user, LocalDate day) {
		DailyVariousStats stats = getOrCreateRecord(user, day);
		
		Collection<BuildTimestamp> builds = getBuilds(user, day);
		stats.setBuildCount(builds.size());
		stats.setTotalBuildDurationInMs(builds.stream().mapToLong(b->b.getDuration()).sum());
		
		Collection<TestResultTimestamp> tests = getTests(user, day);
		stats.setTestsRun(tests.size());
		stats.setSuccessfulTests((int)tests.stream().filter(t->t.pass()).count());
		stats.setTestsFixed(computeFixedTests(tests));
		
		Collection<CommitTimestamp> commits = getCommits(user, day);
		stats.setCommits(commits.size());
		
		int solutionSwitches = getLocationSwitches(user, day, LocationLevel.SOLUTION);
		stats.setSolutionSwitches(solutionSwitches);
		
		int packageSwitches = getLocationSwitches(user, day, LocationLevel.PACKAGE);
		stats.setPackageSwitches(packageSwitches);

		Collection<ActivityInterval> activeIntervals = getActiveIntervals(user, day);
		Collection<ActivityInterval> sessions = getSessions(activeIntervals);
		stats.setNumSessions(sessions.size());
		stats.setNumSessionsLongerThanTenMin((int)sessions.stream().filter(s->(s.getDuration().compareTo(Duration.ofMinutes(10))>0)).count());
		stats.setTotalSessionDuration(sessions.stream().mapToLong(i->i.getDuration().toMillis()).sum());
		stats.setBreaks(countBreaks(activeIntervals));
		
		Collection<FileEditTimestamp> fileEdits = getFileEdits(user, day);
		stats.setFilesEdited((int)fileEdits.stream().map(t->t.filename()).distinct().count());
		
		factory.getCurrentSession().saveOrUpdate(stats);
	}

	private Collection<FileEditTimestamp> getFileEdits(User user, LocalDate day) {
		return factory.getCurrentSession().createQuery("from FileEditTimestamp i where i.user = :user and day(i.instant) = day(:day) and month(i.instant) = month(:day) and year(i.instant) = year(:day)", FileEditTimestamp.class)
				.setParameter("day", day)
				.setParameter("user", user)
				.getResultList();
	}

	private Collection<ActivityInterval> getActiveIntervals(User user, LocalDate day) {
		return factory.getCurrentSession().createQuery("from ActivityInterval i where i.user = :user and i.type = :type and day(i.begin) = day(:day) and month(i.begin) = month(:day) and year(i.begin) = year(:day)", ActivityInterval.class)
			.setParameter("type", ActivityType.ACTIVE)
			.setParameter("day", day)
			.setParameter("user", user)
			.getResultList();
	}

	private Collection<ActivityInterval> getSessions(Collection<ActivityInterval> activeIntervals) {
		if(activeIntervals.size()==0) return activeIntervals;
		List<ActivityInterval> intervals = new ArrayList<>(activeIntervals);
		ArrayList<ActivityInterval> result = new ArrayList<>();
		intervals.sort(ActivityInterval.BEGIN_COMPARATOR);
		Iterator<ActivityInterval> iter = intervals.iterator();
		ActivityInterval current = iter.next();
		ActivityInterval previous;
		Instant begin = current.begin();
		while(iter.hasNext()) {
			previous = current;
			current = iter.next();
			//sessions can have breaks up to 10min
			if(previous.end().plus(maxBreakDuration).isBefore(current.begin())) {
				//session timed out
				result.add(new ActivityInterval(begin, previous.end(), null, null));
				begin = current.begin();
			}
		}
		result.add(new ActivityInterval(begin, current.end(), null, null));
		return result;
	}
	
	private int countBreaks(Collection<ActivityInterval> activeIntervals) {
		if(activeIntervals.size()==0) return 0;
		List<ActivityInterval> intervals = new ArrayList<>(activeIntervals);
		Iterator<ActivityInterval> iter = intervals.iterator();
		ActivityInterval current = iter.next();
		ActivityInterval previous;
		
		int breaks = 0;
		while(iter.hasNext()) {
			previous = current;
			current = iter.next();
			if(previous.end().plus(maxBreakDuration).isAfter(current.begin())) {
				breaks++;
			}
		}
		
		return breaks;
	}

	private Collection<BuildTimestamp> getBuilds(User user, LocalDate day) {
		return factory.getCurrentSession().createQuery("from BuildTimestamp t where t.user = :user and day(t.instant) = day(:date) and month(t.instant) = month(:date) and year(t.instant) = year(:date)", BuildTimestamp.class)
				.setParameter("user", user)
				.setParameter("date", day)
				.getResultList();
	}

	private Collection<TestResultTimestamp> getTests(User user, LocalDate day) {
		return factory.getCurrentSession().createQuery("from TestResultTimestamp t where t.user = :user and day(t.instant) = day(:date) and month(t.instant) = month(:date) and year(t.instant) = year(:date)", TestResultTimestamp.class)
				.setParameter("user", user)
				.setParameter("date", day)
				.getResultList();
	}

	private Collection<CommitTimestamp> getCommits(User user, LocalDate day) {
		return factory.getCurrentSession().createQuery("from CommitTimestamp t where t.user = :user and day(t.instant) = day(:date) and month(t.instant) = month(:date) and year(t.instant) = year(:date)", CommitTimestamp.class)
				.setParameter("user", user)
				.setParameter("date", day)
				.getResultList();
	}

	private int getLocationSwitches(User user, LocalDate day, LocationLevel level) {
		Collection<LocationTimestamp> locations = factory.getCurrentSession().createQuery("from LocationTimestamp t where t.user = :user and day(t.instant) = day(:date) and month(t.instant) = month(:date) and year(t.instant) = year(:date) and t.level = :level", LocationTimestamp.class)
				.setParameter("user", user)
				.setParameter("date", day)
				.setParameter("level", level)
				.getResultList();
		//only count if new != old
		return (int)locations.stream().filter(old->old.getLocationName()!=
				(locations.stream().filter(l->l.instant().isAfter(old.instant()))
						.min(TaggedInstantBase.INSTANT_COMPARATOR)
						.map(l->l.getLocationName())
						.orElse("")))
				.count();
	}

	private int computeFixedTests(Collection<TestResultTimestamp> tests) {
		return (int)tests.stream()
			.filter(t->!t.pass()) //failed tests
			.map(t->tests.stream() //for each, find if next test is pass or fail?
					.filter(s->s.getIdentifier().equals(t.getIdentifier()))
					.filter(s->s.instant().isAfter(t.instant()))
					.min(TaggedInstantBase.INSTANT_COMPARATOR)
					.filter(s->s.pass()).isPresent()) //did test pass?
			.filter(b->b) //only count if fixed
			.count();
	}

	private DailyVariousStats getOrCreateRecord(User user, LocalDate day) {
		Collection<DailyVariousStats> fetch = factory.getCurrentSession()
				.createQuery("from DailyVariousStats where user = :user and date = :day", DailyVariousStats.class)
				.setParameter("user", user)
				.setParameter("day", day)
				.getResultList();
		if(fetch.isEmpty()) {
			return new DailyVariousStats(user, day);
		} else {
			return fetch.iterator().next();
		}
	}

}
