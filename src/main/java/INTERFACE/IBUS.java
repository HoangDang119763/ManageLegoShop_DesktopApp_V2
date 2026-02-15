package INTERFACE;

import java.util.ArrayList;

public interface IBUS<T, K> {
    ArrayList<T> getAll();

    T getById(K id);

    boolean exists(K id);
}