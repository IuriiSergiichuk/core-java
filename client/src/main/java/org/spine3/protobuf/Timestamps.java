/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.spine3.protobuf;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Timestamp;
import com.google.protobuf.TimestampOrBuilder;
import org.spine3.Internal;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.util.Timestamps.add;
import static com.google.protobuf.util.Timestamps.fromMillis;
import static com.google.protobuf.util.Timestamps.subtract;

/**
 * Utilities class for working with {@link Timestamp}s in addition to those available from
 * {@link com.google.protobuf.util.Timestamps}.
 *
 * @author Mikhail Melnik
 * @author Alexander Yevsyukov
 */
public class Timestamps {

    /**
     * The following constants are taken from {@link com.google.protobuf.util.Timestamps}
     * in order to make them publicly visible to time management utils:
     * <ul>
     *   <li>{@link #TIMESTAMP_SECONDS_MIN}
     *   <li>{@link #TIMESTAMP_SECONDS_MAX}
     *   <li>{@link #NANOS_PER_SECOND}
     *   <li>{@link #NANOS_PER_MILLISECOND}
     *   <li>{@link #NANOS_PER_MICROSECOND}
     *   <li>{@link #MILLIS_PER_SECOND}
     *   <li>{@link #MICROS_PER_SECOND}
     * </ul>
     * Consider removing these constants if they become public in the Protobuf utils API.
     **/

    /** Timestamp for "0001-01-01T00:00:00Z" */
    public static final long TIMESTAMP_SECONDS_MIN = -62135596800L;

    /** Timestamp for "9999-12-31T23:59:59Z" */
    public static final long TIMESTAMP_SECONDS_MAX = 253402300799L;

    /** The count of nanoseconds in one second. */
    public static final long NANOS_PER_SECOND = 1_000_000_000L;

    /** The count of nanoseconds in one millisecond. */
    public static final long NANOS_PER_MILLISECOND = 1_000_000L;

    /** The count of milliseconds in one second. */
    public static final long MILLIS_PER_SECOND = 1000L;

    /** The count of nanoseconds in a microsecond. */
    public static final long NANOS_PER_MICROSECOND = 1000L;

    /** The count of microseconds in one second. */
    public static final long MICROS_PER_SECOND = 1_000_000L;

    /** The count of seconds in one minute. */
    public static final int SECONDS_PER_MINUTE = 60;

    /** The count of seconds in one minute. */
    public static final int SECONDS_PER_HOUR = 3600;

    /** The count of minutes in one hour. */
    public static final int MINUTES_PER_HOUR = 60;

    /** The count of hours per day. */
    public static final int HOURS_PER_DAY = 24;

    private static final ThreadLocal<Provider> timeProvider = new ThreadLocal<Provider>() {
        @SuppressWarnings("RefusedBequest") // We want to provide our default value.
        @Override
        protected Provider initialValue() {
            return new SystemTimeProvider();
        }
    };

    private Timestamps() {
    }

    /**
     * Obtains current time.
     *
     * @return current time
     */
    public static Timestamp getCurrentTime() {
        final Timestamp result = timeProvider.get()
                                             .getCurrentTime();
        return result;
    }

    /**
     * Obtains system time.
     *
     * <p>Unlike {@link #getCurrentTime()} this method <strong>always</strong> uses system time millis.
     *
     * @return current system time
     */
    public static Timestamp systemTime() {
        return fromMillis(System.currentTimeMillis());
    }

    /**
     * The provider of the current time.
     *
     * <p>Implement this interface and pass the resulting class to
     */
    @Internal
    public interface Provider {
        Timestamp getCurrentTime();
    }

    /**
     * Sets provider of the current time.
     *
     * <p>The most common scenario for using this method is test cases of code that deals
     * with current time.
     *
     * @param provider the provider to set
     */
    @Internal
    @VisibleForTesting
    public static void setProvider(Provider provider) {
        timeProvider.set(checkNotNull(provider));
    }

    /**
     * Sets the default current time provider that obtains current time from system millis.
     */
    public static void resetProvider() {
        timeProvider.set(new SystemTimeProvider());
    }

    /**
     * Default implementation of current time provider based on {@link System#currentTimeMillis()}.
     *
     * <p>This is the only place, which should invoke obtaining current time from the system millis.
     */
    private static class SystemTimeProvider implements Provider {
        @Override
        public Timestamp getCurrentTime() {
            final Timestamp result = fromMillis(System.currentTimeMillis());
            return result;
        }
    }

    /**
     * Verifies if the passed {@code Timestamp} instance is valid.
     *
     * <p>The {@code seconds} field value must be within the range
     * {@code (TIMESTAMP_SECONDS_MIN, TIMESTAMP_SECONDS_MAX)}.
     *
     * <p>The {@code nanos} field value must be withing the range {@code (0, NANOS_PER_SECOND]}.
     *
     * @param value the value to check
     * @return the passed value
     * @throws IllegalArgumentException if {@link Timestamp} field values are not valid
     */
    public static Timestamp checkTimestamp(Timestamp value) throws IllegalArgumentException {
        final long seconds = value.getSeconds();
        checkArgument(seconds > TIMESTAMP_SECONDS_MIN && seconds < TIMESTAMP_SECONDS_MAX,
                      "Timestamp is out of range.");
        final int nanos = value.getNanos();
        checkArgument(nanos >= 0 && nanos <= NANOS_PER_SECOND,
                      "Timestamp has invalid nanos value.");
        return value;
    }

