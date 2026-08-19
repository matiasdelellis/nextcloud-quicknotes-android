/*
 * Nextcloud Quicknotes Android client application.
 *
 * @copyright Copyright (c) 2020 Matias De lellis <mati86dl@gmail.com>
 *
 * @author Matias De lellis <mati86dl@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ar.com.delellis.quicknotes.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;

/**
 * The datetimes the api speaks are UTC and shaped {@code YYYY-MM-DD HH:MM:SS}.
 * What the user reads is their own time zone, in their own locale.
 */
public final class DateUtil {

    private static final DateTimeFormatter API_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

    private DateUtil() {
    }

    /**
     * Reads a datetime as the api writes it, and answers when that was, or
     * null when there was nothing to read.
     */
    public static ZonedDateTime parseUtc(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), API_FORMAT).atZone(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Writes a moment the way the api wants it back: UTC, to the second.
     */
    public static String formatUtc(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.withZoneSameInstant(ZoneOffset.UTC).format(API_FORMAT);
    }

    public static String formatUtc(long epochMilli) {
        return formatUtc(Instant.ofEpochMilli(epochMilli).atZone(ZoneOffset.UTC));
    }

    /**
     * The same moment in the time zone of the phone, spelled out for reading.
     * Answers null when the value was not a datetime.
     */
    public static String toLocalDisplay(String utcValue) {
        ZonedDateTime dateTime = parseUtc(utcValue);
        if (dateTime == null) {
            return null;
        }
        return dateTime.withZoneSameInstant(ZoneId.systemDefault()).format(DISPLAY_FORMAT);
    }

    /** When this UTC datetime happened, or is going to, in milliseconds. */
    public static long toEpochMilli(String utcValue) {
        ZonedDateTime dateTime = parseUtc(utcValue);
        return dateTime != null ? dateTime.toInstant().toEpochMilli() : 0L;
    }

    public static boolean isInThePast(String utcValue) {
        ZonedDateTime dateTime = parseUtc(utcValue);
        return dateTime != null && dateTime.toInstant().isBefore(Instant.now());
    }
}
