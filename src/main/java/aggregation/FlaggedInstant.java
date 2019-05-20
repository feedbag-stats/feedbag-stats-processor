package aggregation;

import java.time.Instant;
import java.util.Comparator;

public class FlaggedInstant {
	private final Instant instant;
	private final boolean flag;
	
	public static final Comparator<FlaggedInstant> COMPARATOR = new Comparator<FlaggedInstant>() {
		@Override
		public int compare(FlaggedInstant o1, FlaggedInstant o2) {
			return o1.instant.compareTo(o2.instant);
		}
	};
	
	public FlaggedInstant(Instant instant, boolean flag) {
		this.instant = instant;
		this.flag = flag;
	}


	public Instant instant() {
		return instant;
	}


	public boolean flag() {
		return flag;
	}
}
