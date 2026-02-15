package BUS;

import INTERFACE.IBUS;
import java.util.ArrayList;

public abstract class BaseBUS<T, K> implements IBUS<T, K> {
    protected static final int DEFAULT_PAGE_SIZE = 20;

    @Override
    public abstract ArrayList<T> getAll();

    @Override
    public abstract T getById(K id);

    @Override
    public boolean exists(K id) {
        return getById(id) != null;
    }

    protected abstract K getKey(T obj);
}