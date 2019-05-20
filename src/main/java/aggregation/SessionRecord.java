package aggregation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cc.kave.commons.model.events.IDEEvent;

public class SessionRecord {
	private String sessID;
	private Map<LocalDate, DailyRecord> dailyRecords = new HashMap<>();
	
	public SessionRecord() {};
	public SessionRecord(String id) { sessID = id;}
	
	public void setSessId(String id) { sessID = id;}
	
	public void addEvent(IDEEvent e) {
		LocalDate date = e.TriggeredAt.toLocalDate();
		if(!dailyRecords.containsKey(date)) dailyRecords.put(date, new DailyRecord(date, sessID));
		DailyRecord record = dailyRecords.get(date);
		record.logEvent(e);
	}
	
	public String toString() {
		String s = "Session " + sessID + "[\n";
		for (DailyRecord record : dailyRecords.values()) {
			s += "    " + record.toString() + "\n";
		}
		return s + "]\n";
	}
	
	public String toJSON() {
		String s = "Session " + sessID + "[\n";
		for (DailyRecord record : dailyRecords.values()) {
			s += "    " + record.toJSON() + "\n";
		}
		return s + "]\n";
	}
	
	public ArrayList<Pair<String,String>> toSVGs() {
		ArrayList<Pair<String,String>> svgs = new ArrayList<>();
		for(DailyRecord r : dailyRecords.values()) {
			Pair<String, String> pair = r.toSVG();
			svgs.add(new Pair<String,String>(sessID+"-"+pair.getLeft(), pair.getRight()));
		}
		return svgs;
	}
	
}
