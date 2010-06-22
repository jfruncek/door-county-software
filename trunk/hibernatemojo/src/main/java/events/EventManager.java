package events;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import util.HibernateUtil;

public class EventManager {

    public static void main(String[] args) {
        EventManager mgr = new EventManager();

        if (args[0].equals("store")) {
            mgr.createAndStoreEvent(args[1], new Date());
        }
        else if (args[0].equals("list")) {
            List events = mgr.listEvents();
            for (int i = 0; i < events.size(); i++) {
                Event theEvent = (Event) events.get(i);
                System.out.println("Event: " + theEvent.getTitle() +
                                   " Time: " + theEvent.getDate());
            }
        }
        HibernateUtil.getSessionFactory().close();
    }

    public Event createAndStoreEvent(String title, Date theDate) {

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        session.beginTransaction();

        Event event = new Event();
        event.setTitle(title);
        event.setDate(theDate);

        session.save(event);

        session.getTransaction().commit();
        
        return event;
    }

    public Person createAndStorePerson(String lastname, String firstname, int age) {

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        session.beginTransaction();

        Person person = new Person();
        person.setFirstname(firstname);
        person.setLastname(lastname);
        person.setAge(age);

        session.save(person);

        session.getTransaction().commit();
        
        return person;
    }
    
    public Person addPersonToEvent(Long personId, Long eventId) {

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        Person aPerson = (Person) session.load(Person.class, personId);
        Event anEvent = (Event) session.load(Event.class, eventId);

        aPerson.getEvents().add(anEvent);

        session.getTransaction().commit();
        
        return aPerson;
    }

    public List listEvents() {

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        session.beginTransaction();

        List result = session.createQuery("from Event").list();

        session.getTransaction().commit();

        return result;
    }

    public List listPersons() {

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        session.beginTransaction();

        List result = session.createQuery("from Person").list();

        session.getTransaction().commit();

        return result;
    }

    public Person getPerson(long personId) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        Person aPerson = (Person) session.load(Person.class, personId);

        session.getTransaction().commit();
        
        return aPerson;
    	
    }
    
    public void updatePerson(Person person) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        session.update(person); // Reattachment of aPerson

        session.getTransaction().commit();
    }
}