package aggregation.various;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;

import aggregation.DeltaImporter;
import aggregation.ProcessingManager;
import entity.User;
import entity.various.DailyVariousStats;
import helpers.TestHibernateUtil;

public class VariousStatsProcessorTest {
	
	SessionFactory factory = TestHibernateUtil.getSessionFactory();
	private ProcessingManager manager;

	@Before
	public void setup() {
		manager = new ProcessingManager(factory);
	}
	
	@Test
	public void testVariousStats() {
		Transaction t = factory.getCurrentSession().beginTransaction();
		User user = DeltaImporter.getOrCreateUser(factory, "testVariousStats");
		t.commit();
		
		String file = "/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata/variousstats-1.zip";
		
		manager.importZip(file);
		
		t = factory.getCurrentSession().beginTransaction();
		List<DailyVariousStats> dailies = factory.getCurrentSession()
				.createQuery("from DailyVariousStats d where d.user = :user", DailyVariousStats.class)
				.setParameter("user", user)
				.getResultList();
		t.commit();
		
		assertEquals(1, dailies.size());
		assertEquals(2, dailies.get(0).getTestsRun());
	}

}
