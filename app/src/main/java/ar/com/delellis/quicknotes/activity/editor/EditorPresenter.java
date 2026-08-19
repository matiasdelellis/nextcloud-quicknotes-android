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

package ar.com.delellis.quicknotes.activity.editor;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.net.HttpURLConnection;

import ar.com.delellis.quicknotes.R;
import ar.com.delellis.quicknotes.api.ApiProvider;
import ar.com.delellis.quicknotes.api.helper.ApiCallback;
import ar.com.delellis.quicknotes.api.request.NoteRequest;
import ar.com.delellis.quicknotes.api.request.ReminderRequest;
import ar.com.delellis.quicknotes.model.AttachmentInfo;
import ar.com.delellis.quicknotes.model.Note;
import okhttp3.MultipartBody;
import retrofit2.Call;

public class EditorPresenter {

    private final EditorView view;

    public EditorPresenter(EditorView view) {
        this.view = view;
    }

    void createNote(Note note) {
        view.showProgress();

        ApiProvider.getQuicknotesAPI().createNote(NoteRequest.of(note)).enqueue(new ApiCallback<Note>() {
            @Override
            public void onSuccess(Note saved) {
                view.hideProgress();
                if (saved != null) {
                    view.onRequestSuccess(getString(R.string.note_saved, saved.getTitle()));
                } else {
                    view.onRequestError(getString(R.string.error_saving_note));
                }
            }

            @Override
            public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                view.hideProgress();
                view.onRequestError(message != null ? message : getString(R.string.error_saving_note));
            }
        });
    }

    /**
     * Saves a note, telling the server which version it was read at.
     *
     * A 412 means somebody else got there first. The body of that answer would
     * carry the note as it is now, but the SSO transport replaces error bodies
     * with the text of its own exception, so the current note is read back
     * before showing the user what they were about to overwrite.
     */
    void updateNote(Note note) {
        view.showProgress();

        ApiProvider.getQuicknotesAPI()
                .updateNote(note.getId(), note.getEtag(), NoteRequest.of(note))
                .enqueue(new ApiCallback<Note>() {
                    @Override
                    public void onSuccess(Note saved) {
                        view.hideProgress();
                        if (saved != null) {
                            view.onRequestSuccess(getString(R.string.note_saved, saved.getTitle()));
                        } else {
                            view.onRequestError(getString(R.string.error_saving_note));
                        }
                    }

                    @Override
                    public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                        if (statusCode == HttpURLConnection.HTTP_PRECON_FAILED) {
                            readNoteForConflict(note.getId());
                            return;
                        }

                        view.hideProgress();
                        if (statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
                            view.onRequestError(getString(R.string.error_note_is_read_only));
                        } else {
                            view.onRequestError(message != null ? message : getString(R.string.error_saving_note));
                        }
                    }
                });
    }

    private void readNoteForConflict(int noteId) {
        ApiProvider.getQuicknotesAPI().getNote(noteId).enqueue(new ApiCallback<Note>() {
            @Override
            public void onSuccess(Note current) {
                view.hideProgress();
                if (current != null) {
                    view.onConflict(current);
                } else {
                    view.onRequestError(getString(R.string.error_saving_note));
                }
            }

            @Override
            public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                view.hideProgress();
                view.onRequestError(getString(R.string.error_note_changed_elsewhere));
            }
        });
    }

    void destroyNote(int id) {
        view.showProgress();

        ApiProvider.getQuicknotesAPI().destroyNote(id).enqueue(new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                view.hideProgress();
                view.onRequestSuccess(getString(R.string.note_deleted));
            }

            @Override
            public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                view.hideProgress();
                view.onRequestError(message != null ? message : getString(R.string.error_deleting_note));
            }
        });
    }

    void trashNote(int id) {
        enqueueNoteAction(ApiProvider.getQuicknotesAPI().trashNote(id), R.string.note_trashed);
    }

    void restoreNote(int id) {
        enqueueNoteAction(ApiProvider.getQuicknotesAPI().restoreNote(id), R.string.note_restored);
    }

    void archiveNote(int id) {
        enqueueNoteAction(ApiProvider.getQuicknotesAPI().archiveNote(id), R.string.note_archived);
    }

    void unarchiveNote(int id) {
        enqueueNoteAction(ApiProvider.getQuicknotesAPI().unarchiveNote(id), R.string.note_unarchived);
    }

    /**
     * @param reminderAt UTC, {@code YYYY-MM-DD HH:MM:SS}, or null to cancel it.
     */
    void setReminder(int id, String reminderAt) {
        view.showProgress();

        ApiProvider.getQuicknotesAPI().setReminder(id, new ReminderRequest(reminderAt))
                .enqueue(new ApiCallback<Note>() {
                    @Override
                    public void onSuccess(Note note) {
                        view.hideProgress();
                        if (note != null) {
                            view.onNoteUpdated(note);
                        }
                    }

                    @Override
                    public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                        view.hideProgress();
                        view.onRequestError(message != null ? message : getString(R.string.error_setting_reminder));
                    }
                });
    }

    /** Walks away from a note somebody shared with the caller. */
    void leaveNote(int id) {
        view.showProgress();

        ApiProvider.getQuicknotesAPI().leaveNote(id).enqueue(new ApiCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                view.hideProgress();
                view.onRequestSuccess(getString(R.string.note_left));
            }

            @Override
            public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                view.hideProgress();
                view.onRequestError(message != null ? message : getString(R.string.error_leaving_note));
            }
        });
    }

    void uploadAttachment(MultipartBody.Part filePart) {
        view.showProgress();

        ApiProvider.getQuicknotesAPI().uploadAttachment(filePart).enqueue(new ApiCallback<AttachmentInfo>() {
            @Override
            public void onSuccess(AttachmentInfo attachment) {
                view.hideProgress();
                if (attachment != null) {
                    view.addAttachment(attachment);
                } else {
                    view.onRequestError(getString(R.string.error_uploading_attachment));
                }
            }

            @Override
            public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                view.hideProgress();
                view.onRequestError(message != null ? message : getString(R.string.error_uploading_attachment));
            }
        });
    }

    private void enqueueNoteAction(Call<Note> call, int successMessage) {
        view.showProgress();

        call.enqueue(new ApiCallback<Note>() {
            @Override
            public void onSuccess(Note note) {
                view.hideProgress();
                view.onRequestSuccess(getString(successMessage));
            }

            @Override
            public void onError(int statusCode, @Nullable String message, @Nullable String body) {
                view.hideProgress();
                view.onRequestError(message != null ? message : getString(R.string.error_unknown));
            }
        });
    }

    private Context getContext() {
        return ((AppCompatActivity) view).getApplicationContext();
    }

    private String getString(int resId, Object... formatArgs) {
        return getContext().getString(resId, formatArgs);
    }
}
