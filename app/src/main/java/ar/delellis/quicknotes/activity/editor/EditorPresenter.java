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

package ar.delellis.quicknotes.activity.editor;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import ar.delellis.quicknotes.R;
import ar.delellis.quicknotes.api.ApiProvider;
import ar.delellis.quicknotes.model.Attachment;
import ar.delellis.quicknotes.model.Note;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditorPresenter {

    private EditorView view;

    public EditorPresenter(EditorView view) {
        this.view = view;
    }

    void createNote(Note note) {
        view.showProgress();

        Call<Note> call = ApiProvider.getQuicknotesAPI().create(note);
        call.enqueue(new Callback<Note>() {
            @Override
            public void onResponse(@NotNull Call<Note> call, @NotNull Response<Note> response) {
                ((AppCompatActivity) view).runOnUiThread(() -> {
                    view.hideProgress();
                    Context context = ((AppCompatActivity) view).getApplicationContext();
                    if (response.isSuccessful() && response.body() != null) {
                        view.onRequestSuccess(context.getString(R.string.note_saved, response.body().getTitle()));
                    } else {
                        view.onRequestError(context.getString(R.string.error_saving_note));
                    }
                });
            }

            @Override
            public void onFailure(@NotNull Call<Note> call, @NotNull Throwable t) {
                ((AppCompatActivity) view).runOnUiThread(() -> {
                    view.hideProgress();
                    view.onRequestError(t.getLocalizedMessage());
                });
            }
        });
    }

    void updateNote(Note note) {
        view.showProgress();

        Call<Note> call = ApiProvider.getQuicknotesAPI().updateNote(note.getId(), note);
        call.enqueue(new Callback<Note>() {
            @Override
            public void onResponse(@NonNull Call<Note> call, @NonNull Response<Note> response) {
                ((AppCompatActivity) view).runOnUiThread(() -> {
                    view.hideProgress();
                    Context context = ((AppCompatActivity) view).getApplicationContext();
                    if (response.isSuccessful() && response.body() != null) {
                        view.onRequestSuccess(context.getString(R.string.note_saved, response.body().getTitle()));
                    } else {
                        view.onRequestError(context.getString(R.string.error_saving_note));
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<Note> call, @NonNull Throwable t) {
                ((AppCompatActivity) view).runOnUiThread(() -> {
                    view.hideProgress();
                    view.onRequestError(t.getLocalizedMessage());
                });
            }
        });

    }

    void deleteNote(int id) {
        view.showProgress();

        Call<Note> call = ApiProvider.getQuicknotesAPI().deleteNote(id);
        call.enqueue(new Callback<Note>() {
            @Override
            public void onResponse(@NonNull Call<Note> call, @NonNull Response<Note> response) {
                ((AppCompatActivity) view).runOnUiThread(() -> {
                    view.hideProgress();
                    Context context = ((AppCompatActivity) view).getApplicationContext();
                    view.onRequestSuccess(context.getString(R.string.note_deleted));
                });
            }
            @Override
            public void onFailure(@NonNull Call<Note> call, @NonNull Throwable t) {
                ((AppCompatActivity) view).runOnUiThread(() -> {
                    view.hideProgress();
                    view.onRequestError(t.getLocalizedMessage());
                });
            }
        });
    }

    void uploadAttachment(MultipartBody.Part filePart) {
        view.showProgress();

        Call<Attachment> call = ApiProvider.getQuicknotesAPI().uploadAttachment(filePart);
        call.enqueue(new Callback<Attachment>() {
            @Override
            public void onResponse(@NotNull Call<Attachment> call, @NotNull Response<Attachment> response) {
                ((AppCompatActivity) view).runOnUiThread(() -> {
                    view.hideProgress();
                    view.addAttachment(response.body());
                });
            }
            @Override
            public void onFailure(@NotNull Call<Attachment> call, @NotNull Throwable t) {
                ((AppCompatActivity) view).runOnUiThread(() -> {
                    view.hideProgress();
                    view.onRequestError(t.getLocalizedMessage());
                });
            }
        });
    }

}
