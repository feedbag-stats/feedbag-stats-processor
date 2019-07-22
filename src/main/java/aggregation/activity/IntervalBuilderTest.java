package aggregation.activity;

import static org.junit.Assert.assertEquals;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import entity.ActivityType;

public class IntervalBuilderTest {
	private ActivityType active = ActivityType.ACTIVE;
	private ActivityType debug = ActivityType.DEBUG;
	private IntervalBuilder builder;
	
	@Before
	public void setup() {
		builder = new IntervalBuilder(null);
	}
	
	@Test
	public void basicInterval() {
		builder.addActivity(Instant.ofEpochMilli(1), active);
		builder.addActivity(Instant.ofEpochMilli(10000), active);
		builder.addActivity(Instant.ofEpochMilli(20000), active);
		builder.addActivity(Instant.ofEpochMilli(30000), active);
		
		assertEquals(1, builder.getVisibleIntervals().size());
	}
	
	@Test
	public void multiInterval() {
		builder.addActivity(Instant.ofEpochMilli(1), active);
		builder.addActivity(Instant.ofEpochMilli(10000), debug);
		builder.addActivity(Instant.ofEpochMilli(20000), active);
		builder.addActivity(Instant.ofEpochMilli(30000), debug);
		

		builder.addActivity(Instant.ofEpochMilli(300000), debug);
		builder.addActivity(Instant.ofEpochMilli(310000), debug);
		builder.addActivity(Instant.ofEpochMilli(320000), debug);
		
		assertEquals(3, builder.getVisibleIntervals().size());
	}
	
	@Test
	public void testingInterval() {
		builder.addActivity(Instant.ofEpochMilli(1), active);
		builder.addActivity(Instant.ofEpochMilli(10000), active);
		builder.addActivity(Instant.ofEpochMilli(20000), active);
		
		builder.addTestingState(Instant.ofEpochMilli(12000), true);
		builder.addTestingState(Instant.ofEpochMilli(13000), false);
		builder.addTestingState(Instant.ofEpochMilli(14000), true);
		
		assertEquals(3, builder.getVisibleIntervals().size());
	}
	
	@Test
	public void mergeIntervals() {
		builder.addActivity(Instant.ofEpochMilli(1), active);
		builder.addActivity(Instant.ofEpochMilli(100000), active); //farther out than timeout
		builder.addActivity(Instant.ofEpochMilli(50000), active); //added to both intervals
		
		assertEquals(1, builder.getVisibleIntervals().size());
	}
}
