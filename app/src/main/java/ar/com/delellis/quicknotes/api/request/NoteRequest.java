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

package ar.com.delellis.quicknotes.api.request;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import ar.com.delellis.quicknotes.model.Attachment;
import ar.com.delellis.quicknotes.model.Note;
import ar.com.delellis.quicknotes.model.Tag;

/**
 * The body of a create or a save.
 *
 * Title and content are required; the rest is optional and a field that is not
 * sent is left alone. Who may change what is the server's business: it drops
 * the colour of somebody who does not own the note rather than refusing the
 * save, and tags and the pin are personal to whoever is saving.
 */
public class NoteRequest {
    @SerializedName("title") private String title;

    @SerializedName("content") private String content;

    @SerializedName("color") private String color;

    @SerializedName("isPinned") private Boolean isPinned;

    @SerializedName("tags") private List<TagInput> tags;

    @SerializedName("attachments") private List<AttachmentInput> attachments;

    public static NoteRequest of(Note note) {
        NoteRequest request = new NoteRequest();
        request.title = note.getTitle();
        request.content = note.getContent();
        request.color = note.getColor();
        request.isPinned = note.isPinned();

        request.tags = new ArrayList<>();
        for (Tag tag : note.getTags()) {
            request.tags.add(TagInput.of(tag));
        }

        request.attachments = new ArrayList<>();
        for (Attachment attachment : note.getAttachments()) {
            request.attachments.add(AttachmentInput.of(attachment));
        }

        return request;
    }

    /**
     * A save that only carries the pin, for toggling it from the list without
     * touching anything else of the note.
     */
    public static NoteRequest pinOnly(Note note, boolean pinned) {
        NoteRequest request = new NoteRequest();
        request.title = note.getTitle();
        request.content = note.getContent();
        request.isPinned = pinned;
        return request;
    }
}
