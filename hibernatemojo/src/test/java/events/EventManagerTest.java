package events;

import java.util.Date;
import java.util.List;

import util.HibernateUtil;
import junit.framework.TestCase;

public class EventManagerTest extends TestCase {

	EventManager mgr;
	
	@Override
	protected void setUp() throws Exception {
        mgr = new EventManager();
	}

	@Override
	protected void tearDown() throws Exception {
		HibernateUtil.getSessionFactory().close();
	}
	
   public void testAddEventToPerson() {
		Person person = mgr.createAndStorePerson("Fruncek", "John", 29);
		Event event = mgr.createAndStoreEvent("Event1", new Date());
		List events = mgr.listEvents();
		assertTrue("There is one event", events.size() == 1);
		List persons = mgr.listPersons();
		assertTrue("There is one person", persons.size() == 1);

		//person = mgr.addPersonToEvent(person.getId(), event.getId());
		person.getEvents().add(event);

		mgr.updatePerson(person);
//		person = mgr.getPerson(person.getId());
		assertEquals("One event added to the person", 1, person.getEvents().size());
	}
	
}
