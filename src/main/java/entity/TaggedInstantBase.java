package entity;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class TaggedInstantBase {
	
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;
	
	@Column(nullable=false)
	protected  Instant instant;
	
	@ManyToOne(optional=false)
	protected User user;
	
	public static final Comparator<TaggedInstantBase> INSTANT_COMPARATOR = new Comparator<TaggedInstantBase>() {
		@Override
		public int compare(TaggedInstantBase o1, TaggedInstantBase o2) {
			return o1.instant.compareTo(o2.instant);
		}
	};
	
	public TaggedInstantBase() {}
	
	public TaggedInstantBase(Instant instant, User user) {
		this.instant = instant;
		this.user = user;
	}


	public Instant instant() {
		return instant;
	}
	
	public Date getDate() {
		return Date.from(instant);
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public String toString() {
		return "<"+instant.toString()+user.toString()+">";
	}
}
