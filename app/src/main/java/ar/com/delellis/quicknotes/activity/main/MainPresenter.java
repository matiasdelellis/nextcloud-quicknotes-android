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

package ar.com.delellis.quicknotes.activity.main;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.net.HttpURLConnection;
import java.util.List;

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.api.ApiProvider;
import ar.com.delellis.quicknotes.api.helper.ApiCallback;
import ar.com.delellis.quicknotes.api.request.NoteRequest;
import ar.com.delellis.quicknotes.api.request.ReminderRequest;
import ar.com.delellis.quicknotes.model.Note;
import ar.com.delellis.quicknotes.model.TrashResult;
import retrofit2.Call;

/**
 * Reading the board, and everything that can be done to a note without
 * opening it.
 */
public class MainPresenter {
    private final MainView view;

    public MainPresenter(MainView view) {
        this.view = view;
    }

    public void getNotes() {
        view.showLoading();

        ApiProvider.getQuicknotesAPI().getNotes().enqueue(new ApiCallback<List<Note>>() {
            @Override
            public void onSuccess(List<Note> notes) {
                view.hideLoading();
                if (notes != null) {
                    view.onGetResult(notes);
                } else {
                    view.onErrorLoading(null);
                }
            }

            @Override
            public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                view.hideLoading();
                view.onErrorLoading(message);
            }
        });
    }

    /**
     * Reads one note, for a link that named it: the notification of a reminder
     * or of a note somebody shared, and the search results of the server, all
     * point at a single note by its id.
     */
    public void openNote(int noteId) {
        ApiProvider.getQuicknotesAPI().getNote(noteId).enqueue(new ApiCallback<Note>() {
            @Override
            public void onSuccess(Note note) {
                if (note != null) {
                    view.onNoteToOpen(note);
                } else {
                    view.onActionError(getString(R.string.error_note_not_found));
                }
            }

            @Override
            public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                view.onActionError(getString(R.string.error_note_not_found));
            }
        });
    }

    /** The pin is the caller's own, and saving it touches nothing else. */
    public void setPinned(Note note, boolean pinned) {
        Call<Note> call = ApiProvider.getQuicknotesAPI()
                .updateNote(note.getId(), note.getEtag(), NoteRequest.pinOnly(note, pinned));
        enqueueNoteAction(call);
    }

    public void archive(Note note) {
        enqueueNoteAction(ApiProvider.getQuicknotesAPI().archiveNote(note.getId()));
    }

    public void unarchive(Note note) {
        enqueueNoteAction(ApiProvider.getQuicknotesAPI().unarchiveNote(note.getId()));
    }

    public void trash(Note note) {
        enqueueNoteAction(ApiProvider.getQuicknotesAPI().trashNote(note.getId()));
    }

    public void restore(Note note) {
        enqueueNoteAction(ApiProvider.getQuicknotesAPI().restoreNote(note.getId()));
    }

    /**
     * @param reminderAt UTC, {@code YYYY-MM-DD HH:MM:SS}, or null to cancel it.
     */
    public void setReminder(Note note, String reminderAt) {
        enqueueNoteAction(ApiProvider.getQuicknotesAPI()
                .setReminder(note.getId(), new ReminderRequest(reminderAt)));
    }

    /** Destroys a note. Only its owner can. */
    public void destroy(Note note) {
        final int noteId = note.getId();
        ApiProvider.getQuicknotesAPI().destroyNote(noteId).enqueue(new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                view.onNoteRemoved(noteId);
            }

            @Override
            public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                view.onActionError(message);
            }
        });
    }

    /**
     * Walks away from a note somebody shared with the caller. Only a share
     * made with them personally can be left.
     */
    public void leave(Note note) {
        final int noteId = note.getId();
        ApiProvider.getQuicknotesAPI().leaveNote(noteId).enqueue(new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                view.onNoteRemoved(noteId);
            }

            @Override
            public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                view.onActionError(message);
            }
        });
    }

    public void emptyTrash() {
        view.showLoading();
        ApiProvider.getQuicknotesAPI().emptyTrash().enqueue(new ApiCallback<TrashResult>() {
            @Override
            public void onSuccess(TrashResult result) {
                view.hideLoading();
                view.onTrashEmptied(result != null ? result.getDestroyed() : 0);
            }

            @Override
            public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                view.hideLoading();
                view.onActionError(message);
            }
        });
    }

    private String getString(int resId) {
        return ((AppCompatActivity) view).getApplicationContext().getString(resId);
    }

    private void enqueueNoteAction(Call<Note> call) {
        call.enqueue(new ApiCallback<Note>() {
            @Override
            public void onSuccess(Note note) {
                if (note != null) {
                    view.onNoteUpdated(note);
                }
            }

            @Override
            public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                if (statusCode == HttpURLConnection.HTTP_PRECON_FAILED) {
                    // The note moved on since it was read into the list.
                    view.onActionError(getString(R.string.error_note_changed_elsewhere));
                    getNotes();
                    return;
                }
                view.onActionError(message);
            }
        });
    }
}
