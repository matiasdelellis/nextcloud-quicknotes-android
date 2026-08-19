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

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * A note, as whoever asked for it sees it.
 *
 * Some of what comes back is the note's and the same for everybody — title,
 * content, color, attachments — and some belongs to the caller alone:
 * isPinned, tags, reminderAt and archivedAt are read and written per user, so
 * two people see their own on the same note. deletedAt is the note's, and only
 * its owner can set it.
 */
public class Note implements Serializable {
    /** A note that has not been saved yet. */
    public static final int NO_ID = 0;

    @Expose
    @SerializedName("id") private int id = NO_ID;

    @Expose
    @SerializedName("title") private String title = "";

    @Expose
    @SerializedName("content") private String content = "";

    @Expose
    @SerializedName("isPinned") private boolean isPinned;

    @Expose
    @SerializedName("timestamp") private int timestamp;

    @Expose
    @SerializedName("color") private String color;

    @Expose
    @SerializedName("tags") private List<Tag> tags = new ArrayList<>();

    @Expose
    @SerializedName("attachments") private List<Attachment> attachments = new ArrayList<>();

    @Expose
    @SerializedName("archivedAt") private String archivedAt;

    @Expose
    @SerializedName("deletedAt") private String deletedAt;

    @Expose
    @SerializedName("reminderAt") private String reminderAt;

    @Expose
    @SerializedName("reminderNotifiedAt") private String reminderNotifiedAt;

    @Expose
    @SerializedName("owner") private User owner;

    @Expose
    @SerializedName("isOwner") private boolean isOwner = true;

    @Expose
    @SerializedName("canLeave") private boolean canLeave;

    @Expose
    @SerializedName("permissions") private int permissions = Permissions.CAN_EDIT_AND_RESHARE;

    @Expose
    @SerializedName("canEdit") private boolean canEdit = true;

    @Expose
    @SerializedName("canReshare") private boolean canReshare = true;

    @Expose
    @SerializedName("sharedWith") private List<Share> sharedWith = new ArrayList<>();

    @Expose
    @SerializedName("sharedBy") private User sharedBy;

    @Expose
    @SerializedName("sharedByMe") private boolean sharedByMe;

    @Expose
    @SerializedName("etag") private String etag;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content != null ? content : "";
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        this.isPinned = pinned;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @NotNull
    public List<Tag> getTags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    @NotNull
    public List<Attachment> getAttachments() {
        if (attachments == null) {
            attachments = new ArrayList<>();
        }
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    /** When the caller archived it, in UTC, or null. */
    public String getArchivedAt() {
        return archivedAt;
    }

    public boolean isArchived() {
        return archivedAt != null && !archivedAt.isEmpty();
    }

    /** When its owner moved it to the trash, in UTC, or null. */
    public String getDeletedAt() {
        return deletedAt;
    }

    public boolean isTrashed() {
        return deletedAt != null && !deletedAt.isEmpty();
    }

    /** The caller's own reminder, in UTC, or null. */
    public String getReminderAt() {
        return reminderAt;
    }

    public void setReminderAt(String reminderAt) {
        this.reminderAt = reminderAt;
    }

    public boolean hasReminder() {
        return reminderAt != null && !reminderAt.isEmpty();
    }

    public String getReminderNotifiedAt() {
        return reminderNotifiedAt;
    }

    /** A reminder that is set and whose notification has not gone out yet. */
    public boolean isReminderPending() {
        return hasReminder() && (reminderNotifiedAt == null || reminderNotifiedAt.isEmpty());
    }

    public User getOwner() {
        return owner;
    }

    public boolean isOwner() {
        return isOwner;
    }

    /**
     * Whether there is a share of the caller's own to walk away from. A note
     * reaching them through a group is archived instead.
     */
    public boolean canLeave() {
        return canLeave;
    }

    public int getPermissions() {
        return permissions;
    }

    public boolean canEdit() {
        return canEdit;
    }

    public boolean canReshare() {
        return canReshare;
    }

    /** Only filled in for the owner and for somebody who may reshare. */
    @NotNull
    public List<Share> getSharedWith() {
        if (sharedWith == null) {
            sharedWith = new ArrayList<>();
        }
        return sharedWith;
    }

    public void setSharedWith(List<Share> sharedWith) {
        this.sharedWith = sharedWith;
    }

    /** Who shared it with the caller; null on their own note. */
    public User getSharedBy() {
        return sharedBy;
    }

    /** Whether somebody else shared this note with the caller. */
    public boolean isSharedWithMe() {
        return sharedBy != null;
    }

    /** Their own note, and somebody else has it. */
    public boolean isSharedByMe() {
        return sharedByMe || !getSharedWith().isEmpty();
    }

    /**
     * Derived from the stored note, to send back in If-Match when saving so a
     * concurrent edit is reported instead of silently overwritten.
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Moves this note onto the version the server currently holds, so a save
     * that lost a race can be retried on purpose rather than refused again.
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * Takes over everything the server owns about this note, and leaves alone
     * everything the user is in the middle of writing.
     *
     * Setting a reminder, archiving or trashing a note all answer with the
     * whole note. Swapping this object for that answer would throw away
     * whatever is in the editor at the time, so only the fields the editor
     * never writes are taken across. The etag is among them and stays right:
     * the server derives it from the title, the content and the time of the
     * last change, none of which any of those calls touch.
     */
    public void mergeServerState(@NotNull Note updated) {
        this.timestamp = updated.timestamp;
        this.etag = updated.etag;
        this.archivedAt = updated.archivedAt;
        this.deletedAt = updated.deletedAt;
        this.reminderAt = updated.reminderAt;
        this.reminderNotifiedAt = updated.reminderNotifiedAt;
        this.owner = updated.owner;
        this.isOwner = updated.isOwner;
        this.canLeave = updated.canLeave;
        this.permissions = updated.permissions;
        this.canEdit = updated.canEdit;
        this.canReshare = updated.canReshare;
        this.sharedWith = updated.sharedWith;
        this.sharedBy = updated.sharedBy;
        this.sharedByMe = updated.sharedByMe;
    }

    public boolean isNew() {
        return id == NO_ID;
    }

    public static final Comparator<Note> ByTitleAZ = (note, other) ->
            note.getTitle().compareToIgnoreCase(other.getTitle());

    public static final Comparator<Note> ByLastUpdated = (note, other) ->
            Integer.compare(other.timestamp, note.timestamp);

    public static final Comparator<Note> ByLastCreated = (note, other) ->
            Integer.compare(other.id, note.id);

    public static final Comparator<Note> ByPinned = (note, other) ->
            Boolean.compare(other.isPinned, note.isPinned);

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, isPinned, color, tags, attachments);
    }

    /**
     * Compare the fields the editor can change. Anything the server owns —
     * timestamps, shares, permissions — is left out, so that reading a note
     * back does not look like a modification.
     *
     * @param obj Note to compare with
     * @return true if there is no difference in the relevant fields
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Note other = (Note) obj;

        return Objects.equals(other.getTitle(), getTitle()) &&
               Objects.equals(other.getContent(), getContent()) &&
               isPinned == other.isPinned &&
               Objects.equals(other.getColor(), color) &&
               Objects.equals(other.getTags(), getTags()) &&
               Objects.equals(other.getAttachments(), getAttachments());
    }

    /**
     * serialize the object and create a new object from the serialization string
     * @return a new Note object which is a copy from this but no reference
     */
    @NotNull
    @Override
    public Note clone() {
        Gson gson = new Gson(); // We create a new instance every time to save some memory... :)
        return gson.fromJson(gson.toJson(this), Note.class);
    }
}
