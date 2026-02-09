package BUS;

import INTERFACE.IBUS;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class BaseBUS<T, K> implements IBUS<T, K> {
    protected final ArrayList<T> arrLocal = new ArrayList<>();
    protected final HashMap<K, T> mapLocal = new HashMap<>();

    protected abstract K getKey(T obj);

    @Override
    public abstract ArrayList<T> getAll();

    @Override
    public ArrayList<T> getAllLocal() {
        return new ArrayList<>(arrLocal);
    }

    @Override
    public void loadLocal() {
        arrLocal.clear();
        mapLocal.clear();

        ArrayList<T> all = getAll();
        arrLocal.addAll(all);

        for (T obj : all) {
            mapLocal.put(getKey(obj), obj);
        }
    }

    @Override
    public boolean isLocalEmpty() {
        return arrLocal.isEmpty();
    }

    public T getByKeyLocal(K key) {
        return mapLocal.get(key);
    }

    public boolean containsKey(K key) {
        return mapLocal.containsKey(key);
    }

    /**
     * Lấy object từ mapLocal bằng ID (O(1) instead of O(n))
     * Tương đương getByIdLocal() trong các subclass
     * 
     * @param id ID của object cần lấy
     * @return object nếu tìm thấy, null nếu không
     */
    public T getByIdLocal(K id) {
        if (id == null)
            return null;
        return mapLocal.get(id);
    }

}
