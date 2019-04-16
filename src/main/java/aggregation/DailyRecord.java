package aggregation;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.NavigationEvent;

public class DailyRecord {
	
	public static final Comparator<Pair<Instant,Boolean>> TAGGED_INSTANT_COMPARATOR = new Comparator<Pair<Instant,Boolean>>() {
		@Override
		public int compare(Pair<Instant,Boolean> o1, Pair<Instant,Boolean> o2) {
			return o1.getLeft().compareTo(o2.getLeft());
		}
	};
	
	private LocalDate date;
	private IntervalBuilder activityRecord = new IntervalBuilder(Duration.ofSeconds(60), Duration.ofSeconds(5));
	private SortedSet<Pair<Instant,Boolean>> testingState = new TreeSet<Pair<Instant,Boolean>>(TAGGED_INSTANT_COMPARATOR);
	
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
	
	public SortedSet<Pair<Pair<Instant,Instant>,Boolean>> getTestingIntervals() {
		SortedSet<Pair<Pair<Instant,Instant>,Boolean>> testingIntervals = new TreeSet<>(new Comparator<Pair<Pair<Instant,Instant>,Boolean>>() {
			@Override
			public int compare(Pair<Pair<Instant,Instant>,Boolean> o1, Pair<Pair<Instant,Instant>,Boolean> o2) {
				return o1.getLeft().getLeft().compareTo(o2.getLeft().getLeft());
			}
		});
		Iterator<Pair<Instant, Instant>> activeIntervals = activityRecord.getIntervals().iterator();
		PeekingIterator<Pair<Instant,Boolean>> testingStateChanges = Iterators.peekingIterator(testingState.iterator());
		Pair<Instant,Boolean> currentTestingState = new Pair<>(Instant.ofEpochMilli(0),false);
		
		//go through all intervals where we had activity
		while(activeIntervals.hasNext()) {
			Pair<Instant, Instant> i = activeIntervals.next();
			//forward testing state changes until we get to the current point in time
			while(testingStateChanges.hasNext() && testingStateChanges.peek().getLeft().isBefore(i.getLeft())) {
				currentTestingState = testingStateChanges.next();
			}
			
			//create tagged instants for every new testing state change
			TreeSet<Pair<Instant,Boolean>> periods = new TreeSet<>(TAGGED_INSTANT_COMPARATOR);
			periods.add(new Pair<Instant,Boolean>(i.getLeft(),currentTestingState.getRight()));
			while(testingStateChanges.hasNext() && testingStateChanges.peek().getLeft().isBefore(i.getRight())) {
				currentTestingState = testingStateChanges.next();
				periods.add(currentTestingState);
			}
			
			//make tagged periods out of tagged instants
			PeekingIterator<Pair<Instant,Boolean>> periodIt = Iterators.peekingIterator(periods.iterator());
			while(periodIt.hasNext()) {
				Pair<Instant,Boolean> start = periodIt.next();
				Instant end = periodIt.hasNext() ? periodIt.peek().getLeft() : i.getRight();
				testingIntervals.add(new Pair<Pair<Instant,Instant>,Boolean>(new Pair<Instant,Instant>(start.getLeft(),end), start.getRight()));
			}
			
		}
		
		return testingIntervals;
		
	}
	
	public void logActivity(Instant milliOfDay) {
		activityRecord.add(milliOfDay);
	}
	
	public String toString() {
		//return date.toString() +"   "+activityRecord.toString() + testingState.toString();
		return date.toString() +"   "+getTestingIntervals().toString();
	}
	
	public String toSVG() {
		int timelineWidth = 500;
		int textWidth = 40;
		double milliPerDay = (24.0*60*60*1000);
		double widthPerMilli = timelineWidth/milliPerDay;
		String svg = "<svg width=\""+timelineWidth+"\" height=\"20\">";
		
		//date
		svg += "<text x=\"0\" y=\"13\" fontSize=\"6\" lengthAdjust=\"spacingAndGlyphs\" textLength=\""+textWidth+"\">"+date.toString()+"</text>";
		
		//active intervals
		for(Pair<Instant,Instant> i : activityRecord.getIntervals()) {
			svg += "<rect x=\""+(textWidth+(i.getLeft().toEpochMilli()%milliPerDay)*widthPerMilli)+"\" y=\"5\" width=\""+(i.getRight().toEpochMilli() - i.getLeft().toEpochMilli())*widthPerMilli+"\" height=\"10\" />";
		}
			
		//testing intervals
		for(Pair<Pair<Instant, Instant>, Boolean> i : getTestingIntervals()) {
			if(i.getRight())
			svg += "<rect x=\""+(textWidth+(i.getLeft().getLeft().toEpochMilli()%milliPerDay)*widthPerMilli)+"\" y=\"10\" width=\""+(i.getLeft().getRight().toEpochMilli() - i.getLeft().getLeft().toEpochMilli())*widthPerMilli+"\" height=\"5\" fill=\"#30F030\" />";
		}
			
		return svg + "</svg>";
	}
}
