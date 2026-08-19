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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

/**
 * Somebody else having a note: one user, or one group.
 */
public class Share implements Serializable {
    public static final int SHARE_TYPE_USER = 0;
    public static final int SHARE_TYPE_GROUP = 1;

    @Expose
    @SerializedName("id") private int id;

    @Expose
    @SerializedName("noteId") private int noteId;

    @Expose
    @SerializedName("shareType") private int shareType = SHARE_TYPE_USER;

    @Expose
    @SerializedName("isGroup") private boolean isGroup;

    @Expose
    @SerializedName("shareWith") private String shareWith;

    @Expose
    @SerializedName("displayName") private String displayName;

    @Expose
    @SerializedName("permissions") private int permissions = Permissions.READ;

    @Expose
    @SerializedName("canEdit") private boolean canEdit;

    @Expose
    @SerializedName("canReshare") private boolean canReshare;

    @Expose
    @SerializedName("uidOwner") private String uidOwner;

    @Expose
    @SerializedName("ownerDisplayName") private String ownerDisplayName;

    @Expose
    @SerializedName("uidInitiator") private String uidInitiator;

    @Expose
    @SerializedName("createdAt") private long createdAt;

    public int getId() {
        return id;
    }

    public int getNoteId() {
        return noteId;
    }

    public int getShareType() {
        return shareType;
    }

    public boolean isGroup() {
        return isGroup || shareType == SHARE_TYPE_GROUP;
    }

    public String getShareWith() {
        return shareWith;
    }

    public void setShareWith(String shareWith) {
        this.shareWith = shareWith;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** The name to put on screen for whoever this share is with. */
    public String getLabel() {
        return (displayName != null && !displayName.isEmpty()) ? displayName : shareWith;
    }

    public int getPermissions() {
        return permissions;
    }

    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    public boolean canEdit() {
        return canEdit || Permissions.canEdit(permissions);
    }

    public boolean canReshare() {
        return canReshare || Permissions.canReshare(permissions);
    }

    public String getUidOwner() {
        return uidOwner;
    }

    public String getOwnerDisplayName() {
        return ownerDisplayName;
    }

    public String getUidInitiator() {
        return uidInitiator;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shareType, shareWith);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Share other = (Share) obj;
        // A share is the pair of who it is with and of what kind: the id is
        // not there yet on one that has just been made up in the client.
        return this.shareType == other.shareType && Objects.equals(this.shareWith, other.shareWith);
    }
}
