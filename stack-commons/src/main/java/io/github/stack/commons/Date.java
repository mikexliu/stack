package io.github.stack.commons;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Allows you to actually use Date class with swagger. By having a string constructor, we are able to use
 * correctly pass in a formatted string into the Date object.
 */
public class Date extends java.util.Date {

    private static final ISO8601DateFormat ISO_8601_DATE_FORMAT = new ISO8601DateFormat();

    public Date() {
        super(new java.util.Date().getTime());
    }

    public Date(final String source) throws ParseException {
        super(ISO_8601_DATE_FORMAT.parse(source).getTime());
    }

    public Date(final Long time) {
        super(time);
    }

    /**
     * Returns the difference in time between the given Date.
     *
     * @param compareTo
     * @param unit
     * @return
     */
    public long difference(final java.util.Date compareTo, final TimeUnit unit) {
        final long difference = compareTo.getTime() - getTime();
        return unit.convert(difference, TimeUnit.MILLISECONDS);
    }

    /**
     * @param unit
     * @return
     */
    public Date toNearest(final TimeUnit unit) {
        final long time = getTime();
        final long rounded = unit.toMillis(unit.convert(time, TimeUnit.MILLISECONDS));
        final long remainder = unit.toMillis(unit.convert(time - rounded + TimeUnit.MILLISECONDS.convert(1, unit) / 2, TimeUnit.MILLISECONDS));
        return new Date(rounded + remainder);
    }

    /**
     * @return
     */
    public static Date now() {
        return new Date();
    }

    /**
     *
     * @param time
     * @param unit
     * @return
     */
    public boolean isOlderThan(final long time, final TimeUnit unit) {
        return add(time, unit).getTime() <= now().getTime();
    }

    /**
     *
     * @param time
     * @param unit
     * @return
     */
    public boolean isYoungerThan(final long time, final TimeUnit unit) {
        return add(time, unit).getTime() > now().getTime();
    }
    /**
     *
     * @param duration
     * @return
     */
    public Date add(final Duration duration) {
        return add(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     *
     * @param time
     * @param unit
     * @return
     */
    public Date add(final long time, final TimeUnit unit) {
        return new Date(getTime() + unit.toMillis(time));
    }

    /**
     *
     * @param date
     * @return
     */
    public Date add(final Date date) {
        return add(date.getTime(), TimeUnit.MILLISECONDS);
    }

    /**
     *
     * @param duration
     * @return
     */
    public Date subtract(final Duration duration) {
        return subtract(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Returns a Date object that is <b>time</b> <b>unit</b> before now.
     *
     * @param time
     * @param unit
     * @return
     */
    public Date subtract(final long time, final TimeUnit unit) {
        return new Date(getTime() - unit.toMillis(time));
    }

    /**
     *
     * @param date
     * @return
     */
    public Date subtract(final Date date) {
        return subtract(date.getTime(), TimeUnit.MILLISECONDS);
    }

    /**
     * Converts a regular java.util.Date object into a Date object
     *
     * @param date
     * @return
     */
    public static Date from(final java.util.Date date) {
        if (date == null) {
            return null;
        }

        return from(date.toInstant());
    }

    public static Date from(final Instant instant) {
        return new Date(java.util.Date.from(instant).getTime());
    }

    @Override
    public String toString() {
        return ISO_8601_DATE_FORMAT.format(this);
    }

    public Duration toDuration() {
        return Duration.ofMillis(getTime());
    }
}