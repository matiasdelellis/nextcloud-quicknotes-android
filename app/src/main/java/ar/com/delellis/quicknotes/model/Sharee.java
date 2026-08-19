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
 * Somebody a note could still be shared with, as the collaborator search of
 * the server hands them over.
 */
public class Sharee implements Serializable {
    @Expose
    @SerializedName("shareType") private int shareType = Share.SHARE_TYPE_USER;

    @Expose
    @SerializedName("shareWith") private String shareWith;

    @Expose
    @SerializedName("label") private String label;

    @Expose
    @SerializedName("subline") private String subline;

    public int getShareType() {
        return shareType;
    }

    public boolean isGroup() {
        return shareType == Share.SHARE_TYPE_GROUP;
    }

    public String getShareWith() {
        return shareWith;
    }

    public String getLabel() {
        return (label != null && !label.isEmpty()) ? label : shareWith;
    }

    public String getSubline() {
        return subline;
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
        Sharee other = (Sharee) obj;
        return this.shareType == other.shareType && Objects.equals(this.shareWith, other.shareWith);
    }
}
