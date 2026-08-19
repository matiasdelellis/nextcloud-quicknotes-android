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

package ar.com.delellis.quicknotes.activity.editor;

import ar.com.delellis.quicknotes.model.AttachmentInfo;
import ar.com.delellis.quicknotes.model.Note;

public interface EditorView {
    void showProgress();
    void hideProgress();

    /** The note was saved, or destroyed, and the editor is done. */
    void onRequestSuccess(String message);

    void onRequestError(String message);

    /** A file was uploaded and is ready to be attached to the note. */
    void addAttachment(AttachmentInfo attachment);

    /** The note came back changed, and the editor stays open on it. */
    void onNoteUpdated(Note note);

    /**
     * Somebody else saved the note since it was read here.
     *
     * @param current the note as it is now on the server.
     */
    void onConflict(Note current);
}
