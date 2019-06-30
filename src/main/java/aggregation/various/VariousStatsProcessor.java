package aggregation.various;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;

import org.hibernate.SessionFactory;

import aggregation.AbstractBatchProcessor;
import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.testrunevents.TestResult;
import cc.kave.commons.model.events.testrunevents.TestRunEvent;
import cc.kave.commons.model.events.versioncontrolevents.VersionControlActionType;
import cc.kave.commons.model.events.versioncontrolevents.VersionControlEvent;
import cc.kave.commons.model.events.visualstudio.BuildEvent;
import entity.User;
import entity.various.DailyVariousStats;

public class VariousStatsProcessor extends AbstractBatchProcessor {
	
	HashMap<LocalDate, DailyVariousStats> dailyStatsMap;
	User user;

	public VariousStatsProcessor(SessionFactory factory) {
		super(factory);
	}

	@Override
	protected void prepare(User user, Instant begin, Instant end) {
		dailyStatsMap = new HashMap<>();
		this.user = user;
		
		LocalDate beginDate = begin.atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate endDate = end.atZone(ZoneId.systemDefault()).toLocalDate();
		
		Collection<DailyVariousStats> dailies = factory.getCurrentSession()
				.createQuery("from DailyVariousStats where user = :user and date >= :beginDate and date <= :endDate", DailyVariousStats.class)
				.setParameter("user", user)
				.setParameter("beginDate", beginDate)
				.setParameter("endDate", endDate)
				.getResultList();
		
		for(DailyVariousStats d : dailies) {
			dailyStatsMap.put(d.getDate(), d);
		}
	}

	@Override
	protected void addNewData(Collection<IDEEvent> data) {
		for(IDEEvent e : data) {
			try {
				LocalDate day = e.TriggeredAt.toLocalDate();
				DailyVariousStats eventDay;
				if(dailyStatsMap.containsKey(day)) {
					eventDay = dailyStatsMap.get(day);
				} else {
					eventDay = new DailyVariousStats(user, day);
					dailyStatsMap.put(day, eventDay);
				}
				
				if(e instanceof BuildEvent) {
					long duration = ((BuildEvent)e).Duration.toMillis();
					eventDay.addBuildDuration(duration);
					eventDay.addBuildCount(1);
				} else if (e instanceof TestRunEvent) {
					int tests = ((TestRunEvent)e).Tests.size();
					eventDay.addTestRuns(tests);
					long successfulTests = ((TestRunEvent)e).Tests.stream().filter(t->t.Result.equals(TestResult.Success)).count();
					eventDay.addSuccessfulTests((int) successfulTests);
				} else if (e instanceof VersionControlEvent) {
					boolean isCommit = ((VersionControlEvent)e).Actions.stream().anyMatch(v->v.ActionType.equals(VersionControlActionType.Commit));
					if(isCommit) {
						eventDay.addCommits(1);
					}
				}
			} catch (Exception e1) {
				//Event had not enough information, so we just ignore it
			}
		}
	}

	@Override
	protected void saveChanges() {
		for(DailyVariousStats d : dailyStatsMap.values()) {
			factory.getCurrentSession().saveOrUpdate(d);
		}
	}

}
