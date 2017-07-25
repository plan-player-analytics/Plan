package main.java.com.djrapitops.plan.utilities.analysis;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.stream.Stream;

/**
 * @author Rsl1122
 */
public class MathUtils {

    /**
     * Gets the average of a Stream of Integers.
     * If there are no components in the Stream, it will return 0.
     *
     * @param values The Stream of Integers.
     * @return The average
     */
    public static double averageInt(Stream<Integer> values) {
        OptionalDouble average = values.mapToInt(i -> i).average();

        return average.isPresent() ? average.getAsDouble() : 0;
    }

    /**
     * Gets the average of a Collection with Long as Entry.
     * If the collection is empty, it will return 0.
     *
     * @param values The Collection with Long as the Entry.
     * @return The average
     * @see #averageLong(Stream)
     */
    public static long averageLong(Collection<Long> values) {
        return averageLong(values.stream());
    }

    /**
     * Gets the average of a Stream of Longs.
     * If there are no components in the Stream, it will return 0.
     *
     * @param values The Stream of Longs.
     * @return The average
     * @see #averageLong(Collection)
     */
    public static long averageLong(Stream<Long> values) {
        OptionalDouble average = values.mapToLong(i -> i).average();

        return average.isPresent() ? (long) average.getAsDouble() : 0L;

    }

    /**
     * Gets the average of a Stream of Double.
     * If there are no components in the Stream, it will return 0.
     *
     * @param values The Stream of Double.
     * @return The average
     */
    public static double averageDouble(Stream<Double> values) {
        OptionalDouble average = values.mapToDouble(i -> i).average();

        return average.isPresent() ? average.getAsDouble() : 0;
    }

    /**
     * Calculates the average
     *
     * @param total The total summed amount of all Integers
     * @param size  The amount of all Integers that were summed
     * @return The average
     * @see #averageLong(long, long)
     */
    public static double average(int total, int size) {
        return (double) total / size;
    }

    /**
     * Calculates the average
     *
     * @param total The total summed amount of all Longs
     * @param size  The amount of all Longs that were summed
     * @return The average
     * @see #average(int, int)
     */
    public static long averageLong(long total, long size) {
        return total / size;
    }

    /**
     * Counts all Booleans that are true in the Stream of Booleans
     *
     * @param values The Stream of Booleans
     * @return The amount of Booleans that are true
     */
    public static long countTrueBoolean(Stream<Boolean> values) {
        return values.filter(value -> value).count();
    }

    /**
     * Sums all Integers in a Stream of Serializable
     *
     * @param values The Stream of Serializable
     * @return The sum
     * @see #sumLong(Stream)
     * @see #sumDouble(Stream)
     */
    public static int sumInt(Stream<Serializable> values) {
        return values
                .mapToInt(value -> (Integer) value)
                .sum();
    }

    /**
     * Sums all Longs in a Stream of Serializable
     *
     * @param values The Stream of Serializable
     * @return The sum
     * @see #sumInt(Stream)
     * @see #sumDouble(Stream)
     */
    public static long sumLong(Stream<Serializable> values) {
        return values
                .mapToLong(value -> (Long) value)
                .sum();
    }

    /**
     * Sums all Doubles in a Stream of Serializable
     *
     * @param values The Stream of Serializable
     * @return The sum
     * @see #sumLong(Stream)
     * @see #sumInt(Stream)
     */
    public static double sumDouble(Stream<Serializable> values) {
        return values
                .mapToDouble(value -> (Double) value)
                .sum();
    }

    /**
     * Gets the biggest Integer in a Collection with Integer as Entry
     * If the Collection is empty, it will return 0.
     *
     * @param values The Collection with Integer as the Entry
     * @return The biggest Integer
     * @see #getBiggestLong(Collection)
     */
    public static int getBiggest(Collection<Integer> values) {
        OptionalInt biggest = values.stream().mapToInt(i -> i).max();

        return biggest.isPresent() ? biggest.getAsInt() : 1;
    }

    /**
     * Gets the biggest Long in a Collection with Long as Entry
     * If the Collection is empty, it will return 0.
     *
     * @param values The Collection with Long as the Entry
     * @return The biggest Integer
     * @see #getBiggest(Collection)
     */
    public static long getBiggestLong(Collection<Long> values) {
        OptionalLong biggest = values.stream().mapToLong(i -> i).max();

        return biggest.isPresent() ? biggest.getAsLong() : 1;
    }

    private static final DecimalFormat df = new DecimalFormat("#'.'##");

    /**
     * Rounds the double to a double with two digits at the end.
     * Output: #.##
     *
     * @param number The number that's rounded
     * @return The rounded number
     */
    public static double round(double number) {
        return Double.valueOf(df.format(number));
    }
}
