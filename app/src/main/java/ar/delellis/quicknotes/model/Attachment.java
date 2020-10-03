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

public class Attachment implements Serializable {
    @Expose
    @SerializedName("id") private int id;

    @Expose
    @SerializedName("note_id") private String note_id;

    @Expose
    @SerializedName("file_id") private String file_id;

    @Expose
    @SerializedName("create_at") private String created_at;

    @Expose
    @SerializedName("preview_url") private String preview_url;

    @Expose
    @SerializedName("redirect_url") private String redirect_url;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNoteId() {
        return note_id;
    }

    public void setNoteId(String note_id) {
        this.note_id = note_id;
    }

    public String getFileId() {
        return file_id;
    }

    public void setFileId(String file_id) {
        this.file_id = file_id;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    public String getPreviewUrl() {
        return preview_url;
    }

    public void setPreviewUrl(String preview_url) {
        this.preview_url = preview_url;
    }

    public String getRedirectUrl() {
        return redirect_url;
    }

    public void setRedirect_url(String redirect_url) {
        this.redirect_url = redirect_url;
    }
}
