/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.database;

/**
 *
 * @author Rsl1122
 * @since 3.4.3
 * @param <T>
 */
public class Container<T> {
    private T object;
    private int id;

    public Container(T object, int id) {
        this.object = object;
        this.id = id;
    }

    public T getObject() {
        return object;
    }

    public int getId() {
        return id;
    }
}
