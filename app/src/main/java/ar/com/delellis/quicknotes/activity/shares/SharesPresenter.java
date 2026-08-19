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

import androidx.annotation.Nullable;

import java.util.List;

import ar.com.delellis.quicknotes.api.ApiProvider;
import ar.com.delellis.quicknotes.api.helper.ApiCallback;
import ar.com.delellis.quicknotes.api.request.PermissionsRequest;
import ar.com.delellis.quicknotes.api.request.ShareRequest;
import ar.com.delellis.quicknotes.model.Share;
import ar.com.delellis.quicknotes.model.Sharee;

/**
 * Who else has a note, and what they may do with it.
 */
public class SharesPresenter {
    /** What the collaborator search of the server is asked for at a time. */
    private static final int SHAREE_LIMIT = 25;

    private final SharesView view;
    private final int noteId;

    public SharesPresenter(SharesView view, int noteId) {
        this.view = view;
        this.noteId = noteId;
    }

    public void getShares() {
        view.showLoading();

        ApiProvider.getQuicknotesAPI().getShares(noteId).enqueue(new ApiCallback<List<Share>>() {
            @Override
            public void onSuccess(List<Share> shares) {
                view.hideLoading();
                view.onSharesLoaded(shares);
            }

            @Override
            public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                view.hideLoading();
                view.onError(message);
            }
        });
    }

    public void searchSharees(String search) {
        ApiProvider.getQuicknotesAPI().getSharees(noteId, search, SHAREE_LIMIT)
                .enqueue(new ApiCallback<List<Sharee>>() {
                    @Override
                    public void onSuccess(List<Sharee> sharees) {
                        view.onShareesLoaded(sharees);
                    }

                    @Override
                    public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                        view.onError(message);
                    }
                });
    }

    public void createShare(Sharee sharee, int permissions) {
        view.showLoading();

        ApiProvider.getQuicknotesAPI().createShare(noteId, ShareRequest.of(sharee, permissions))
                .enqueue(new ApiCallback<Share>() {
                    @Override
                    public void onSuccess(Share share) {
                        view.hideLoading();
                        if (share != null) {
                            view.onShareCreated(share);
                        }
                    }

                    @Override
                    public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                        view.hideLoading();
                        view.onError(message);
                    }
                });
    }

    public void updatePermissions(Share share, int permissions) {
        ApiProvider.getQuicknotesAPI().updateShare(share.getId(), new PermissionsRequest(permissions))
                .enqueue(new ApiCallback<Share>() {
                    @Override
                    public void onSuccess(Share updated) {
                        if (updated != null) {
                            view.onShareUpdated(updated);
                        }
                    }

                    @Override
                    public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                        view.onError(message);
                        // Put back whatever the server still holds.
                        getShares();
                    }
                });
    }

    public void deleteShare(Share share) {
        final int shareId = share.getId();
        ApiProvider.getQuicknotesAPI().deleteShare(shareId).enqueue(new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                view.onShareDeleted(shareId);
            }

            @Override
            public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                view.onError(message);
            }
        });
    }
}
