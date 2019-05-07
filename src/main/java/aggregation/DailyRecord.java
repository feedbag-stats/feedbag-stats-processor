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
	
	public SortedSet<Pair<Instant,Instant>> getTestingIntervals() {
		SortedSet<Pair<Instant,Instant>> testingIntervals = new TreeSet<>(IntervalBuilder.INTERVAL_COMP);
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
				if(!start.getRight()) continue;
				Instant end = periodIt.hasNext() ? periodIt.peek().getLeft() : i.getRight();
				testingIntervals.add(new Pair<Instant,Instant>(start.getLeft(),end));
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
		
		
		String svg = "<svg width=\""+(timelineWidth+textWidth)+"\" height=\"200\">";
		
		//date
		svg += "<text x=\"0\" y=\"13\" fontSize=\"6\" lengthAdjust=\"spacingAndGlyphs\" textLength=\""+textWidth+"\">"+date.toString()+"</text>";
		
		//active intervals
		svg += intervalsToSVG(activityRecord.getIntervals(), 50, 100, textWidth, "#000000", 0.1, timelineWidth);
			
		//testing intervals
		svg += intervalsToSVG(getTestingIntervals(), 100, 50, textWidth, "#30F030\" fill-opacity=\"0.4", 0.1, timelineWidth);
		
		//testruns
		svg += intervalsToSVG(testruns.getIntervals(), 55, 10, textWidth, "#30F030", 1, timelineWidth);
		
		//writing
		svg += intervalsToSVG(writingRecord.getIntervals(), 70, 30, textWidth, "#3030F0", 1, timelineWidth);
		
		//debugging
		svg += intervalsToSVG(debuggingRecord.getIntervals(), 100, 20, textWidth, "#F03030", 1, timelineWidth);
			
		return new Pair<String,String>(date.toString(), svg + "</svg>");
	}
	
	private String intervalsToSVG(SortedSet<Pair<Instant,Instant>> intervals, int yOffset, int height, int textWidth, String colour, double minBarWidth, int timelineWidth) {
		double milliPerDay = (24.0*60*60*1000);
		double widthPerMilli = timelineWidth/milliPerDay;
		String svg = "";
		for(Pair<Instant,Instant> i : intervals) {
			long milliDuration = i.getRight().toEpochMilli() - i.getLeft().toEpochMilli();
			double px_width = milliDuration*widthPerMilli;
			String widthString = String.format("%.12f", (px_width>minBarWidth ? px_width : minBarWidth));
			svg += "<rect x=\""+(textWidth+(i.getLeft().toEpochMilli()%milliPerDay)*widthPerMilli)+"\" y=\""+yOffset+"\" width=\""+widthString+"\" height=\""+height+"\" fill=\""+colour+"\" />";
		}
		return svg;
	}
	
	public String toJSON() {
		String json = "{";
		
		json += "\"date\":\""+date.toString()+"\",";
		
		//active intervals
		json += "\"activeIntervals\":"+intervalsToJSON(activityRecord.getIntervals())+",";
			
		//testing intervals
		json += "\"testingIntervals\":"+intervalsToJSON(getTestingIntervals())+",";
		
		//testruns
		json += "\"testrunIntervals\":"+intervalsToJSON(testruns.getIntervals())+",";
		
		//writing
		json += "\"writingIntervals\":"+intervalsToJSON(writingRecord.getIntervals())+",";
		
		//debugging
		json += "\"debuggingIntervals\":"+intervalsToJSON(debuggingRecord.getIntervals());
		
		
		return json + "}";
	}
	
	private String intervalsToJSON(SortedSet<Pair<Instant,Instant>> intervals) {
		String json = "[";
		boolean first = true;
		for(Pair<Instant,Instant> i : intervals) {
			if(!first) {
				json+=",";
			} else {
				first = false;
			}
			json += "{\"begin\":\""+i.getLeft().toString()+"\",\"end\":\""+i.getRight().toString()+"\"}";
		}
		return json + "]";
	}
}
