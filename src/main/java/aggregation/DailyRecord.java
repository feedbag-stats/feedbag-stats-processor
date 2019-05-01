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
import cc.kave.commons.model.events.testrunevents.TestCaseResult;
import cc.kave.commons.model.events.testrunevents.TestRunEvent;
import cc.kave.commons.model.events.visualstudio.DebuggerEvent;
import cc.kave.commons.model.events.visualstudio.EditEvent;

public class DailyRecord {
	
	public static final Comparator<Pair<Instant,Boolean>> TAGGED_INSTANT_COMPARATOR = new Comparator<Pair<Instant,Boolean>>() {
		@Override
		public int compare(Pair<Instant,Boolean> o1, Pair<Instant,Boolean> o2) {
			return o1.getLeft().compareTo(o2.getLeft());
		}
	};
	
	private LocalDate date;
	private IntervalBuilder activityRecord = new IntervalBuilder(Duration.ofSeconds(60), Duration.ofSeconds(5));
	private IntervalBuilder writingRecord = new IntervalBuilder(Duration.ofSeconds(50), Duration.ofSeconds(15));
	private IntervalBuilder debuggingRecord = new IntervalBuilder(Duration.ofSeconds(60), Duration.ofSeconds(5));
	private IntervalBuilder testruns = new IntervalBuilder(Duration.ofSeconds(0), Duration.ofSeconds(0));
	private SortedSet<Pair<Instant,Boolean>> testingState = new TreeSet<Pair<Instant,Boolean>>(TAGGED_INSTANT_COMPARATOR);
	
	
	public DailyRecord(LocalDate date) {
		this.date = date;
	}
	
	public void logEvent(IDEEvent e) {
		
		//add event to active period
		final Instant triggeredAt = e.getTriggeredAt().toInstant();
		logActivity(triggeredAt);
		
		//changes to testingState
		if(e instanceof NavigationEvent) {
			NavigationEvent n = (NavigationEvent)e;
			String fileName = n.ActiveDocument.getFileName();
			boolean isTestingFile = fileName.endsWith("Test.cs") || fileName.endsWith("Tests.cs");
			testingState.add(new Pair<Instant,Boolean>(triggeredAt,isTestingFile));
		} else if(e instanceof EditEvent) {
			//Programmer is in writing mode
			writingRecord.add(e.TriggeredAt.toInstant());
		} else if (e instanceof DebuggerEvent) {
			//Programmer is in debugging mode
			debuggingRecord.add(e.TriggeredAt.toInstant());
		} else if (e instanceof TestRunEvent) {
			//add test intervals
			TestRunEvent t = (TestRunEvent) e;
			for(TestCaseResult i : t.Tests) {
				testruns.add(new Pair<Instant, Instant>(t.TriggeredAt.toInstant(), t.TriggeredAt.toInstant().plus(i.Duration)));
			}
			
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
	
	//returns datestring + svg-string
	public Pair<String,String> toSVG() {
		int timelineWidth = 5000;
		int textWidth = 80;
		double milliPerDay = (24.0*60*60*1000);
		double widthPerMilli = timelineWidth/milliPerDay;
		String svg = "<svg width=\""+(timelineWidth+textWidth)+"\" height=\"200\">";
		
		//date
		svg += "<text x=\"0\" y=\"13\" fontSize=\"6\" lengthAdjust=\"spacingAndGlyphs\" textLength=\""+textWidth+"\">"+date.toString()+"</text>";
		svg += "<rect x='"+(textWidth)+"' y='0' width='2' height='200' fill='#ff0000' />";
		svg += "<rect x='"+(textWidth+(milliPerDay*widthPerMilli))+"' y='0' width='2' height='200' fill='#ff0000' />";
		
		//active intervals
		for(Pair<Instant,Instant> i : activityRecord.getIntervals()) {
			svg += "<rect x=\""+(textWidth+(i.getLeft().toEpochMilli()%milliPerDay)*widthPerMilli)+"\" y=\"50\" width=\""+(i.getRight().toEpochMilli() - i.getLeft().toEpochMilli())*widthPerMilli+"\" height=\"100\" />";
		}
			
		//testing intervals
		for(Pair<Pair<Instant, Instant>, Boolean> i : getTestingIntervals()) {
			if(i.getRight())
			svg += "<rect x=\""+(textWidth+(i.getLeft().getLeft().toEpochMilli()%milliPerDay)*widthPerMilli)+"\" y=\"100\" width=\""+(i.getLeft().getRight().toEpochMilli() - i.getLeft().getLeft().toEpochMilli())*widthPerMilli+"\" height=\"50\" fill=\"#30F030\" fill-opacity=\"0.4\" />";
		}
		
		//testruns
		for(Pair<Instant,Instant> p : testruns.getIntervals()) {
			long milliDuration = p.getRight().toEpochMilli() - p.getLeft().toEpochMilli();
			double px_width = milliDuration*widthPerMilli;
			String widthString = String.format("%.12f", (px_width>1 ? px_width : 1));
			svg += "<rect x=\""+(textWidth+(p.getLeft().toEpochMilli()%milliPerDay)*widthPerMilli)+"\" y=\"50\" width=\""+widthString+"\" height=\"20\" fill=\"#30F030\" />";
		}
		
		//writing
		for(Pair<Instant,Instant> w : writingRecord.getIntervals()) {
			long milliDuration = w.getRight().toEpochMilli() - w.getLeft().toEpochMilli();
			double px_width = milliDuration*widthPerMilli;
			String widthString = String.format("%.12f", (px_width>1 ? px_width : 1));
			svg += "<rect x=\""+(textWidth+(w.getLeft().toEpochMilli()%milliPerDay)*widthPerMilli)+"\" y=\"70\" width=\""+widthString+"\" height=\"30\" fill=\"#3030F0\" />";
		}
		
		//debugging
		for(Pair<Instant,Instant> d : debuggingRecord.getIntervals()) {
			long milliDuration = d.getRight().toEpochMilli() - d.getLeft().toEpochMilli();
			double px_width = milliDuration*widthPerMilli;
			String widthString = String.format("%.12f", (px_width>1 ? px_width : 1));
			svg += "<rect x=\""+(textWidth+(d.getLeft().toEpochMilli()%milliPerDay)*widthPerMilli)+"\" y=\"100\" width=\""+widthString+"\" height=\"20\" fill=\"#F03030\" />";
		}
			
		return new Pair<String,String>(date.toString(), svg + "</svg>");
	}
}
