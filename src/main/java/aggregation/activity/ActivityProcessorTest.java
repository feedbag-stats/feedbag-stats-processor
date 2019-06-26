package aggregation.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;

import aggregation.ImportBatch;
import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.userprofiles.UserProfileEvent;
import cc.kave.commons.utils.io.IReadingArchive;
import cc.kave.commons.utils.io.ReadingArchive;
import entity.ActivityInterval;
import entity.User;
import helpers.TestHibernateUtil;

public class ActivityProcessorTest {
	private ActivityProcessor processor;
	private SessionFactory factory = TestHibernateUtil.getSessionFactory();
	UserProfileEvent e = new UserProfileEvent() {
		
	};
	
	@Before
	public void setup() {
		processor = new ActivityProcessor(factory);
	}
	
	@Test
	public void getOrCreateUser() {
		//create new user
		Transaction t = factory.getCurrentSession().beginTransaction();
		User u = processor.getOrCreateUser("username");
		assertNotNull(u);
		factory.getCurrentSession().save(u);
		t.commit();
		t = factory.getCurrentSession().beginTransaction();
		//get existing user
		User u2 = processor.getOrCreateUser("username");
		t.commit();
		assertEquals(u, u2);
	}
	
	@Test
	public void process() {
		ArrayList<IDEEvent> list1 = readEvents("/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata", "intervalbuilder-1.zip");
		ArrayList<IDEEvent> list2 = readEvents("/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata", "intervalbuilder-2.zip");
		
		ImportBatch b1 = new ImportBatch(list1);
		ImportBatch b2 = new ImportBatch(list2);
		
		processor.process(b1);
		
		Transaction t = factory.getCurrentSession().beginTransaction();
		List<ActivityInterval> intervals = factory.getCurrentSession()
				.createQuery("from ActivityInterval", ActivityInterval.class)
				.getResultList();
		t.commit();
		
		processor.process(b2);
		
		t = factory.getCurrentSession().beginTransaction();
		intervals = factory.getCurrentSession()
				.createQuery("from ActivityInterval", ActivityInterval.class)
				.getResultList();
		t.commit();
		
		assertEquals(2, intervals.size());
	}
	
	private ArrayList<IDEEvent> readEvents(String path, String file) {
		ArrayList<IDEEvent> list = new ArrayList<>();
		try (IReadingArchive ra = new ReadingArchive(new File(path, file))) {
			// ... and iterate over content.
			while (ra.hasNext() ) {
				IDEEvent e = ra.getNext(IDEEvent.class);
				list.add(e);
			}
		}
		return list;
	}
}
