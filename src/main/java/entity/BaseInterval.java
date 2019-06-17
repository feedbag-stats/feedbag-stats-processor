package entity;

import java.time.Instant;
import java.util.Comparator;

import javax.persistence.*;

@Entity  
@Table(name = "baseinterval")  
@Inheritance(strategy=InheritanceType.JOINED)  
public abstract class BaseInterval {
	public static final Comparator<BaseInterval> BEGIN_COMPARATOR = new Comparator<BaseInterval>() {	
		@Override
		public int compare(BaseInterval o1, BaseInterval o2) {
			return o1.begin.compareTo(o2.begin);
		}
	};
	
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;
	
	@Column(nullable = false)
	protected Instant begin;
	
	@Column(nullable = false)
	protected Instant end;
	
	@ManyToOne(optional=false)
	protected final User user;
	
	public BaseInterval(Instant begin, Instant end, User user) {
		this.begin = begin;
		this.end = end;
		this.user = user;
	}
	
	public boolean contains(BaseInterval i) {
		return contains(i.begin()) && contains(i.end());
	}
	
	public boolean contains(Instant i) {
		boolean beginsBefore = begin.equals(i) || begin.isBefore(i);
		boolean endsAfter = end.equals(i) || end.isAfter(i);
		return beginsBefore && endsAfter; 
	}
	
	public void merge(BaseInterval i) {
		merge(i.begin());
		merge(i.end());
	}
	
	public void merge(Instant i) {
		this.begin = min(begin, i);
		this.end = max(end, i);
	}

	public abstract boolean canMerge(BaseInterval i);
	
	public abstract boolean canMerge(Instant i);

	public Instant begin() {
		return begin;
	}

	public void setBegin(Instant begin) {
		this.begin = begin;
	}

	public Instant end() {
		return end;
	}

	public void setEnd(Instant end) {
		this.end = end;
	}
	
	public static Instant min(Instant a, Instant b) {
		return a.isBefore(b) ? a : b;
	}
	
	public static Instant max(Instant a, Instant b) {
		return a.isAfter(b) ? a : b;
	}
	
}
