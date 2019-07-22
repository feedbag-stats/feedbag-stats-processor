package aggregation;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;

import entity.User;
import entity.ZipMapping;
import entity.various.DailyVariousStats;
import helpers.TestHibernateUtil;

public class ProcessingManagerTest {
	
	private SessionFactory factory = TestHibernateUtil.getSessionFactory();
	private ProcessingManager manager;

	@Before
	public void setup() {
		manager = new ProcessingManager(factory);
	}
	
	@Test
	public void testRemoval() throws Exception {
		Transaction t = factory.getCurrentSession().beginTransaction();
		User user = DeltaImporter.getOrCreateUser(factory, "testRemoval");
		t.commit();
		
		String file1Original = "/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata/delete-1.zip";
		String file1 = "/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata/delete-1-copy.zip";
		String file2 = "/home/kitty/Desktop/uni/mp/feedbag-stats-processor/testdata/delete-2.zip";
		Files.copy(new File(file1Original).toPath(), new File(file1).toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		manager.importZip(file1);
		manager.importZip(file2);
		
		t = factory.getCurrentSession().beginTransaction();
		List<DailyVariousStats> dailies = factory.getCurrentSession()
				.createQuery("from DailyVariousStats d where d.user = :user", DailyVariousStats.class)
				.setParameter("user", user)
				.getResultList();
		t.commit();
		
		assertEquals(1, dailies.size());
		assertEquals(2, dailies.get(0).getBuildCount());
		
		t = factory.getCurrentSession().beginTransaction();
		List<ZipMapping> zips = factory.getCurrentSession()
				.createQuery("from ZipMapping z where z.zip = :zip", ZipMapping.class)
				.setParameter("zip", file1)
				.getResultList();
		zips.get(0).setMarkedForDelete(true);
		factory.getCurrentSession().saveOrUpdate(zips.get(0));
		t.commit();
		
		manager.removeMarkedData();
		
		t = factory.getCurrentSession().beginTransaction();
		dailies = factory.getCurrentSession()
				.createQuery("from DailyVariousStats d where d.user = :user", DailyVariousStats.class)
				.setParameter("user", user)
				.getResultList();
		t.commit();
		
		assertEquals(1, dailies.size());
		assertEquals(1, dailies.get(0).getBuildCount());
	}

}
