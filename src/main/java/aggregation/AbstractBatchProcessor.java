package aggregation;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import cc.kave.commons.model.events.IDEEvent;
import entity.User;

public abstract class AbstractBatchProcessor {
	protected final SessionFactory factory;
	
	public AbstractBatchProcessor(SessionFactory factory) {
		this.factory = factory;
	}
	
	public User getOrCreateUser(String username) {
		List<User> users = factory.getCurrentSession().createQuery("from User where username = :username", User.class)
				.setParameter("username", username)
				.getResultList();
		if(users.isEmpty()) {
			 User u = new User();
			 u.setName(username);
			 u.setUsername(username);
			 u.setToken(username);
			 factory.getCurrentSession().save(u);
			 return u;
		} else {
			return users.get(0);
		}
	}
	
	public void process(ImportBatch batch) {
		Transaction t = factory.getCurrentSession().beginTransaction();
		User user = getOrCreateUser(batch.getUsername());
		prepare(user, batch.getFirst(), batch.getLast());
		addNewData(batch.getEvents());
		saveChanges();
		t.commit();
	}
	
	//prepare to add the new data. eg: load all existing data for the relevant time period
	protected abstract void prepare(User user, Instant begin, Instant end);
	//process the new data
	protected abstract void addNewData(Collection<IDEEvent> data);
	protected abstract void saveChanges();
}
