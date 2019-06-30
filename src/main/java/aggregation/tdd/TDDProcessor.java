package aggregation.tdd;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.hibernate.SessionFactory;

import aggregation.AbstractBatchProcessor;
import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.testrunevents.TestCaseResult;
import cc.kave.commons.model.events.testrunevents.TestResult;
import cc.kave.commons.model.events.testrunevents.TestRunEvent;
import cc.kave.commons.model.events.visualstudio.EditEvent;
import entity.User;
import entity.tdd.DailyTDDCycles;
import entity.tdd.FileEditTimestamp;
import entity.tdd.TestResultTimestamp;

public class TDDProcessor extends AbstractBatchProcessor{
	
	private User user;
	private Collection<LocalDate> editedDates = new HashSet<>();

	public TDDProcessor(SessionFactory factory) {
		super(factory);
	}

	@Override
	protected void prepare(User user, Instant begin, Instant end) {
		this.user = user;
	}

	@Override
	protected void addNewData(Collection<IDEEvent> data) {
		for(IDEEvent e : data) {
			if(!editedDates.contains(e.TriggeredAt.toLocalDate())) {
				editedDates.add(e.TriggeredAt.toLocalDate());
			}
			
			if(e instanceof EditEvent) {
				if(((EditEvent)e).Context2 != null) {
					factory.getCurrentSession().save(new FileEditTimestamp(e.TriggeredAt.toInstant(), ((EditEvent) e).Context2.getSST().getEnclosingType().toString(), user));
				}
			} else if (e instanceof CompletionEvent) {
				CompletionEvent c = (CompletionEvent)e;
				if(c.context != null)
					factory.getCurrentSession().save(new FileEditTimestamp(e.TriggeredAt.toInstant(), c.context.getSST().getEnclosingType().toString(), user));
			} else if(e instanceof TestRunEvent) {
				for(TestCaseResult r : ((TestRunEvent)e).Tests) {
					factory.getCurrentSession().save(new TestResultTimestamp(e.TriggeredAt.toInstant(), r.TestMethod, r.Result.equals(TestResult.Success), user));
				}
			}
		}
	}

	@Override
	protected void saveChanges() {
		for(LocalDate d : editedDates) {
			updateResultCount(d);
		}
	}
	
	private void updateResultCount(LocalDate date) {
		DailyTDDCycles cycle;
		List<DailyTDDCycles> cycles = factory.getCurrentSession().createQuery("from DailyTDDCycles t where t.user = :user and t.date = :date", DailyTDDCycles.class)
				.setParameter("user", user)
				.setParameter("date", date)
				.getResultList();
		if(cycles.size()==0) cycle = new DailyTDDCycles(user, date, 0);
			else cycle = cycles.get(0);
		Collection<TestResultTimestamp> testResults = factory.getCurrentSession().createQuery("from TestResultTimestamp t where t.user = :user and day(t.instant) = day(:date) and month(t.instant) = month(:date) and year(t.instant) = year(:date)", TestResultTimestamp.class)
				.setParameter("user", user)
				.setParameter("date", date)
				.getResultList();
		Collection<FileEditTimestamp> fileEdits = factory.getCurrentSession().createQuery("from FileEditTimestamp t where t.user = :user and day(t.instant) = day(:date) and month(t.instant) = month(:date) and year(t.instant) = year(:date)", FileEditTimestamp.class)
				.setParameter("user", user)
				.setParameter("date", date)
				.getResultList();
		TDDCycleDetector detector = new TDDCycleDetector(user);
		detector.addEditEvents(fileEdits);
		detector.addTestResults(testResults);
		int newCycleCount = detector.getMaxConsecutiveCycles();
		cycle.setCount(newCycleCount);
		factory.getCurrentSession().saveOrUpdate(cycle);
	}

}
