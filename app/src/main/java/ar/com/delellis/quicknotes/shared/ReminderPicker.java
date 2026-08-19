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

package ar.com.delellis.quicknotes.shared;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.util.DateUtil;

/**
 * Asking the user when to be reminded: the day, then the time.
 *
 * What the api wants back is UTC to the second; what the user picks is their
 * own wall clock, so the two are put together in the time zone of the phone
 * and converted once at the end.
 */
public final class ReminderPicker {

    private static final String TAG_DATE = "reminder_date";
    private static final String TAG_TIME = "reminder_time";

    private ReminderPicker() {
    }

    /**
     * @param current the reminder as it stands, in UTC, or null for none.
     */
    public static void pick(@NonNull FragmentActivity activity,
                            @Nullable String current,
                            @NonNull OnReminderPicked listener) {
        ZonedDateTime existing = DateUtil.parseUtc(current);
        ZonedDateTime start = existing != null
                ? existing.withZoneSameInstant(ZoneId.systemDefault())
                : ZonedDateTime.now().plusHours(1).withMinute(0).withSecond(0);

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.select_reminder_date)
                .setSelection(start.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // The date picker speaks in UTC midnights, whatever the time zone.
            LocalDate day = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate();
            pickTime(activity, day, start.toLocalTime(), listener);
        });

        datePicker.show(activity.getSupportFragmentManager(), TAG_DATE);
    }

    private static void pickTime(@NonNull FragmentActivity activity,
                                 @NonNull LocalDate day,
                                 @NonNull LocalTime start,
                                 @NonNull OnReminderPicked listener) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(android.text.format.DateFormat.is24HourFormat(activity)
                        ? TimeFormat.CLOCK_24H
                        : TimeFormat.CLOCK_12H)
                .setTitleText(R.string.select_reminder_time)
                .setHour(start.getHour())
                .setMinute(start.getMinute())
                .build();

        timePicker.addOnPositiveButtonClickListener(view -> {
            ZonedDateTime when = ZonedDateTime.of(
                    day,
                    LocalTime.of(timePicker.getHour(), timePicker.getMinute()),
                    ZoneId.systemDefault());
            listener.onReminderPicked(DateUtil.formatUtc(when), when.toInstant().isAfter(Instant.now()));
        });

        timePicker.show(activity.getSupportFragmentManager(), TAG_TIME);
    }

    public interface OnReminderPicked {
        /**
         * @param reminderAt   UTC, {@code YYYY-MM-DD HH:MM:SS}.
         * @param isInTheFuture whether that moment has not happened yet. The
         *                      server takes a past one, but it would never
         *                      fire, so it is worth saying.
         */
        void onReminderPicked(@NonNull String reminderAt, boolean isInTheFuture);
    }
}
