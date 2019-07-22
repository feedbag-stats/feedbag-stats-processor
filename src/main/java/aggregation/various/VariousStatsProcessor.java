package aggregation.various;

import java.time.LocalDate;
import java.util.Collection;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import aggregation.DeltaImporter;
import aggregation.IDataProcessor;
import aggregation.ImportBatch;
import aggregation.location.LocationLevel;
import entity.TaggedInstantBase;
import entity.User;
import entity.location.LocationTimestamp;
import entity.tdd.TestResultTimestamp;
import entity.various.BuildTimestamp;
import entity.various.CommitTimestamp;
import entity.various.DailyVariousStats;

public class VariousStatsProcessor implements IDataProcessor {
	
	private SessionFactory factory;

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
		
		factory.getCurrentSession().saveOrUpdate(stats);
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
