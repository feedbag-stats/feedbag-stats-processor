package aggregation.activity;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import aggregation.AbstractBatchProcessor;
import aggregation.ActivityType;
import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.NavigationEvent;
import cc.kave.commons.model.events.testrunevents.TestCaseResult;
import cc.kave.commons.model.events.testrunevents.TestRunEvent;
import cc.kave.commons.model.events.visualstudio.DebuggerEvent;
import cc.kave.commons.model.events.visualstudio.EditEvent;
import entity.ActivityInterval;
import entity.TestingStateTimestamp;
import entity.User;

public class ActivityProcessor extends AbstractBatchProcessor {
	
	private IntervalBuilder builder;
	private User user;
	private Collection<ActivityInterval> intervalsAtStart;
	private Collection<TestingStateTimestamp> testingTimestampsAtStart;
	
	public ActivityProcessor(SessionFactory factory) {
		super(factory);
	}

	@Override
	protected void prepare(User user, Instant begin, Instant end) {
		this.user = user;
		builder = new IntervalBuilder(user);
		intervalsAtStart = factory.getCurrentSession()
				.createQuery("from ActivityInterval i where i.end >= :begin and i.begin <= :end and i.user = :user", ActivityInterval.class)
				.setParameter("begin", begin)
				.setParameter("end", end)
				.setParameter("user", user)
				.getResultList();
		builder.addActivityIntervals(intervalsAtStart);
		testingTimestampsAtStart = factory.getCurrentSession()
				.createQuery("from TestingStateTimestamp t where t.instant > :begin and t.instant < :end and t.user = :user", TestingStateTimestamp.class)
				.setParameter("begin", begin)
				.setParameter("end", end)
				.setParameter("user", user)
				.getResultList();
		builder.addTestingStates(testingTimestampsAtStart);
	}

	@Override
	protected void addNewData(Collection<IDEEvent> data) {
		for(IDEEvent e : data) {
			recordEvent(e);
		}
		builder.cleanIntervals();
	}

	@Override
	protected void saveChanges() {
		Collection<ActivityInterval> intervalsAtEnd = builder.exportRawIntervals();
		Collection<TestingStateTimestamp> testingTimestampsAtEnd = builder.exportRawTimestamps();
		
		Collection<ActivityInterval> removedIntervals = intervalsAtStart.stream().filter(i->!intervalsAtEnd.contains(i)).collect(Collectors.toList());
		Collection<ActivityInterval> notRemovedIntervals = intervalsAtEnd.stream().filter(i->intervalsAtEnd.contains(i)).collect(Collectors.toList());
		Collection<TestingStateTimestamp> removedTimestamps = testingTimestampsAtStart.stream().filter(i->!testingTimestampsAtStart.contains(i)).collect(Collectors.toList());
		Collection<TestingStateTimestamp> notRemovedTimestamps = testingTimestampsAtEnd.stream().filter(i->testingTimestampsAtStart.contains(i)).collect(Collectors.toList());
		
		Session s = factory.getCurrentSession();
		
		for(ActivityInterval i : removedIntervals) {s.remove(i);}
		for(ActivityInterval i : notRemovedIntervals) {s.saveOrUpdate(i);}
		for(TestingStateTimestamp i : removedTimestamps) {s.remove(i);}
		for(TestingStateTimestamp i : notRemovedTimestamps) {s.saveOrUpdate(i);}
	}
	
	private void recordEvent(IDEEvent e) {
		
		//add event to active period
		final Instant triggeredAt = e.getTriggeredAt().toInstant();
		builder.addActivity(triggeredAt, ActivityType.ACTIVE);
		
		//changes to testingState
		if(e instanceof NavigationEvent) {
			NavigationEvent n = (NavigationEvent)e;
			String fileName = n.ActiveDocument.getFileName();
			boolean isTestingFile = fileName.endsWith("Test.cs") || fileName.endsWith("Tests.cs");
			builder.addTestingState(triggeredAt,isTestingFile);
		} else if(e instanceof EditEvent) {
			//Programmer is in writing mode
			builder.addActivity(triggeredAt, ActivityType.WRITE);
		} else if (e instanceof DebuggerEvent) {
			//Programmer is in debugging mode
			builder.addActivity(triggeredAt, ActivityType.DEBUG);
		} else if (e instanceof TestRunEvent) {
			//add test intervals
			TestRunEvent t = (TestRunEvent) e;
			for(TestCaseResult i : t.Tests) {
				builder.addActivityInterval(new ActivityInterval(triggeredAt, triggeredAt.plus(i.Duration), ActivityType.TESTRUN, user));
			}
			
		}
	}

}
