package aggregation;

import static org.junit.Assert.assertEquals;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

public class IntervalBuilderTest {
	private ActivityType active = ActivityType.ACTIVE;
	private ActivityType debug = ActivityType.DEBUG;
	private IntervalBuilder builder;
	
	@Before
	public void setup() {
		builder = new IntervalBuilder();
	}
	
	@Test
	public void basicInterval() {
		builder.add(Instant.ofEpochMilli(1), active);
		builder.add(Instant.ofEpochMilli(10000), active);
		builder.add(Instant.ofEpochMilli(20000), active);
		builder.add(Instant.ofEpochMilli(30000), active);
		
		assertEquals(1, builder.getVisibleIntervals().size());
	}
	
	@Test
	public void multiInterval() {
		builder.add(Instant.ofEpochMilli(1), active);
		builder.add(Instant.ofEpochMilli(10000), debug);
		builder.add(Instant.ofEpochMilli(20000), active);
		builder.add(Instant.ofEpochMilli(30000), debug);
		

		builder.add(Instant.ofEpochMilli(300000), debug);
		builder.add(Instant.ofEpochMilli(310000), debug);
		builder.add(Instant.ofEpochMilli(320000), debug);
		
		assertEquals(3, builder.getVisibleIntervals().size());
	}
	
	@Test
	public void testingInterval() {
		builder.add(Instant.ofEpochMilli(1), active);
		builder.add(Instant.ofEpochMilli(10000), active);
		builder.add(Instant.ofEpochMilli(20000), active);
		
		builder.add(Instant.ofEpochMilli(12000), true);
		builder.add(Instant.ofEpochMilli(13000), false);
		builder.add(Instant.ofEpochMilli(14000), true);
		
		assertEquals(3, builder.getVisibleIntervals().size());
	}
	
	@Test
	public void mergeIntervals() {
		builder.add(Instant.ofEpochMilli(1), active);
		builder.add(Instant.ofEpochMilli(100000), active); //farther out than timeout
		builder.add(Instant.ofEpochMilli(50000), active); //added to both intervals
		
		assertEquals(1, builder.getVisibleIntervals().size());
	}
}
