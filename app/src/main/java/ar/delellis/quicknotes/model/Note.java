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

package ar.delellis.quicknotes.model;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

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

    /**
     * Compare specific fields from two Note elements.
     * If there is any difference or fields are present in one,
     * but not the other objects, this will return true.
     *
     * @param other Note to compare with
     * @return true if there is other difference in the relevant fields
     */
    public boolean compareBasicsWith(Note other) {

        // Lets first check if one field is null and the other not (difference between other and current)
        // This is so we don't run into other NPE later when accessing methods of fields.
        if (other.getTitle() == null ^ this.getTitle() == null
                || other.getTags() == null ^ this.getTags() == null
                || other.getColor() == null ^ this.getColor() == null
                || other.getAttachtments() == null ^ this.getAttachtments() == null
                || other.getContent() == null ^ this.getContent() == null) {
            return true;
        }

        // Now lets compare the fields itself
        return ((other.getTitle() != null
                && !other.getTitle().equals(this.getTitle()))
                || (other.getTags() != null
                && other.getTags().size() != this.getTags().size())
                || (other.getColor() != null
                && !other.getColor().equals(this.getColor()))
                || other.getAttachtments() != null
                && other.getAttachtments().size() != this.getAttachtments().size())
                || (other.getContent() != null
                && !other.getContent().equals(this.getContent()))
                || other.getIsPinned() != this.getIsPinned()
                || other.getIsShared() != this.getIsShared();
    }

    /**
     * serialize the object and create a new object from the serialization string
     * @return a new Note object which is a copy from this but no reference
     */
    public Note createCopy() {
        Gson gson = new Gson(); // We create a new instance every time to save some memory... :)
        return gson.fromJson(gson.toJson(this), Note.class);
    }

}
