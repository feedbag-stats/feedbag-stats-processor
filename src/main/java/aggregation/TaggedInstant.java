package aggregation;

import java.time.Instant;
import java.util.Comparator;

public class TaggedInstant<T> {
	private final Instant instant;
	private final T tag;
	
	@SuppressWarnings("rawtypes")
	public static final Comparator<TaggedInstant> INSTANT_COMPARATOR = new Comparator<TaggedInstant>() {
		@Override
		public int compare(TaggedInstant o1, TaggedInstant o2) {
			return o1.instant.compareTo(o2.instant);
		}
	};
	
	public TaggedInstant(Instant instant, T tag) {
		this.instant = instant;
		this.tag = tag;
	}


	public Instant instant() {
		return instant;
	}


	public T tag() {
		return tag;
	}
	
	public String toString() {
		return "<"+instant.toString()+","+tag.toString()+">";
	}
}
