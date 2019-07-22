package aggregation.tdd;

import java.time.LocalDate;
import java.util.Collection;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import aggregation.DeltaImporter;
import aggregation.IDataProcessor;
import aggregation.ImportBatch;
import entity.User;
import entity.tdd.DailyTDDCycles;
import entity.tdd.FileEditTimestamp;
import entity.tdd.TestResultTimestamp;

public class TDDProcessor implements IDataProcessor{
	
	private final SessionFactory factory;

	public TDDProcessor(SessionFactory factory) {
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
		DailyTDDCycles cycles = getOrCreateRecord(user, day);
		
		Collection<FileEditTimestamp> edits = getEdits(user, day);
		Collection<TestResultTimestamp> tests = getTests(user, day);
		
		TDDCycleDetector detector = new TDDCycleDetector(user);
		detector.addEditEvents(edits);
		detector.addTestResults(tests);
		
		cycles.setCount(detector.getMaxConsecutiveCycles());
		factory.getCurrentSession().save(cycles);
	}

	private DailyTDDCycles getOrCreateRecord(User user, LocalDate day) {
		Collection<DailyTDDCycles> cycles = factory.getCurrentSession().createQuery("from DailyTDDCycles t where t.user = :user and day(t.date) = day(:date) and month(t.date) = month(:date) and year(t.date) = year(:date)", DailyTDDCycles.class)
				.setParameter("user", user)
				.setParameter("date", day)
				.getResultList();
		if(cycles.isEmpty()) {
			return new DailyTDDCycles(user, day, 0);
		} else {
			return cycles.iterator().next();
		}
	}

	private Collection<TestResultTimestamp> getTests(User user, LocalDate day) {
		return factory.getCurrentSession().createQuery("from TestResultTimestamp t where t.user = :user and day(t.instant) = day(:date) and month(t.instant) = month(:date) and year(t.instant) = year(:date)", TestResultTimestamp.class)
				.setParameter("user", user)
				.setParameter("date", day)
				.getResultList();
	}

	private Collection<FileEditTimestamp> getEdits(User user, LocalDate day) {
		return factory.getCurrentSession().createQuery("from FileEditTimestamp t where t.user = :user and day(t.instant) = day(:date) and month(t.instant) = month(:date) and year(t.instant) = year(:date)", FileEditTimestamp.class)
				.setParameter("user", user)
				.setParameter("date", day)
				.getResultList();
	}

}
