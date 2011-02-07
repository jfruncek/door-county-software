package problems.venkat.tdd.multithreaded;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiValueMap<K, V> {

    Map<K, List<V>> _map = new HashMap<K, List<V>>();
    
    public int getSize() {
        return _map.size();
    }

    public List<V> get(K key) {
        if (_map.containsKey(key))
            return _map.get(key);
        else
            return Collections.emptyList();
    }

    public void put(K key, V value) {
        List<V> values = new ArrayList<V>();
        if (_map.containsKey(key)) values = _map.get(key);
        values.add(value);
        _map.put(key, values);
    }
}
