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

import ar.com.delellis.quicknotes.model.Sharee;

/**
 * Sharing a note with one user or one group.
 */
public class ShareRequest {
    @SerializedName("shareType") private int shareType;

    @SerializedName("shareWith") private String shareWith;

    @SerializedName("permissions") private int permissions;

    public ShareRequest(int shareType, String shareWith, int permissions) {
        this.shareType = shareType;
        this.shareWith = shareWith;
        this.permissions = permissions;
    }

    public static ShareRequest of(Sharee sharee, int permissions) {
        return new ShareRequest(sharee.getShareType(), sharee.getShareWith(), permissions);
    }
}
