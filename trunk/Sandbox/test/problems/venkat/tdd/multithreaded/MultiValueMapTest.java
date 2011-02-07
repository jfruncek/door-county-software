package problems.venkat.tdd.multithreaded;

import java.util.Collections;

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
        _map.put("", "");
        assertNotNull(_map.get(""));
        assertEquals("map value list size:", 1, _map.get("").size());
    }
    
    public void testAddMultiValueToMap() {
        _map.put(KEY, "v1");
        _map.put(KEY, "v2");
        assertEquals("map value list size:", 2, _map.get(KEY).size());
    }
}
