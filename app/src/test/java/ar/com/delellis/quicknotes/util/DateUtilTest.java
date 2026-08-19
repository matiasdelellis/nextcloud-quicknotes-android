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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateUtilTest {

    @Test
    public void readsTheDatetimeShapeTheApiSpeaks() {
        ZonedDateTime parsed = DateUtil.parseUtc("2026-08-24 12:00:00");

        assertEquals(2026, parsed.getYear());
        assertEquals(8, parsed.getMonthValue());
        assertEquals(24, parsed.getDayOfMonth());
        assertEquals(12, parsed.getHour());
        assertEquals("Z", parsed.getZone().getId());
    }

    @Test
    public void writesBackWhatItRead() {
        assertEquals("2026-08-24 12:00:00", DateUtil.formatUtc(DateUtil.parseUtc("2026-08-24 12:00:00")));
    }

    @Test
    public void aLocalMomentIsWrittenInUtc() {
        // Nine in the morning in Buenos Aires is noon UTC.
        ZonedDateTime local = ZonedDateTime.of(2026, 8, 24, 9, 0, 0, 0,
                ZoneId.of("America/Argentina/Buenos_Aires"));

        assertEquals("2026-08-24 12:00:00", DateUtil.formatUtc(local));
    }

    @Test
    public void nothingToReadAnswersNothing() {
        assertNull(DateUtil.parseUtc(null));
        assertNull(DateUtil.parseUtc(""));
        assertNull(DateUtil.parseUtc("not a date"));
        assertNull(DateUtil.formatUtc(null));
        assertNull(DateUtil.toLocalDisplay("whenever"));
    }

    @Test
    public void tellsAPastReminderFromAFutureOne() {
        assertTrue(DateUtil.isInThePast("2000-01-01 00:00:00"));
        assertTrue(!DateUtil.isInThePast("2999-01-01 00:00:00"));
        // Something that is not a date has not happened.
        assertTrue(!DateUtil.isInThePast(null));
    }
}