    /**
     * Compares two timestamps. Returns a negative integer, zero, or a positive integer
     * if the first timestamp is less than, equal to, or greater than the second one.
     *
     * @param t1 a timestamp to compare
     * @param t2 another timestamp to compare
     * @return a negative integer, zero, or a positive integer
     * if the first timestamp is less than, equal to, or greater than the second one
     */
    public static int compare(@Nullable Timestamp t1, @Nullable Timestamp t2) {
        if (t1 == null) {
            return (t2 == null) ? 0 : -1;
        }
        if (t2 == null) {
            return 1;
        }
        int result = Long.compare(t1.getSeconds(), t2.getSeconds());
        result = (result == 0)
                 ? Integer.compare(t1.getNanos(), t2.getNanos())
                 : result;
        return result;
    }

    /**
     * Calculates if the {@code timestamp} is between the {@code start} and {@code finish} timestamps.
     *
     * @param timestamp the timestamp to check if it is between the {@code start} and {@code finish}
     * @param start     the first point in time, must be before the {@code finish} timestamp
     * @param finish    the second point in time, must be after the {@code start} timestamp
     * @return true if the {@code timestamp} is after the {@code start} and before the {@code finish} timestamps,
     * false otherwise
     */
    public static boolean isBetween(Timestamp timestamp, Timestamp start, Timestamp finish) {
        final boolean isAfterStart = compare(start, timestamp) < 0;
        final boolean isBeforeFinish = compare(timestamp, finish) < 0;
        return isAfterStart && isBeforeFinish;
    }

    /**
     * Calculates if {@code timestamp} is later {@code thanTime} timestamp.
     *
     * @param timestamp the timestamp to check if it is later then {@code thanTime}
     * @param thanTime  the first point in time which is supposed to be before the {@code timestamp}
     * @return true if the {@code timestamp} is later than {@code thanTime} timestamp, false otherwise
     */
    public static boolean isLaterThan(Timestamp timestamp, Timestamp thanTime) {
        final boolean isAfter = compare(timestamp, thanTime) > 0;
        return isAfter;
    }

    /**
     * Compares two timestamps. Returns a negative integer, zero, or a positive integer
     * if the first timestamp is less than, equal to, or greater than the second one.
     *
     * @return a negative integer, zero, or a positive integer
     * if the first timestamp is less than, equal to, or greater than the second one
     */
    public static Comparator<? super Timestamp> comparator() {
        return new TimestampComparator();
    }

    /**
     * Converts a {@link Timestamp} to {@link Date} to the nearest millisecond.
     *
     * @return a {@link Date} instance
     */
    public static Date convertToDate(TimestampOrBuilder timestamp) {
        final long millisecsFromNanos = timestamp.getNanos() / NANOS_PER_MILLISECOND;
        final long millisecsFromSeconds = timestamp.getSeconds() * MILLIS_PER_SECOND;
        final Date date = new Date(millisecsFromSeconds + millisecsFromNanos);
        return date;
    }

    /**
     * Retrieves total nanoseconds from {@link Timestamp}.
     *
     * @return long value
     */
    public static long convertToNanos(TimestampOrBuilder timestamp) {
        final long nanosFromSeconds = timestamp.getSeconds() * MILLIS_PER_SECOND * NANOS_PER_MILLISECOND;
        final long totalNanos = nanosFromSeconds + timestamp.getNanos();
        return totalNanos;
    }

    private static class TimestampComparator implements Comparator<Timestamp>, Serializable {

        private static final long serialVersionUID = 0;

        @Override
        public int compare(Timestamp t1, Timestamp t2) {
            return Timestamps.compare(t1, t2);
        }
    }

    /**
     * The testing assistance utility, which returns a timestamp of the moment
     * of the passed number of minutes from now.
     *
     * @param value a positive number of minutes
     * @return a timestamp instance
     */
    @VisibleForTesting
    public static Timestamp minutesAgo(int value) {
        checkPositive(value);
        final Timestamp currentTime = getCurrentTime();
        final Timestamp result = subtract(currentTime, Durations.ofMinutes(value));
        return result;
    }

    /**
     * Obtains timestamp in the past a number of seconds ago.
     *
     * @param value a positive number of seconds
     * @return the moment `value` seconds ago
     */
    @VisibleForTesting
    public static Timestamp secondsAgo(long value) {
        checkPositive(value);
        final Timestamp currentTime = getCurrentTime();
        final Timestamp result = subtract(currentTime, Durations.ofSeconds(value));
        return result;
    }

    private static void checkPositive(long value) {
        if (value <= 0) {
            throw new IllegalArgumentException(String.format("value must be positive. Passed: %d", value));
        }
    }

    /**
     * Obtains timestamp in the future a number of seconds from current time.
     *
     * @param seconds a positive number of seconds
     * @return the moment `value` seconds from now
     */
    @VisibleForTesting
    public static Timestamp secondsFromNow(int seconds) {
        checkPositive(seconds);
        final Timestamp currentTime = getCurrentTime();
        final Timestamp result = add(currentTime, Durations.ofSeconds(seconds));
        return result;
    }
}
