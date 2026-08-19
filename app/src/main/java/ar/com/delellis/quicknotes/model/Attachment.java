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
 * A file hanging off a note. Everything the client needs to show it is served
 * by the app itself, not by Files: a recipient of a shared note cannot reach
 * the file there.
 */
public class Attachment implements Serializable {
    @Expose
    @SerializedName("id") private int id;

    @Expose
    @SerializedName("note_id") private int noteId;

    @Expose
    @SerializedName("file_id") private int fileId;

    @Expose
    @SerializedName("created_at") private long createdAt;

    @Expose
    @SerializedName("user_id") private String userId;

    @Expose
    @SerializedName("is_mine") private boolean isMine;

    @Expose
    @SerializedName("has_preview") private boolean hasPreview;

    @Expose
    @SerializedName("basename") private String basename;

    @Expose
    @SerializedName("mime") private String mime;

    @Expose
    @SerializedName("preview_url") private String previewUrl;

    @Expose
    @SerializedName("download_url") private String downloadUrl;

    @Expose
    @SerializedName("redirect_url") private String redirectUrl;

    @Expose
    @SerializedName("deep_link_url") private String deepLinkUrl;

    @Expose
    @SerializedName("link_url") private String linkUrl;

    public Attachment() {
    }

    /**
     * The attachment a file just uploaded, or just picked out of Files, turns
     * into once it is put on a note.
     */
    public static Attachment fromInfo(AttachmentInfo info) {
        Attachment attachment = new Attachment();
        attachment.fileId = info.getFileId();
        attachment.basename = info.getBasename();
        attachment.mime = info.getMime();
        attachment.hasPreview = info.hasPreview();
        attachment.previewUrl = info.getPreviewUrl();
        attachment.redirectUrl = info.getRedirectUrl();
        attachment.deepLinkUrl = info.getDeepLinkUrl();
        attachment.linkUrl = info.getRedirectUrl();
        attachment.isMine = true;
        return attachment;
    }

    public int getId() {
        return id;
    }

    public int getNoteId() {
        return noteId;
    }

    public int getFileId() {
        return fileId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getUserId() {
        return userId;
    }

    /** Whether the caller is the one who attached it. */
    public boolean isMine() {
        return isMine;
    }

    /** Whether there is a real thumbnail behind the preview url. */
    public boolean hasPreview() {
        return hasPreview;
    }

    public String getBasename() {
        return basename;
    }

    public String getMime() {
        return mime;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getDeepLinkUrl() {
        return deepLinkUrl;
    }

    /**
     * Where a tap on the thumbnail should go: the file in Files when the
     * caller can reach it there, the download otherwise.
     */
    public String getLinkUrl() {
        if (linkUrl != null && !linkUrl.isEmpty()) {
            return linkUrl;
        }
        return redirectUrl != null ? redirectUrl : downloadUrl;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, userId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Attachment other = (Attachment) obj;
        return this.fileId == other.fileId && Objects.equals(this.userId, other.userId);
    }
}
