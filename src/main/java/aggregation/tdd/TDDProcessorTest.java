package aggregation.tdd;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;

import aggregation.DeltaImporter;
import aggregation.ProcessingManager;
import entity.User;
import entity.tdd.DailyTDDCycles;
import helpers.TestHibernateUtil;

public class TDDProcessorTest {
	private SessionFactory factory = TestHibernateUtil.getSessionFactory();
	private ProcessingManager manager;
	
	@Before
	public void setup() {
		manager = new ProcessingManager(factory);
	}
	
	@Test
	public void testCycles() {
		Transaction t = factory.getCurrentSession().beginTransaction();
		User user = DeltaImporter.getOrCreateUser(factory, "testCycles");
		t.commit();
		
		String file1 = "/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata/tddcycle-1.zip";
		String file2 = "/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata/tddcycle-2.zip";
		String file3 = "/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata/tddcycle-3.zip";
		
		manager.importZip(file1);
		
		t = factory.getCurrentSession().beginTransaction();
		List<DailyTDDCycles> cycles = factory.getCurrentSession()
				.createQuery("from DailyTDDCycles d where d.user = :user", DailyTDDCycles.class)
				.setParameter("user", user)
				.getResultList();
		t.commit();
		
		assertEquals(1, cycles.size());
		assertEquals(1, cycles.get(0).getCycleCount());
		
		manager.importZip(file2);
		
		t = factory.getCurrentSession().beginTransaction();
		cycles = factory.getCurrentSession()
				.createQuery("from DailyTDDCycles d where d.user = :user", DailyTDDCycles.class)
				.setParameter("user", user)
				.getResultList();
		t.commit();
		
		assertEquals(1, cycles.size());
		assertEquals(2, cycles.get(0).getCycleCount());
		
		manager.importZip(file3);
		
		t = factory.getCurrentSession().beginTransaction();
		cycles = factory.getCurrentSession()
				.createQuery("from DailyTDDCycles d where d.user = :user", DailyTDDCycles.class)
				.setParameter("user", user)
				.getResultList();
		t.commit();
		
		assertEquals(1, cycles.size());
		assertEquals(0, cycles.get(0).getCycleCount());
	}
}
