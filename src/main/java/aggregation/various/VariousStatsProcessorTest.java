package aggregation.various;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;

import aggregation.AbstractBatchProcessor;
import aggregation.ImportBatch;
import cc.kave.commons.model.events.IDEEvent;
import entity.various.DailyVariousStats;
import helpers.TestHibernateUtil;

public class VariousStatsProcessorTest {
	
	SessionFactory factory = TestHibernateUtil.getSessionFactory();
	VariousStatsProcessor processor;

	@Before
	public void setup() {
		processor = new VariousStatsProcessor(factory);
	}
	
	@Test
	public void testVariousStats() {
		Collection<IDEEvent> list = AbstractBatchProcessor.readEvents("/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata", "variousstats-1.zip");
		ImportBatch batch = new ImportBatch(list);
		
		processor.process(batch);
		
		Transaction t = factory.getCurrentSession().beginTransaction();
		List<DailyVariousStats> dailies = factory.getCurrentSession()
				.createQuery("from DailyVariousStats", DailyVariousStats.class)
				.getResultList();
		t.commit();
		
		System.out.println(dailies);
		
		assertEquals(1, dailies.size());
		assertEquals(2, dailies.get(0).getTestsRun());
	}

}
