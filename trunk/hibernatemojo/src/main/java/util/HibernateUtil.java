package util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import example.ReportSchedule;
import example.ScheduleParameter;

public class HibernateUtil {

    private static final SessionFactory sessionFactory;

    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void main(String[] arg) {
    	Session session = sessionFactory.openSession();
    	Transaction tx = session.beginTransaction();
    	ReportSchedule schedule = new ReportSchedule("schedule2");
    	ScheduleParameter parameter = new ScheduleParameter(new String[] { "v11", "v12" });
		schedule.addParameter("parm1", parameter);
    	session.saveOrUpdate(parameter);
		parameter = new ScheduleParameter(new String[] { "v21", "v22" });
		schedule.addParameter("parm2", parameter);
    	session.saveOrUpdate(parameter);
    	schedule.setDeliveryMethods(new String[] {"method1", "method2", "method3"});
    	session.saveOrUpdate(schedule);
    	tx.commit();
    	session.close();
    	sessionFactory.close();
    }
}