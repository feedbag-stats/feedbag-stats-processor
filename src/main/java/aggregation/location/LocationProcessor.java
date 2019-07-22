package aggregation.location;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import aggregation.DeltaImporter;
import aggregation.IDataProcessor;
import aggregation.ImportBatch;
import aggregation.activity.IntervalBuilder;
import entity.TaggedInstantBase;
import entity.User;
import entity.activity.ActivityEntry;
import entity.activity.ActivityInterval;
import entity.location.LocationInterval;
import entity.location.LocationTimestamp;

public class LocationProcessor implements IDataProcessor{
	
	private final SessionFactory factory;

	public LocationProcessor(SessionFactory factory) {
		this.factory = factory;
	}

	@Override
	public void updateData(ImportBatch batch) {
		Transaction t = factory.getCurrentSession().beginTransaction();
		try {
			User user = DeltaImporter.getOrCreateUser(factory, batch.getUsername());
			for(LocalDate day : batch.getDates()) {
				updateDay(user, day);
			}
			t.commit();
		} catch (Exception e) {
			e.printStackTrace();
			t.rollback();
		}
	}

	public void updateDay(User user, LocalDate day) {
		//remove previous intervals
		Collection<LocationInterval> oldIntervals = factory.getCurrentSession()
				.createQuery("from LocationInterval i where day(i.begin) = day(:date) and month(i.begin) = month(:date) and year(i.begin) = year(:date) and i.user = :user", LocationInterval.class)
				.setParameter("date", day)
				.setParameter("user", user)
				.getResultList();
		for(LocationInterval i : oldIntervals) {
			factory.getCurrentSession().delete(i);
		}
		
		Collection<LocationInterval> newIntervals = getLocationsForDay(user, day);
		for(LocationInterval l : newIntervals) {
			factory.getCurrentSession().save(l);
		}
	}

	private Collection<ActivityInterval> getIntervalsForDay(User user, LocalDate day) {
		IntervalBuilder builder = new IntervalBuilder(user);
		Collection<ActivityEntry> activities = factory.getCurrentSession().createQuery("from ActivityEntry i where day(i.instant) = day(:day) and month(i.instant) = month(:day) and year(i.instant) = year(:day) and i.user = :user", ActivityEntry.class)
				.setParameter("day", day)
				.setParameter("user", user)
				.getResultList();
		builder.addActivityEntries(activities);
		return builder.getVisibleIntervals();
	}
	
	private Collection<LocationInterval> getLocationsForDay(User user, LocalDate day) {
		Collection<LocationInterval> results = new HashSet<>();
		Collection<ActivityInterval> activeIntervals = getIntervalsForDay(user, day);
		for(LocationLevel l : LocationLevel.getAll()) {
			Collection<LocationTimestamp> allLocationChanges = factory.getCurrentSession().createQuery("from LocationTimestamp i where day(i.instant) = day(:day) and month(i.instant) = month(:day) and year(i.instant) = year(:day) and i.user = :user and i.level = :level", LocationTimestamp.class)
					.setParameter("day", day)
					.setParameter("user", user)
					.setParameter("level", l)
					.getResultList();
			for(ActivityInterval i : activeIntervals) {
				TreeSet<LocationTimestamp> locationChangeTree = new TreeSet<>(TaggedInstantBase.INSTANT_COMPARATOR);
				locationChangeTree.addAll(allLocationChanges.stream().filter(t->(t.instant().isAfter(i.begin())&&t.instant().isBefore(i.end()))).collect(Collectors.toList()));
				Iterator<LocationTimestamp> locationChanges = locationChangeTree.iterator();
				String location = allLocationChanges.stream().filter(t->!t.instant().isAfter(i.begin())).max(TaggedInstantBase.INSTANT_COMPARATOR).map(t->t.getLocationName()).orElse("???");
				Instant intervalStart = i.begin();
				while(locationChanges.hasNext()) {
					LocationTimestamp newLocation = locationChanges.next();
					if(!newLocation.getLocationName().equals(location)) {
						results.add(new LocationInterval(intervalStart, newLocation.instant(), location, user, l));
						location = newLocation.getLocationName();
						intervalStart = newLocation.instant();
					}
				}
				results.add(new LocationInterval(intervalStart, i.end(), location, user, l));
			}
		}
		return results;
	}

}
