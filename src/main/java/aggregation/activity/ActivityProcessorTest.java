package aggregation.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;

import aggregation.DeltaImporter;
import aggregation.ProcessingManager;
import entity.User;
import entity.activity.ActivityInterval;
import helpers.TestHibernateUtil;

public class ActivityProcessorTest {
	private ProcessingManager manager;
	private SessionFactory factory = TestHibernateUtil.getSessionFactory();
	
	@Before
	public void setup() {
		manager = new ProcessingManager(factory);
	}
	
	@Test
	public void getOrCreateUser() {
		//create new user
		Transaction t = factory.getCurrentSession().beginTransaction();
		User u = DeltaImporter.getOrCreateUser(factory, "username");
		assertNotNull(u);
		factory.getCurrentSession().save(u);
		t.commit();
		t = factory.getCurrentSession().beginTransaction();
		//get existing user
		User u2 = DeltaImporter.getOrCreateUser(factory, "username");
		t.commit();
		assertEquals(u, u2);
	}
	
	@Test
	public void processorTest() {
		Transaction t = factory.getCurrentSession().beginTransaction();
		User user = DeltaImporter.getOrCreateUser(factory, "processorTest");
		t.commit();
		
		String file1 = "/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata/intervalbuilder-1.zip";
		String file2 = "/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata/intervalbuilder-2.zip";
		
		manager.importZip(file1);
		
		t = factory.getCurrentSession().beginTransaction();
		List<ActivityInterval> intervals = factory.getCurrentSession()
				.createQuery("from ActivityInterval i where i.user = :user", ActivityInterval.class)
				.setParameter("user", user)
				.getResultList();
		t.commit();
		
		manager.importZip(file2);
		
		t = factory.getCurrentSession().beginTransaction();
		intervals = factory.getCurrentSession()
				.createQuery("from ActivityInterval i where i.user = :user", ActivityInterval.class)
				.setParameter("user", user)
				.getResultList();
		t.commit();
		System.out.println(intervals);
		assertEquals(1, intervals.size());
	}
}
