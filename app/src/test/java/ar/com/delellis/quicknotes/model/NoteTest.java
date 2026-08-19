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

package ar.com.delellis.quicknotes.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NoteTest {

    private static Note noteWith(String title, String content) {
        Note note = new Note();
        note.setId(1);
        note.setTitle(title);
        note.setContent(content);
        note.setColor("#F7EB96");
        return note;
    }

    @Test
    public void whatTheEditorWritesIsWhatCountsAsAChange() {
        Note note = noteWith("Groceries", "<p>milk</p>");
        Note copy = note.clone();

        assertEquals(note, copy);

        copy.setContent("<p>milk, bread</p>");
        assertNotEquals(note, copy);
    }

    @Test
    public void whatTheServerOwnsIsNotAChange() {
        Note note = noteWith("Groceries", "<p>milk</p>");
        Note copy = note.clone();

        copy.setReminderAt("2026-08-24 12:00:00");
        assertEquals("A reminder is not something the editor wrote", note, copy);
    }

    @Test
    public void mergingKeepsWhatIsBeingWrittenAndTakesTheRest() {
        Note beingEdited = noteWith("Groceries", "<p>milk</p>");
        beingEdited.setTitle("Groceries for Sunday");

        Note fromServer = noteWith("Groceries", "<p>milk</p>");
        fromServer.setReminderAt("2026-08-24 12:00:00");
        fromServer.setEtag("abc123");

        beingEdited.mergeServerState(fromServer);

        assertEquals("Groceries for Sunday", beingEdited.getTitle());
        assertEquals("2026-08-24 12:00:00", beingEdited.getReminderAt());
        assertEquals("abc123", beingEdited.getEtag());
    }

    @Test
    public void tellsAPendingReminderFromOneThatAlreadyFired() {
        Note note = noteWith("Call back", "");
        assertFalse(note.hasReminder());

        note.setReminderAt("2026-08-24 12:00:00");
        assertTrue(note.hasReminder());
        assertTrue(note.isReminderPending());
    }

    @Test
    public void aNoteWithoutAnIdHasNotBeenSaved() {
        Note note = new Note();
        assertTrue(note.isNew());

        note.setId(7);
        assertFalse(note.isNew());
    }
}
