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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Note implements Serializable {
    @Expose
    @SerializedName("id")
    private int id;

    @Expose
    @SerializedName("title")
    private String title;

    @Expose
    @SerializedName("content")
    private String content;

    @Expose
    @SerializedName("isPinned")
    private boolean is_pinned;

    @Expose
    @SerializedName("color")
    private String color;

    @Expose
    @SerializedName("timestamp")
    private int timestamp;

    @Expose
    @SerializedName("sharedWith")
    private List<Share> share_with;

    @Expose
    @SerializedName("sharedBy")
    private List<Share> share_by;

    @Expose
    @SerializedName("tags")
    private List<Tag> tags;

    @Expose
    @SerializedName("attachments")
    private List<Attachment> attachments;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean getIsPinned() {
        return is_pinned;
    }

    public void setIsPinned(boolean is_pinned) {
        this.is_pinned = is_pinned;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public List<Share> getShareWith() {
        return share_with;
    }

    public void setShareWith(List<Share> share) {
        this.share_with = share;
    }

    public List<Share> getShareBy() {
        return share_by;
    }

    public void setShareBy(List<Share> share) {
        this.share_by = share;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Attachment> getAttachtments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public boolean getIsShared() {
        return share_by != null && !share_by.isEmpty();
    }

    public static Comparator<Note> ByTitleAZ = (note, t1) -> note.title.compareTo(t1.title);

    public static Comparator<Note> ByLastUpdated = (note, t1) -> t1.timestamp - note.timestamp;

    public static Comparator<Note> ByLastCreated = (note, t1) -> t1.id - note.id;

    public static Comparator<Note> ByPinned = (note, t1) -> {
        if (note.is_pinned && t1.is_pinned) {
            return 0;
        } else if (note.is_pinned) {
            return -1;
        } else if (t1.is_pinned) {
            return 1;
        }
        return 0;
    };

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, is_pinned, color, timestamp, share_with, share_by, tags, attachments);
    }

    /**
     * Compare specific fields from two Note elements.
     * If there is any difference or fields are present in one,
     * but not the other objects, this will return true.
     *
     * @param obj Note to compare with
     * @return true if there is no difference in the relevant fields
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        Note other = (Note) obj;

        return ((Objects.equals(other.getTitle(), title)) &&
                (Objects.equals(other.getContent(), content)) &&
                (is_pinned == other.getIsPinned()) &&
                (Objects.equals(other.getColor(), color)) &&
                (Objects.equals(other.getShareWith(), share_with)) &&
                (Objects.equals(other.getShareBy(), share_by)) &&
                (Objects.equals(other.getTags(), tags)) &&
                (Objects.equals(other.getAttachtments(), attachments)));
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
