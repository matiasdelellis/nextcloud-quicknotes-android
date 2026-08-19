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

package ar.com.delellis.quicknotes.activity.shares;

import java.util.List;

import ar.com.delellis.quicknotes.model.Share;
import ar.com.delellis.quicknotes.model.Sharee;

public interface SharesView {
    void showLoading();
    void hideLoading();

    /** Who has the note right now. */
    void onSharesLoaded(List<Share> shares);

    /** Who the note could still be shared with, for the search that asked. */
    void onShareesLoaded(List<Sharee> sharees);

    void onShareCreated(Share share);
    void onShareUpdated(Share share);
    void onShareDeleted(int shareId);

    void onError(@androidx.annotation.Nullable String message);
}
