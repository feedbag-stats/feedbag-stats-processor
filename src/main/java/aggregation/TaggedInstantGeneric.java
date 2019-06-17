package aggregation;

import java.time.Instant;

import entity.TaggedInstantBase;

public class TaggedInstantGeneric<T> extends TaggedInstantBase {
	
	private T tag;
	
	public TaggedInstantGeneric(Instant instant, T tag) {
		super(instant, null);
		this.tag = tag;
	}
	
	public T tag() {
		return tag;
	}
}
