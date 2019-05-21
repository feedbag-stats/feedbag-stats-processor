package aggregation;

import java.time.Instant;
import java.time.LocalDate;
import java.util.SortedSet;

import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.NavigationEvent;
import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.testrunevents.TestCaseResult;
import cc.kave.commons.model.events.testrunevents.TestResult;
import cc.kave.commons.model.events.testrunevents.TestRunEvent;
import cc.kave.commons.model.events.visualstudio.DebuggerEvent;
import cc.kave.commons.model.events.visualstudio.EditEvent;
import cc.kave.commons.model.ssts.declarations.IMethodDeclaration;

public class DailyRecord {
	
	private String userid;
	private LocalDate date;
	private IntervalBuilder activityRecord = new IntervalBuilder();
	public TDDCycleDetector tddDetector = new TDDCycleDetector();
	
	
	public DailyRecord(LocalDate date, String userid) {
		this.date = date;
		this.userid = userid;
	}
	
	public void logEvent(IDEEvent e) {
		
		//add event to active period
		final Instant triggeredAt = e.getTriggeredAt().toInstant();
		activityRecord.add(triggeredAt, ActivityType.ACTIVE);
		
		//changes to testingState
		if(e instanceof NavigationEvent) {
			NavigationEvent n = (NavigationEvent)e;
			String fileName = n.ActiveDocument.getFileName();
			boolean isTestingFile = fileName.endsWith("Test.cs") || fileName.endsWith("Tests.cs");
			activityRecord.add(triggeredAt,isTestingFile);
		} else if(e instanceof EditEvent) {
			//Programmer is in writing mode
			activityRecord.add(triggeredAt, ActivityType.WRITE);
		} else if (e instanceof DebuggerEvent) {
			//Programmer is in debugging mode
			activityRecord.add(triggeredAt, ActivityType.DEBUG);
		} else if (e instanceof TestRunEvent) {
			//add test intervals
			TestRunEvent t = (TestRunEvent) e;
			for(TestCaseResult i : t.Tests) {
				activityRecord.add(new ActivityInterval(triggeredAt, triggeredAt.plus(i.Duration), ActivityType.TESTRUN));
				tddDetector.addTestResult(i.TestMethod, triggeredAt, i.Result);
			}
			
		} else if (e instanceof CompletionEvent) {
			CompletionEvent c = (CompletionEvent) e;
			tddDetector.addSST(c.getContext().getSST(), triggeredAt);
		}
	}

	//returns datestring + svg-string
	public Pair<String,String> toSVG() {
		int timelineWidth = 5000;
		int textWidth = 80;
		
		
		String svg = "<svg width=\""+(timelineWidth+textWidth)+"\" height=\"200\">";
		
		//date
		svg += "<text x=\"0\" y=\"13\" fontSize=\"6\" lengthAdjust=\"spacingAndGlyphs\" textLength=\""+textWidth+"\">"+date.toString()+"</text>";
		
		
		svg += intervalsToSVG(activityRecord.getIntervals(), textWidth, 0.1, timelineWidth);
			
		return new Pair<String,String>(date.toString(), svg + "</svg>");
	}
	
	private String intervalsToSVG(SortedSet<ActivityInterval> intervals, int textWidth, double minBarWidth, int timelineWidth) {
		double milliPerDay = (24.0*60*60*1000);
		double widthPerMilli = timelineWidth/milliPerDay;
		String svg = "";
		for(ActivityInterval i : intervals) {
			long milliDuration = i.end().toEpochMilli() - i.begin().toEpochMilli();
			double px_width = milliDuration*widthPerMilli;
			String widthString = String.format("%.12f", (px_width>minBarWidth ? px_width : minBarWidth));
			svg += "<rect x=\""+(textWidth+(i.begin().toEpochMilli()%milliPerDay)*widthPerMilli)+"\" y=\""+i.getType().svgYOffset()+"\" width=\""+widthString+"\" height=\""+i.getType().svgHeight()+"\" fill=\""+i.getType().svgColour()+"\" />";
		}
		return svg;
	}
	
	public String toJSON() {
		String json = "{";
		
		json += "\"date\":\""+date.toString()+"\",";
		
		//active intervals
		json += "\"intervals\":["+intervalsToJSON(activityRecord.getIntervals())+"]";
		
		
		return json + "}";
	}
	
	//return format: {begin,end,user,type},{begin,end,user,type},...
	private String intervalsToJSON(SortedSet<ActivityInterval> intervals) {
		String json = "";
		boolean first = true;
		for(ActivityInterval i : intervals) {
			if(!first) {
				json+=",";
			} else {
				first = false;
			}
			json += i.toJSON(userid);
		}
		return json;
	}
}
