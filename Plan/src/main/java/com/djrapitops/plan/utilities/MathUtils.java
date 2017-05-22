package main.java.com.djrapitops.plan.utilities;

import java.io.Serializable;
import java.util.Collection;
import java.util.OptionalDouble;
import java.util.stream.Stream;

/**
 *
 * @author Rsl1122
 */
public class MathUtils {

    public static double averageInt(Stream<Integer> values) {
        OptionalDouble average = values.mapToInt(i -> i).average();
        if (average.isPresent()) {
            return average.getAsDouble();
        } else {
            return 0;
        }
    }

    public static long averageLong(Collection<Long> values) {
        return averageLong(values.stream());
    }

    public static long averageLong(Stream<Long> values) {
        OptionalDouble average = values.mapToLong(i -> i).average();
        if (average.isPresent()) {
            return (long) average.getAsDouble();
        } else {
            return 0L;
        }
    }

    public static double averageDouble(Stream<Double> values) {
        OptionalDouble average = values.mapToDouble(i -> i).average();
        if (average.isPresent()) {
            return average.getAsDouble();
        } else {
            return 0;
        }
    }

    public static double average(int total, int size) {
        return 1.0 * total / size;
    }

    public static long countTrueBoolean(Stream<Boolean> values) {
        return values.filter(i -> i).count();
    }

    public static int sumInt(Stream<Serializable> values) {
        return values
                .mapToInt(value -> (Integer) value)
                .sum();
    }

    public static long sumLong(Stream<Serializable> values) {
        return values
                .mapToLong(value -> (Long) value)
                .sum();
    }

    public static double sumDouble(Stream<Serializable> values) {
        return values
                .mapToDouble(value -> (Double) value)
                .sum();
    }

    public static int getBiggest(Collection<Integer> values) {
        int biggest = 1;
        for (Integer value : values) {
            if (value > biggest) {
                biggest = value;
            }
        }
        return biggest;
    }
    public static long getBiggestLong(Collection<Long> values) {
        long biggest = 1;
        for (Long value : values) {
            if (value > biggest) {
                biggest = value;
            }
        }
        return biggest;
    }
}
