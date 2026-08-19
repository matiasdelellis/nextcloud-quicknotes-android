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

/**
 * A file, before it is attached to anything: what an upload answers, and what
 * describes a file picked out of Files. Attaching it is a separate step.
 */
public class AttachmentInfo implements Serializable {
    @Expose
    @SerializedName("file_id") private int fileId;

    @Expose
    @SerializedName("basename") private String basename;

    @Expose
    @SerializedName("mime") private String mime;

    @Expose
    @SerializedName("has_preview") private boolean hasPreview;

    @Expose
    @SerializedName("preview_url") private String previewUrl;

    @Expose
    @SerializedName("redirect_url") private String redirectUrl;

    @Expose
    @SerializedName("deep_link_url") private String deepLinkUrl;

    public int getFileId() {
        return fileId;
    }

    public String getBasename() {
        return basename;
    }

    public String getMime() {
        return mime;
    }

    public boolean hasPreview() {
        return hasPreview;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getDeepLinkUrl() {
        return deepLinkUrl;
    }
}
