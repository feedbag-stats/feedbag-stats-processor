package aggregation;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.NavigationEvent;

public class DailyRecord {
	
	private LocalDate date;
	private IntervalBuilder activityRecord = new IntervalBuilder(Duration.ofSeconds(60), Duration.ofSeconds(5));
	private SortedSet<Pair<Instant,Boolean>> testingState = new TreeSet<Pair<Instant,Boolean>>(new Comparator<Pair<Instant,Boolean>>() {
		@Override
		public int compare(Pair<Instant,Boolean> o1, Pair<Instant,Boolean> o2) {
			return o1.getLeft().compareTo(o2.getLeft());
		}
	});
	
	public DailyRecord(LocalDate date) {
		this.date = date;
	}
	
	public void logEvent(IDEEvent e) {
		final Instant millisecondOfDay = e.getTriggeredAt().toInstant();
		logActivity(millisecondOfDay);
		
		//changes to testingState
		if(e instanceof NavigationEvent) {
			final NavigationEvent n = (NavigationEvent)e;
			final String fileName = n.ActiveDocument.getFileName();
			final boolean isTestingFile = fileName.endsWith("Test.cs") || fileName.endsWith("Tests.cs");

			testingState.add(new Pair<Instant,Boolean>(millisecondOfDay,isTestingFile));
		}
	}
	
	public void logActivity(Instant milliOfDay) {
		activityRecord.add(milliOfDay);
	}
	
	public String toString() {
		return date.toString() +"   "+activityRecord.toString() + testingState.toString();
	}
}
