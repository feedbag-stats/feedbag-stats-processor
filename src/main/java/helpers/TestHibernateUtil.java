package helpers;

import java.util.Properties;

import entity.*;
import entity.activity.ActivityEntry;
import entity.activity.ActivityInterval;
import entity.activity.TestingStateTimestamp;
import entity.location.LocationInterval;
import entity.location.LocationTimestamp;
import entity.tdd.DailyTDDCycles;
import entity.tdd.FileEditTimestamp;
import entity.tdd.TestResultTimestamp;
import entity.various.BuildTimestamp;
import entity.various.CommitTimestamp;
import entity.various.DailyVariousStats;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

public class TestHibernateUtil {
	private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            createSessionFactory();
        }
        return sessionFactory;
    }
    
    private static void createSessionFactory() {
    	try {
            Configuration configuration = new Configuration();
            // Hibernate settings equivalent to hibernate.cfg.xml's properties
            Properties settings = new Properties();
            settings.put(Environment.DRIVER, "org.h2.Driver");
            settings.put(Environment.URL, "jdbc:h2:mem:play");
            settings.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");
            settings.put(Environment.SHOW_SQL, "true");
            settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
            settings.put(Environment.HBM2DDL_AUTO, "create");
            configuration.setProperties(settings);
            configuration.addAnnotatedClass(ActivityInterval.class);
            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(LocationInterval.class);
            configuration.addAnnotatedClass(TestingStateTimestamp.class);
            configuration.addAnnotatedClass(FileEditTimestamp.class);
            configuration.addAnnotatedClass(TestResultTimestamp.class);
            configuration.addAnnotatedClass(EventTimeStamp.class);
            configuration.addAnnotatedClass(DailyVariousStats.class);
            configuration.addAnnotatedClass(DailyTDDCycles.class);
            configuration.addAnnotatedClass(ActivityEntry.class);
            configuration.addAnnotatedClass(BuildTimestamp.class);
            configuration.addAnnotatedClass(CommitTimestamp.class);
            configuration.addAnnotatedClass(LocationTimestamp.class);
            configuration.addAnnotatedClass(AllEvents.class);
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
