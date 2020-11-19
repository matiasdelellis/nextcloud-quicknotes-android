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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

public class Share implements Serializable {
    @Expose
    @SerializedName("id") private int id;

    @Expose
    @SerializedName("user_id") private String userId;

    @Expose
    @SerializedName("note_id") private String noteId;

    @Expose
    @SerializedName("shared_user") private String sharedUser;

    @Expose
    @SerializedName("shared_group") private String sharedGroup;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getSharedUser() {
        return sharedUser;
    }

    public void setSharedUser(String sharedUser) {
        this.sharedUser = sharedUser;
    }

    public String getSharedGroup() {
        return sharedGroup;
    }

    public void setSharedGroup(String sharedGroup) {
        this.sharedGroup = sharedGroup;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, noteId, sharedUser, sharedGroup);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Share oShare = (Share) obj;
        // The important is the shared user or group since the new shares always use the same empty id.
        return  (Objects.equals(this.sharedUser, oShare.getSharedUser()) &&
                 Objects.equals(this.sharedGroup, oShare.getSharedGroup()));
    }
}
