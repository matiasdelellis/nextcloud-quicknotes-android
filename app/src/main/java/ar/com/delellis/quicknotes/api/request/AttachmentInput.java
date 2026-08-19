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

import ar.com.delellis.quicknotes.model.Attachment;

/**
 * An attachment on a save. Somebody else's is sent back as it came so the
 * server leaves it alone; the caller's own travels without a user_id.
 */
public class AttachmentInput {
    @SerializedName("file_id") private int fileId;

    @SerializedName("user_id") private String userId;

    public static AttachmentInput of(Attachment attachment) {
        AttachmentInput input = new AttachmentInput();
        input.fileId = attachment.getFileId();
        if (!attachment.isMine()) {
            input.userId = attachment.getUserId();
        }
        return input;
    }
}
