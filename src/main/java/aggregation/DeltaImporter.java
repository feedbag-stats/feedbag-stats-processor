package aggregation;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import aggregation.location.LocationLevel;
import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.NavigationEvent;
import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.testrunevents.TestCaseResult;
import cc.kave.commons.model.events.testrunevents.TestResult;
import cc.kave.commons.model.events.testrunevents.TestRunEvent;
import cc.kave.commons.model.events.versioncontrolevents.VersionControlActionType;
import cc.kave.commons.model.events.versioncontrolevents.VersionControlEvent;
import cc.kave.commons.model.events.visualstudio.BuildEvent;
import cc.kave.commons.model.events.visualstudio.DebuggerEvent;
import cc.kave.commons.model.events.visualstudio.EditEvent;
import cc.kave.commons.model.events.visualstudio.SolutionAction;
import cc.kave.commons.model.events.visualstudio.SolutionEvent;
import cc.kave.commons.utils.io.IReadingArchive;
import cc.kave.commons.utils.io.ReadingArchive;
import entity.ActivityType;
import entity.ActivityEntry;
import entity.User;
import entity.ZipMapping;
import entity.activity.TestingStateTimestamp;
import entity.location.LocationTimestamp;
import entity.tdd.FileEditTimestamp;
import entity.tdd.TestResultTimestamp;
import entity.various.BuildTimestamp;
import entity.various.CommitTimestamp;

public class DeltaImporter {
	
	private final SessionFactory factory;

	public DeltaImporter(SessionFactory factory) {
		this.factory = factory;
	}
	
	public void importData(ImportBatch batch, String zipname) {
		Transaction t = factory.getCurrentSession().beginTransaction();
		try {
			User user = getOrCreateUser(factory, batch.getUsername());
			
			for(LocalDate day : batch.getDates()) {
				factory.getCurrentSession().save(new ZipMapping(user, zipname, day));
			}
			
			int numProcessed = 0;
			for(IDEEvent e : batch.getEvents()) {
				try {
					if(numProcessed++%1000==0) System.out.println(numProcessed+" events imported");
					factory.getCurrentSession().save(new ActivityEntry(e.TriggeredAt.toInstant(), user, e.getClass().toString(), e.KaVEVersion, e.toString(), ActivityType.ACTIVE));
					
					if(e instanceof EditEvent) {
						factory.getCurrentSession().save(new FileEditTimestamp(e.TriggeredAt.toInstant(), ((EditEvent) e).Context2.getSST().getEnclosingType().toString(), user));
					} else if (e instanceof CompletionEvent) {
						CompletionEvent c = (CompletionEvent)e;
							factory.getCurrentSession().save(new FileEditTimestamp(e.TriggeredAt.toInstant(), c.context.getSST().getEnclosingType().toString(), user));
					} else if(e instanceof TestRunEvent) {
						for(TestCaseResult r : ((TestRunEvent)e).Tests) {
							factory.getCurrentSession().save(new TestResultTimestamp(e.TriggeredAt.toInstant(), r.TestMethod, r.Result.equals(TestResult.Success), user));
						}
					} else if(e instanceof NavigationEvent) {
						NavigationEvent n = (NavigationEvent)e;
						String fileName = n.ActiveDocument.getFileName();
						String packageName = packageName(n.ActiveDocument.getFileName());
						boolean isTestingFile = fileName.endsWith("Test.cs") || fileName.endsWith("Tests.cs");
						factory.getCurrentSession().save(new TestingStateTimestamp(e.TriggeredAt.toInstant(), isTestingFile, user));
						factory.getCurrentSession().save(new LocationTimestamp(user, e.TriggeredAt.toInstant(), fileName, LocationLevel.FILE));
						factory.getCurrentSession().save(new LocationTimestamp(user, e.TriggeredAt.toInstant(), packageName, LocationLevel.PACKAGE));
					} else if (e instanceof DebuggerEvent) {
						factory.getCurrentSession().save(new ActivityEntry(e.TriggeredAt.toInstant(), user, e.getClass().toString(), e.KaVEVersion, e.toString(), ActivityType.DEBUG));
					} else if(e instanceof BuildEvent) {
						long duration = ((BuildEvent)e).Duration.toMillis();
						factory.getCurrentSession().save(new BuildTimestamp(e.TriggeredAt.toInstant(), user, duration));
					} else if (e instanceof VersionControlEvent) {
						boolean isCommit = ((VersionControlEvent)e).Actions.stream().anyMatch(v->v.ActionType.equals(VersionControlActionType.Commit));
						if(isCommit) {
							factory.getCurrentSession().save(new CommitTimestamp(e.TriggeredAt.toInstant(), user));
						}
					} else if (e instanceof SolutionEvent) {
						SolutionEvent s = (SolutionEvent)e;
						if(s.Action.equals(SolutionAction.OpenSolution)) {
							factory.getCurrentSession().save(new LocationTimestamp(user, e.TriggeredAt.toInstant(), s.Target.getIdentifier(), LocationLevel.SOLUTION));
						} else if (s.Action.equals(SolutionAction.AddProject)) {
							factory.getCurrentSession().save(new LocationTimestamp(user, e.TriggeredAt.toInstant(), s.Target.getIdentifier(), LocationLevel.PROJECT));
						}
					}
				} catch (Exception e1) {
					//ignore malformed events
					System.out.println("Malformed event.");
				}
			}
			
			System.out.println("committing");
			t.commit();
		} catch (Exception e) {
			e.printStackTrace();
			t.rollback();
		}
	}
	
	public static String packageName(String filename) {
		if(filename.lastIndexOf("\\")==-1) return filename;
		return filename.substring(0, filename.lastIndexOf("\\"));
	}
	
	public static User getOrCreateUser(SessionFactory factory, String username) {
		List<User> users = factory.getCurrentSession().createQuery("from User where username = :username", User.class)
				.setParameter("username", username)
				.getResultList();
		if(users.isEmpty()) {
			 User u = new User();
			 u.setName(username);
			 u.setUsername(username);
			 u.setToken(username);
			 factory.getCurrentSession().save(u);
			 return u;
		} else {
			return users.get(0);
		}
	}
	
	public static Collection<IDEEvent> readEvents(String zip) {
		File f = new File(zip);
		if(!f.isFile()) return null;
		ArrayList<IDEEvent> list = new ArrayList<>();
		try (IReadingArchive ra = new ReadingArchive(new File(zip))) {
			// ... and iterate over content.
			while (ra.hasNext() ) {
				IDEEvent e = ra.getNext(IDEEvent.class);
				list.add(e);
			}
		}
		return list;
}

}
