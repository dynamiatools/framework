package tools.dynamia.commons.math;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for generating random numbers and strings.
 * Uses {@link ThreadLocalRandom} for better performance in concurrent environments.
 */
public class Randoms {

    /**
     * Returns a random integer between the specified origin (inclusive) and the specified bound (exclusive).
     *
     * @param min the least value returned
     * @param max the upper bound (exclusive)
     * @return a random integer between {@code min} (inclusive) and {@code max} (exclusive)
     */
    public static int nextInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    /**
     * Returns a random integer between 0 (inclusive) and the specified bound (exclusive).
     *
     * @param max the upper bound (exclusive)
     * @return a random integer between 0 (inclusive) and {@code max} (exclusive)
     */
    public static int nextInt(int max) {
        return ThreadLocalRandom.current().nextInt(max);
    }

    /**
     * Returns a random integer.
     *
     * @return a random integer
     */
    public static int nextInt() {
        return ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
    }

    /**
     * Returns a random long between the specified origin (inclusive) and the specified bound (exclusive).
     *
     * @param min the least value returned
     * @param max the upper bound (exclusive)
     * @return a random long between {@code min} (inclusive) and {@code max} (exclusive)
     */
    public static long nextLong(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min, max);
    }

    /**
     * Returns a random float between the specified origin (inclusive) and the specified bound (exclusive).
     *
     * @param min the least value returned
     * @param max the upper bound (exclusive)
     * @return a random float between {@code min} (inclusive) and {@code max} (exclusive)
     */
    public static float nextFloat(float min, float max) {
        return min + ThreadLocalRandom.current().nextFloat() * (max - min);
    }

    /**
     * Returns a random double between the specified origin (inclusive) and the specified bound (exclusive).
     *
     * @param min the least value returned
     * @param max the upper bound (exclusive)
     * @return a random double between {@code min} (inclusive) and {@code max} (exclusive)
     */
    public static double nextDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * Generates a random string of the specified length using alphanumeric characters.
     *
     * @param length the length of the random string
     * @return a random string of alphanumeric characters
     */
    public static String nextString(int length) {
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < length; i++) {
            int index = nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * Generates a random string of the specified length using numeric characters.
     *
     * @param length the length of the random string
     * @return a random string of numeric characters
     */
    public static String nextNumericString(int length) {
        StringBuilder sb = new StringBuilder();
        String chars = "0123456789";
        for (int i = 0; i < length; i++) {
            int index = nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * Generates a random string of the specified length using alphabetic characters.
     *
     * @param length the length of the random string
     * @return a random string of alphabetic characters
     */
    public static String nextAlphabeticString(int length) {
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < length; i++) {
            int index = nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * Generates a random string of the specified length using hexadecimal characters.
     *
     * @param length the length of the random string
     * @return a random string of hexadecimal characters
     */
    public static String nextHexString(int length) {
        StringBuilder sb = new StringBuilder();
        String chars = "0123456789ABCDEF";
        for (int i = 0; i < length; i++) {
            int index = nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * Generates a random hex color string (e.g., #FF0000).
     *
     * @return a random hex color string
     */
    public static String nextHexColor() {
        return "#" + nextHexString(6);
    }

    /**
     * Generates a random UUID string.
     *
     * @return a random UUID string
     */
    public static String nextUUID() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Returns a random name from a predefined list of names.
     *
     * @return a random name
     */
    public static String nextName() {
        String[] names = {"Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Hannah", "Ivy", "Jack",
                "Kathy", "Liam", "Mia", "Noah", "Olivia", "Paul", "Quinn", "Rachel", "Sam", "Tina",
                "Uma", "Victor", "Wendy", "Xander", "Yara", "Zane", "Aaron", "Bella", "Carter", "Diana", "Ethan", "Fiona",
                "Gavin", "Hailey", "Ian", "Jasmine", "Kevin", "Luna", "Mason", "Nora", "Owen", "Piper", "Quincy", "Ruby", "Sean", "Tara",
                "Mario", "Nina", "Leo", "Eva", "Jake", "Lily", "Cindy", "Derek", "Elena", "Felix", "Gloria"};
        return names[nextInt(names.length)];
    }

    /**
     * Returns a random element from the specified array.
     *
     * @param <T> the type of the elements
     * @param array the array to choose from
     * @return a random element from the array, or {@code null} if the array is null or empty
     */
    public static <T> T chooseRandom(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        int index = nextInt(array.length);
        return array[index];
    }

    /**
     * Returns a random element from the specified array, or the default value if the array is null or empty.
     *
     * @param <T> the type of the elements
     * @param array the array to choose from
     * @param defaultValue the value to return if the array is null or empty
     * @return a random element from the array, or {@code defaultValue}
     */
    public static <T> T chooseRandom(T[] array, T defaultValue) {
        if (array == null || array.length == 0) {
            return defaultValue;
        }

        int index = nextInt(array.length);
        return array[index];
    }

    /**
     * Returns a random element from the specified list.
     *
     * @param <T> the type of the elements
     * @param list the list to choose from
     * @return a random element from the list, or {@code null} if the list is null or empty
     */
    public static <T> T chooseRandom(java.util.List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int index = nextInt(list.size());
        return list.get(index);
    }

    /**
     * Returns a random date between the specified start (inclusive) and end (exclusive) dates.
     *
     * @param startInclusive the start date (inclusive)
     * @param endExclusive the end date (exclusive)
     * @return a random date between {@code startInclusive} and {@code endExclusive}
     */
    public static LocalDate nextDate(LocalDate startInclusive, LocalDate endExclusive) {
        long startEpochDay = startInclusive.toEpochDay();
        long endEpochDay = endExclusive.toEpochDay();
        long randomDay = nextLong(startEpochDay, endEpochDay);
        return LocalDate.ofEpochDay(randomDay);
    }

    /**
     * Returns a random date in the past, up to the specified maximum number of days.
     *
     * @param maxDaysInPast the maximum number of days in the past
     * @return a random date in the past
     */
    public static LocalDate nextPastDate(int maxDaysInPast) {
        LocalDate today = LocalDate.now();
        long randomDays = nextLong(1, maxDaysInPast + 1);
        return today.minusDays(randomDays);
    }

    /**
     * Returns a random date in the future, up to the specified maximum number of days.
     *
     * @param maxDaysInFuture the maximum number of days in the future
     * @return a random date in the future
     */
    public static LocalDate nextFutureDate(int maxDaysInFuture) {
        LocalDate today = LocalDate.now();
        long randomDays = nextLong(1, maxDaysInFuture + 1);
        return today.plusDays(randomDays);
    }

    /**
     * Returns a random date-time between the specified start (inclusive) and end (exclusive) date-times.
     *
     * @param startInclusive the start date-time (inclusive)
     * @param endExclusive the end date-time (exclusive)
     * @return a random date-time between {@code startInclusive} and {@code endExclusive}
     */
    public static LocalDateTime nextDateTime(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        long startEpochSecond = startInclusive.toEpochSecond(java.time.ZoneOffset.UTC);
        long endEpochSecond = endExclusive.toEpochSecond(java.time.ZoneOffset.UTC);
        long randomSecond = nextLong(startEpochSecond, endEpochSecond);
        return LocalDateTime.ofEpochSecond(randomSecond, 0, java.time.ZoneOffset.UTC);
    }

    /**
     * Returns a random time of day.
     *
     * @return a random time
     */
    public static LocalTime nextTime() {
        int hour = nextInt(0, 24);
        int minute = nextInt(0, 60);
        int second = nextInt(0, 60);
        return LocalTime.of(hour, minute, second);
    }

    /**
     * Returns a random time between the specified minimum hour (inclusive) and maximum hour (exclusive).
     *
     * @param minHour the minimum hour (inclusive)
     * @param maxHour the maximum hour (exclusive)
     * @return a random time between {@code minHour} and {@code maxHour}
     */
    public static LocalTime nextTime(int minHour, int maxHour) {
        int hour = nextInt(minHour, maxHour);
        int minute = nextInt(0, 60);
        int second = nextInt(0, 60);
        return LocalTime.of(hour, minute, second);
    }
}
