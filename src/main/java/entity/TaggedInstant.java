package entity;

import java.time.Instant;
import java.util.Comparator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;

@MappedSuperclass
public class TaggedInstant<T> {
	
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;
	
	@Column(nullable=false)
	private final Instant instant;
	
	@Any(metaColumn = @Column(name = "what_i_contain"))
	@Cascade(CascadeType.ALL)
    @AnyMetaDef(
        idType = "integer",
        metaType = "string",
        metaValues = {
            @MetaValue(value = "String", targetEntity = String.class),
            @MetaValue(value = "Boolean", targetEntity = Boolean.class)
        })
	@JoinColumn(name = "property_id")
	private final T tag;
	
	@Column(nullable=false)
	private User user;
	
	@SuppressWarnings("rawtypes")
	public static final Comparator<TaggedInstant> INSTANT_COMPARATOR = new Comparator<TaggedInstant>() {
		@Override
		public int compare(TaggedInstant o1, TaggedInstant o2) {
			return o1.instant.compareTo(o2.instant);
		}
	};
	
	public TaggedInstant(Instant instant, T tag, User user) {
		this.instant = instant;
		this.tag = tag;
		this.user = user;
	}


	public Instant instant() {
		return instant;
	}


	public T tag() {
		return tag;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public String toString() {
		return "<"+instant.toString()+","+tag.toString()+","+user.toString()+">";
	}
}
