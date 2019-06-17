package aggregation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.testrunevents.TestCaseResult;
import cc.kave.commons.model.events.testrunevents.TestRunEvent;
import entity.User;

public class SessionRecord {
	private User user = new User();
	private Map<LocalDate, DailyRecord> dailyRecords = new HashMap<>();
	private TDDCycleDetector sessionDetector = new TDDCycleDetector(null);
	
	public SessionRecord() {};
	public SessionRecord(String id) { user.setName(id);;}
	
	public void setSessId(String id) { user.setName(id);;}
	
	public void addEvent(IDEEvent e) {
		LocalDate date = e.TriggeredAt.toLocalDate();
		if(!dailyRecords.containsKey(date)) dailyRecords.put(date, new DailyRecord(date, user));
		DailyRecord record = dailyRecords.get(date);
		record.logEvent(e);
		
		if (e instanceof CompletionEvent) {
			CompletionEvent c = (CompletionEvent) e;
			sessionDetector.addEditEvent(c.getContext().getSST(), e.TriggeredAt.toInstant());
		} else if (e instanceof TestRunEvent) {
			//add test intervals
			TestRunEvent t = (TestRunEvent) e;
			for(TestCaseResult i : t.Tests) {
				sessionDetector.addTestResult(i.TestMethod, e.TriggeredAt.toInstant(), i.Result);
			}
			
		}
	}
	
	public int totalCycles() {
		return dailyRecords.values().stream().map((r)->r.tddDetector.getMaxConsecutiveCycles()).mapToInt(Integer::intValue).sum();
	}
	
	public String toString() {
		String s = "Session " + user.getName() + "[\n";
		s += sessionDetector.toString();
		return s + "]\n";
	}
	
	public String toJSON() {
		String s = "Session " + user.getName() + "[\n";
		for (DailyRecord record : dailyRecords.values()) {
			s += "    " + record.toJSON() + "\n";
		}
		return s + "]\n";
	}
	
	public ArrayList<Pair<String,String>> toSVGs() {
		ArrayList<Pair<String,String>> svgs = new ArrayList<>();
		for(DailyRecord r : dailyRecords.values()) {
			Pair<String, String> pair = r.toSVG();
			svgs.add(new Pair<String,String>(user.getName()+"-"+pair.getLeft(), pair.getRight()));
		}
		return svgs;
	}
	
}
