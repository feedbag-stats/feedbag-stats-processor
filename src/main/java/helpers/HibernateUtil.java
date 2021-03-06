package helpers;

import entity.*;
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

import java.util.Properties;

public class HibernateUtil {
    public static final int BATCH_SIZE = 50;

    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();
                // Hibernate settings equivalent to hibernate.cfg.xml's properties
                Properties settings = new Properties();
                settings.put(Environment.DRIVER, "com.mysql.jdbc.Driver");
                settings.put(Environment.URL, "jdbc:mysql://feedbag.ch.inn.host.ch:3306/jkwebgm_feedbag?useSSL=false&rewriteBatchedStatements=true");
                settings.put(Environment.USER, "feedbag");
                settings.put(Environment.PASS, "Fq23rn9*");
                settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL5Dialect");
                settings.put(Environment.SHOW_SQL, "false");
                settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
                settings.put(Environment.HBM2DDL_AUTO, "update");
                settings.put(Environment.STATEMENT_BATCH_SIZE, BATCH_SIZE);
                settings.put(Environment.GENERATE_STATISTICS, true);
                settings.put(Environment.ORDER_INSERTS, true);
                settings.put(Environment.ORDER_UPDATES, true);
                configuration.setProperties(settings);
                configuration.addAnnotatedClass(ActivityInterval.class);
                configuration.addAnnotatedClass(User.class);
                configuration.addAnnotatedClass(LocationInterval.class);
                configuration.addAnnotatedClass(TestingStateTimestamp.class);
                configuration.addAnnotatedClass(FileEditTimestamp.class);
                configuration.addAnnotatedClass(TestResultTimestamp.class);
                configuration.addAnnotatedClass(EventTimeStamp.class);
                configuration.addAnnotatedClass(DailyTDDCycles.class);
                configuration.addAnnotatedClass(DailyVariousStats.class);
                configuration.addAnnotatedClass(BuildTimestamp.class);
                configuration.addAnnotatedClass(CommitTimestamp.class);
                configuration.addAnnotatedClass(LocationTimestamp.class);
                configuration.addAnnotatedClass(ActivityEntry.class);
                configuration.addAnnotatedClass(ZipMapping.class);
                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties()).build();
                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }
}