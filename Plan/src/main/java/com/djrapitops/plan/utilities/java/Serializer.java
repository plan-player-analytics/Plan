package com.djrapitops.plan.utilities.java;

import com.djrapitops.plan.data.store.Type;

import java.io.*;

/**
 * Utility class for storing {@link Serializable} things.
 *
 * @author Rsl1122
 */
public class Serializer<T> {

    private final Type<T> type;

    public Serializer(Type<T> type) {
        this.type = type;
    }

    /**
     * Serializes an object.
     *
     * @param object Object to Serialize.
     * @return byte array that contains the serialized object.
     * @throws IOException              If output fails.
     * @throws NotSerializableException If object does not implement Serializable.
     */
    public byte[] serialize(T object) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (ObjectOutput oo = new ObjectOutputStream(out)) {
                oo.writeObject(object);
            }
            return out.toByteArray();
        }
    }

    /**
     * De-serializes an object.
     *
     * @param bytes byte array that contains the serialized object.
     * @return De-serialized object.
     * @throws IOException            If input fails.
     * @throws ClassNotFoundException If a Serialized class is not found.
     */
    public T deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInput oi = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (T) oi.readObject();
        }
    }
}