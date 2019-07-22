package aggregation.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;

import aggregation.DeltaImporter;
import aggregation.ImportBatch;
import cc.kave.commons.model.events.IDEEvent;
import entity.User;
import entity.activity.ActivityInterval;
import helpers.TestHibernateUtil;

public class ActivityProcessorTest {
	private ActivityProcessor processor;
	private DeltaImporter importer;
	private SessionFactory factory = TestHibernateUtil.getSessionFactory();
	
	@Before
	public void setup() {
		processor = new ActivityProcessor(factory);
		importer = new DeltaImporter(factory);
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
	public void process() {
		Collection<IDEEvent> list1 = DeltaImporter.readEvents("/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata/intervalbuilder-1.zip");
		Collection<IDEEvent> list2 = DeltaImporter.readEvents("/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata/intervalbuilder-2.zip");
		
		ImportBatch b1 = new ImportBatch(list1);
		ImportBatch b2 = new ImportBatch(list2);
		
		importer.importData(b1, "");
		processor.updateData(b1);
		
		Transaction t = factory.getCurrentSession().beginTransaction();
		List<ActivityInterval> intervals = factory.getCurrentSession()
				.createQuery("from ActivityInterval", ActivityInterval.class)
				.getResultList();
		t.commit();
		
		importer.importData(b2, "");
		processor.updateData(b2);
		
		t = factory.getCurrentSession().beginTransaction();
		intervals = factory.getCurrentSession()
				.createQuery("from ActivityInterval", ActivityInterval.class)
				.getResultList();
		t.commit();
		System.out.println(intervals);
		assertEquals(1, intervals.size());
	}
}
