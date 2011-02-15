package problems.venkat.tdd.multithreaded;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import junit.framework.TestCase;

public class MultiValueMapTest extends TestCase {

    private static final String KEY = "k";
    MultiValueMap<String, String> _map;

    @Override
    protected void setUp() throws Exception {
        _map = new MultiValueMap<String, String>();
    }

    public void testMapEmptyUponCreate() {
        assertEquals(0, _map.getSize());
    }

    public void testMapReturnsEmptyListForNonExistentKey() {
        assertEquals(Collections.EMPTY_LIST, _map.get(""));
    }
    
    public void testAddValueToMap() {
        _map.put(KEY, "v1");
        assertNotNull(_map.get(""));
        assertEquals("map value list size:", 1, _map.get(KEY).size());
        assertEquals("v1", _map.get(KEY).get(0));
    }
    
    public void testAddMultiValueToMap() {
        _map.put(KEY, "v1");
        _map.put(KEY, "v2");
        assertEquals("map value list size:", 2, _map.get(KEY).size());
        assertEquals("v1", _map.get(KEY).get(0));
        assertEquals("v2", _map.get(KEY).get(1));
    }
    
    public void testCheckSizeAfterPuts() {
        _map.put("1", "one");
        _map.put("2", "two");
        _map.put("1", "uno");

        assertEquals(2, _map.getSize());
    }
  
  public void testEnsureGetValuesReturnsSynchronizedList() {
        _map.put("1", "one");
        List<String> values = _map.get("1");
    
        assertEquals(Collections.synchronizedList(values).getClass(), values.getClass());
  }
  
  class MockLock extends ReentrantLock {
    private static final long serialVersionUID = 1L;
    public boolean locked;
    public boolean unlocked;

    @Override public void lock() {
      locked = true;
    }

    @Override public void unlock() {
      unlocked = true;
    }
  }
  
  public void testPutIsMutuallyExclusive() {
    final MockLock mockLock = new MockLock();
    MultiValueMap<String, String> map = new MultiValueMap<String, String>() {
      @Override protected void putValue(String key, String value) {
        assertTrue(mockLock.locked);
        assertFalse(mockLock.unlocked);
      }
    };
    map.setLock(mockLock);

    assertFalse(mockLock.locked);
    assertFalse(mockLock.unlocked);

    map.put("3", "three");

    assertTrue(mockLock.locked);
    assertTrue(mockLock.unlocked);
  }
  
  public void testPutLockAndUnlockAreGuardedByTryFinally() {
    MultiValueMap<String, String> map = new MultiValueMap<String, String>() {
      @Override protected void putValue(String key, String value) {
        throw new RuntimeException("simulated exception");
      }
    };

    MockLock mockLock = new MockLock();
    map.setLock(mockLock);

    try {
      map.put("4", "four");
      fail("Expected the simulated exception!");
    } catch(RuntimeException ex) {
      // :) Expected
    }

    assertTrue(mockLock.locked);
    assertTrue(mockLock.unlocked);
  }
}
