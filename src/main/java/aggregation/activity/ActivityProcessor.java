package aggregation.activity;

import java.time.LocalDate;
import java.util.Collection;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import aggregation.DeltaImporter;
import aggregation.IDataProcessor;
import aggregation.ImportBatch;
import entity.User;
import entity.activity.ActivityEntry;
import entity.activity.ActivityInterval;
import entity.activity.TestingStateTimestamp;

public class ActivityProcessor implements IDataProcessor {
	
	private final SessionFactory factory;
	
	public ActivityProcessor(SessionFactory factory) {
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
		//remove previous intervals
		Collection<ActivityInterval> intervalsToClear = factory.getCurrentSession()
				.createQuery("from ActivityInterval i where day(i.begin) = day(:date) and month(i.begin) = month(:date) and year(i.begin) = year(:date) and i.user = :user", ActivityInterval.class)
				.setParameter("date", day)
				.setParameter("user", user)
				.getResultList();
		for(ActivityInterval i : intervalsToClear) {
			factory.getCurrentSession().delete(i);
		}
		
		//replace with new ones
		Collection<ActivityEntry> activities = factory.getCurrentSession()
				.createQuery("from ActivityEntry e where e.user = :user and day(e.instant) = day(:date) and month(e.instant) = month(:date) and year(e.instant) = year(:date)", ActivityEntry.class)
				.setParameter("user", user)
				.setParameter("date", day)
				.getResultList();
		Collection<TestingStateTimestamp> testingTimestamps = factory.getCurrentSession()
				.createQuery("from TestingStateTimestamp t where day(t.instant) = day(:date) and month(t.instant) = month(:date) and year(t.instant) = year(:date) and t.user = :user", TestingStateTimestamp.class)
				.setParameter("date", day)
				.setParameter("user", user)
				.getResultList();
		IntervalBuilder builder = new IntervalBuilder(user);
		builder.addActivityEntries(activities);
		builder.addTestingStates(testingTimestamps);
		
		//save new intervals
		Collection<ActivityInterval> intervals = builder.getVisibleIntervals();
		for(ActivityInterval i : intervals) {
			factory.getCurrentSession().save(i);
		}
	}

}
