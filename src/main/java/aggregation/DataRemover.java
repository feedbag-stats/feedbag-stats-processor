package aggregation;

import java.io.File;
import java.util.Collection;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import entity.*;
import entity.activity.*;
import entity.location.*;
import entity.tdd.*;
import entity.various.*;

public class DataRemover {

	private final SessionFactory factory;
	
	public DataRemover(SessionFactory factory) {
		this.factory = factory;
	}
	
	public Collection<User> findUsersAffectedByRemoval() {
		Transaction t = factory.getCurrentSession().beginTransaction();
		Collection<User> result = factory.getCurrentSession().createQuery("select distinct user from ZipMapping z where z.markedForDelete is true", User.class).getResultList();
		t.commit();
		return result;
	}
	
	public Collection<String> findAllZipsForUsers(Collection<User> users) {
		Transaction t = factory.getCurrentSession().beginTransaction();
		Collection<String> result = factory.getCurrentSession().createQuery("select distinct zip from ZipMapping z where z.user in :user_list", String.class)
				.setParameterList("user_list", users)
				.getResultList();
		t.commit();
		return result;
	}
	
	public void deleteZipsToDelete() {
		Transaction t = factory.getCurrentSession().beginTransaction();
		Collection<String> zipsToDelete = factory.getCurrentSession().createQuery("select distinct zip from ZipMapping z where z.markedForDelete is true", String.class).getResultList();
		t.commit();
		for(String zip : zipsToDelete) {
			try {
				File file = new File(zip);
				file.delete();
			} catch (Exception e) {
				//file not found, already deleted
			}
		}
	}
	
	public void deleteAllDataForUsers(Collection<User> users) {
		Transaction t = factory.getCurrentSession().beginTransaction();
		Collection<ActivityEntry> activityEntry = factory.getCurrentSession()
				.createQuery("from ActivityEntry e where e.user in :user_list", ActivityEntry.class)
				.setParameterList("user_list", users)
				.getResultList();
		deleteAll(activityEntry);
		Collection<ActivityInterval> activityInterval = factory.getCurrentSession()
				.createQuery("from ActivityInterval e where e.user in :user_list", ActivityInterval.class)
				.setParameterList("user_list", users)
				.getResultList();
		deleteAll(activityInterval);
		Collection<TestingStateTimestamp> TestingStateTimestamp = factory.getCurrentSession()
				.createQuery("from TestingStateTimestamp e where e.user in :user_list", TestingStateTimestamp.class)
				.setParameterList("user_list", users)
				.getResultList();
		deleteAll(TestingStateTimestamp);
		Collection<LocationInterval> LocationInterval = factory.getCurrentSession()
				.createQuery("from LocationInterval e where e.user in :user_list", LocationInterval.class)
				.setParameterList("user_list", users)
				.getResultList();
		deleteAll(LocationInterval);
		Collection<LocationTimestamp> LocationTimestamp = factory.getCurrentSession()
				.createQuery("from LocationTimestamp e where e.user in :user_list", LocationTimestamp.class)
				.setParameterList("user_list", users)
				.getResultList();
		deleteAll(LocationTimestamp);
		Collection<DailyTDDCycles> DailyTDDCycles = factory.getCurrentSession()
				.createQuery("from DailyTDDCycles e where e.user in :user_list", DailyTDDCycles.class)
				.setParameterList("user_list", users)
				.getResultList();
		deleteAll(DailyTDDCycles);
		Collection<FileEditTimestamp> FileEditTimestamp = factory.getCurrentSession()
				.createQuery("from FileEditTimestamp e where e.user in :user_list", FileEditTimestamp.class)
				.setParameterList("user_list", users)
				.getResultList();
		deleteAll(FileEditTimestamp);
		Collection<TestResultTimestamp> TestResultTimestamp = factory.getCurrentSession()
				.createQuery("from TestResultTimestamp e where e.user in :user_list", TestResultTimestamp.class)
				.setParameterList("user_list", users)
				.getResultList();
		deleteAll(TestResultTimestamp);
		Collection<BuildTimestamp> BuildTimestamp = factory.getCurrentSession()
				.createQuery("from BuildTimestamp e where e.user in :user_list", BuildTimestamp.class)
				.setParameterList("user_list", users)
				.getResultList();
		deleteAll(BuildTimestamp);
		Collection<CommitTimestamp> CommitTimestamp = factory.getCurrentSession()
				.createQuery("from CommitTimestamp e where e.user in :user_list", CommitTimestamp.class)
				.setParameterList("user_list", users)
				.getResultList();
		deleteAll(CommitTimestamp);
		Collection<DailyVariousStats> DailyVariousStats = factory.getCurrentSession()
				.createQuery("from DailyVariousStats e where e.user in :user_list", DailyVariousStats.class)
				.setParameterList("user_list", users)
				.getResultList();
		deleteAll(DailyVariousStats);
		Collection<EventTimeStamp> EventTimeStamp = factory.getCurrentSession()
				.createQuery("from EventTimeStamp e where e.user in :user_list", EventTimeStamp.class)
				.setParameterList("user_list", users)
				.getResultList();
		deleteAll(EventTimeStamp);
		Collection<ZipMapping> ZipMapping = factory.getCurrentSession()
				.createQuery("from ZipMapping e where e.user in :user_list", ZipMapping.class)
				.setParameterList("user_list", users)
				.getResultList();
		deleteAll(ZipMapping);
		t.commit();
	}
	
	private void deleteAll(Collection<? extends Object> items) {
		for(Object o : items) {
			factory.getCurrentSession().delete(o);
		}
	}

}
