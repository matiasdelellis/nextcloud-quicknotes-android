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

package ar.com.delellis.quicknotes.activity.main;

import java.util.List;

import ar.com.delellis.quicknotes.model.Note;

public interface MainView {
    void showLoading();
    void hideLoading();
    void onGetResult(List<Note> notes);
    void onErrorLoading(String errorMessage);

    /** One note came back changed, and the list has to show it that way. */
    void onNoteUpdated(Note note);

    /** One note is not in the list any more: destroyed, or left. */
    void onNoteRemoved(int noteId);

    /** How many notes emptying the trash destroyed. */
    void onTrashEmptied(int destroyed);

    /** An action on a single note did not go through. */
    void onActionError(String errorMessage);
}
