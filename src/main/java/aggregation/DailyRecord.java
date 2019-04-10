package aggregation;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.NavigationEvent;

public class DailyRecord {
	
	private LocalDate date;
	private IntervalBuilder activityRecord = new IntervalBuilder(60000);
	private SortedSet<Pair<Long,Boolean>> testingState = new TreeSet<Pair<Long,Boolean>>(new Comparator<Pair<Long,Boolean>>() {
		@Override
		public int compare(Pair<Long,Boolean> o1, Pair<Long,Boolean> o2) {
			return o1.getLeft().compareTo(o2.getLeft());
		}
	});
	
	public DailyRecord(LocalDate date) {
		this.date = date;
	}
	
	public void logEvent(IDEEvent e) {
		final long millisecondOfDay = e.getTriggeredAt().toInstant().toEpochMilli() % (24*3600*1000);
		logActivity(millisecondOfDay);
		
		//changes to testingState
		if(e instanceof NavigationEvent) {
			final NavigationEvent n = (NavigationEvent)e;
			final String fileName = n.ActiveDocument.getFileName();
			final boolean isTestingFile = fileName.endsWith("Test.cs") || fileName.endsWith("Tests.cs");

			testingState.add(new Pair<Long,Boolean>(millisecondOfDay,isTestingFile));
		}
	}
	
	public void logActivity(long milliOfDay) {
		activityRecord.add(milliOfDay);
	}
	
	public String toString() {
		return date.toString() +"   "+activityRecord.toString() + testingState.toString();
	}
}
