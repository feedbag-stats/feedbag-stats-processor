package aggregation.location;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;

import aggregation.DeltaImporter;
import aggregation.ImportBatch;
import cc.kave.commons.model.events.IDEEvent;
import entity.location.LocationInterval;
import entity.location.LocationTimestamp;
import helpers.TestHibernateUtil;

public class LocationProcessorTest {
	
	private LocationProcessor processor;
	private DeltaImporter importer;
	private SessionFactory factory = TestHibernateUtil.getSessionFactory();
	
	@Before
	public void setup() {
		processor = new LocationProcessor(factory);
		importer = new DeltaImporter(factory);
	}
	
	@Test
	public void test() {
		Collection<IDEEvent> list1 = DeltaImporter.readEvents("/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata", "location-1.zip");
		ImportBatch batch = new ImportBatch(list1);
		importer.importData(batch);
		processor.updateData(batch);

		Transaction t = factory.getCurrentSession().beginTransaction();
		List<LocationInterval> intervals = factory.getCurrentSession()
				.createQuery("from LocationInterval", LocationInterval.class)
				.getResultList();
		List<LocationTimestamp> timestamps = factory.getCurrentSession()
				.createQuery("from LocationTimestamp", LocationTimestamp.class)
				.getResultList();
		t.commit();

		assertEquals(3, timestamps.size());
		assertEquals(5, intervals.size());
		List<String> solutionLocation = intervals.stream().filter(i->i.getLevel().equals(LocationLevel.SOLUTION)).map(i->i.getLocation()).collect(Collectors.toList());
		assertEquals(2, solutionLocation.size());
		assertFalse(solutionLocation.stream().anyMatch(s->s.equals("???")));
	}

}
