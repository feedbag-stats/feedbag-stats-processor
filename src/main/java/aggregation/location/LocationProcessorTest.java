package aggregation.location;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;

import aggregation.DeltaImporter;
import aggregation.ProcessingManager;
import entity.User;
import entity.location.LocationInterval;
import entity.location.LocationTimestamp;
import helpers.TestHibernateUtil;

public class LocationProcessorTest {

	private ProcessingManager manager;
	private SessionFactory factory = TestHibernateUtil.getSessionFactory();
	
	@Before
	public void setup() {
		manager = new ProcessingManager(factory);
	}
	
	@Test
	public void testLocation() {
		Transaction t = factory.getCurrentSession().beginTransaction();
		User user = DeltaImporter.getOrCreateUser(factory, "testLocation");
		t.commit();
		
		String file1 = "/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata/location-1.zip";
		
		manager.importZip(file1);

		t = factory.getCurrentSession().beginTransaction();
		List<LocationInterval> intervals = factory.getCurrentSession()
				.createQuery("from LocationInterval i where i.user = :user", LocationInterval.class)
				.setParameter("user", user)
				.getResultList();
		List<LocationTimestamp> timestamps = factory.getCurrentSession()
				.createQuery("from LocationTimestamp t where t.user = :user", LocationTimestamp.class)
				.setParameter("user", user)
				.getResultList();
		t.commit();

		assertEquals(3, timestamps.size());
		assertEquals(5, intervals.size());
		List<String> solutionLocation = intervals.stream().filter(i->i.getLevel().equals(LocationLevel.SOLUTION)).map(i->i.getLocation()).collect(Collectors.toList());
		assertEquals(2, solutionLocation.size());
		assertFalse(solutionLocation.stream().anyMatch(s->s.equals("???")));
	}

}
