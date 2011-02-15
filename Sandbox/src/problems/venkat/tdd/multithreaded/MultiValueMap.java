package problems.venkat.tdd.multithreaded;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MultiValueMap<K, V> {

    Map<K, List<V>> _map = new HashMap<K, List<V>>();
  private Lock _lock = new ReentrantLock();
    
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
        _lock.lock();
        try {
            putValue(key, value);
        } finally {
            _lock.unlock();
        }
    }

    protected void putValue(K key, V value) {
        List<V> values = Collections.synchronizedList(new ArrayList<V>());
        if (_map.containsKey(key)) values = _map.get(key);
        values.add(value);
        _map.put(key, values);
    }

    protected void setLock(Lock lock) {
        _lock = lock;
    }
}
