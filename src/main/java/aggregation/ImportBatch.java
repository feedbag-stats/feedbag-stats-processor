package aggregation;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.model.events.userprofiles.UserProfileEvent;

public class ImportBatch {
	
	private final Collection<IDEEvent> events;
	private final String username;
	private Instant first = null;
	private Instant last = null;
	private final Collection<LocalDate> dates;
	
	public ImportBatch(Collection<IDEEvent> events) {
		this.events = events.stream().filter(i->i!=null).collect(Collectors.toList());
		username = events.stream().filter(e->e instanceof UserProfileEvent).findAny().map(e->((UserProfileEvent)e).ProfileId).orElse(null);
		first = this.events.stream().map(e->e.TriggeredAt.toInstant()).min(INSTANT_COMPARATOR).orElse(null);
		last = this.events.stream().map(e->e.TriggeredAt.toInstant()).max(INSTANT_COMPARATOR).orElse(null);
		dates = this.events.stream().map(e->e.TriggeredAt.toLocalDate()).distinct().collect(Collectors.toList());
	}
	
	public boolean batchIsValid() {
		return username!=null && first!=null && last!=null && dates.size()>0;
	}
	
	public Collection<LocalDate> getDates() {
		return dates;
	}

	public Collection<IDEEvent> getEvents() {
		return events;
	}

	public String getUsername() {
		return username;
	}

	public Instant getFirst() {
		return first;
	}

	public Instant getLast() {
		return last;
	}

	public static final Comparator<Instant> INSTANT_COMPARATOR = new Comparator<Instant>() {
		@Override
		public int compare(Instant o1, Instant o2) {
			return o1.compareTo(o2);
		}
	};
	
}
