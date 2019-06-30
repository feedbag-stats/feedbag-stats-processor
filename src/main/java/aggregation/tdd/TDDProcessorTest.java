package aggregation.tdd;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;

import aggregation.AbstractBatchProcessor;
import aggregation.ImportBatch;
import cc.kave.commons.model.events.IDEEvent;
import entity.tdd.DailyTDDCycles;
import helpers.TestHibernateUtil;

public class TDDProcessorTest {
	private SessionFactory factory = TestHibernateUtil.getSessionFactory();
	private TDDProcessor processor;
	
	@Before
	public void setup() {
		processor = new TDDProcessor(factory);
	}
	
	@Test
	public void testCycles() {
		ArrayList<IDEEvent> events = AbstractBatchProcessor.readEvents("/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata", "tddcycle-1.zip");
		ArrayList<IDEEvent> events2 = AbstractBatchProcessor.readEvents("/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata", "tddcycle-2.zip");
		ArrayList<IDEEvent> events3 = AbstractBatchProcessor.readEvents("/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata", "tddcycle-3.zip");
		ImportBatch batch = new ImportBatch(events);
		ImportBatch batch2 = new ImportBatch(events2);
		ImportBatch batch3 = new ImportBatch(events3);
		
		processor.process(batch);
		
		Transaction t = factory.getCurrentSession().beginTransaction();
		List<DailyTDDCycles> cycles = factory.getCurrentSession()
				.createQuery("from DailyTDDCycles", DailyTDDCycles.class)
				.getResultList();
		t.commit();
		
		assertEquals(1, cycles.size());
		assertEquals(1, cycles.get(0).getCycleCount());
		
		processor.process(batch2);
		
		t = factory.getCurrentSession().beginTransaction();
		cycles = factory.getCurrentSession()
				.createQuery("from DailyTDDCycles", DailyTDDCycles.class)
				.getResultList();
		t.commit();
		
		assertEquals(1, cycles.size());
		assertEquals(2, cycles.get(0).getCycleCount());
		
		processor.process(batch3);
		
		t = factory.getCurrentSession().beginTransaction();
		cycles = factory.getCurrentSession()
				.createQuery("from DailyTDDCycles", DailyTDDCycles.class)
				.getResultList();
		t.commit();
		
		assertEquals(1, cycles.size());
		assertEquals(0, cycles.get(0).getCycleCount());
	}
}
